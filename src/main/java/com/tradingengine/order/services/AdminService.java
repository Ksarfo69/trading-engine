package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class AdminService {

    @Autowired
    private TickerService tickerService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private ClientOrderService clientOrderService;

    @Autowired
    private HoldingService holdingService;


    public Ticker newTicker(Ticker ticker)
    {
        log.info("Saving ticker with details: {}", ticker);

        Ticker repTicker = tickerService.saveTicker(ticker);

        log.info("Ticker saved successfully with details: {}", ticker);

        return repTicker;
    }


    public Execution newExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        log.info("Execution received with details: {}", request);

        Execution repExecution = executionService.saveExecution(orderId, request).get();

        ClientOrder clientOrder = repExecution.getClientOrder();


        //buy order
        if(clientOrder.getSide() == Side.BUY)
        {
            performBuyExecutionProcedure(repExecution, clientOrder);
        }


        //sell order
        if(clientOrder.getSide() == Side.SELL) {
            performSellExecutionProcedure(repExecution, clientOrder);
        }

        log.info("Execution saved successfully with details: {}", repExecution);
        return repExecution;
    }


    public void updatePortfolioBalance(Execution execution, ClientOrder clientOrder)
    {
        //get portfolio
        log.info("Fetching portfolio from order.");
        Portfolio portfolio = execution
                        .getClientOrder()
                        .getPortfolio();


        log.info("Portfolio fetched successfully with details: {}", portfolio);


        //update balance
        log.info("Updating client portfolio balance. Initial : {}", portfolio.getBalance());

        if(clientOrder.getSide() == Side.BUY)
        {
            portfolio.setBalance(portfolio.getBalance() - execution.getPrice() * execution.getQuantity());
        }
        else
        {
            portfolio.setBalance(portfolio.getBalance() + execution.getPrice() * execution.getQuantity());
        }


        Portfolio repPortfolio = portfolioService.updatePortfolio(portfolio.getPortfolioId(), portfolio);

        log.info("Client balance updated successfully to {}", repPortfolio.getBalance());

    }


    public void performProfitCheckForFullExecution(Execution execution, ClientOrder clientOrder)
    {
        log.info("Running P&L assessment on order : {}", clientOrder);

        Double profit = (execution.getPrice() - clientOrder.getPrice()) * execution.getQuantity();

        clientOrder.setProfit(clientOrder.getProfit() + profit);

        ClientOrder repOrder = clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

        log.info("P&L assessed and updated in order table successfully with details : {}", repOrder);

        log.info("Getting the holding details for execution: {}", execution);
        Holding holding = clientOrder
                .getHolding();

        log.info("Updating accumulated profit on holding: {}", holding);

        holding.setAccumulatedProfit(holding.getAccumulatedProfit() + profit);
        holdingService.updateHolding(holding.getHoldingId(), holding);

        log.info("Accumulated profit updated successfully on holding: {}", holding);

    }


    public Boolean checkForFullOrderExecution(Execution execution) {

        ClientOrder clientOrder = execution.getClientOrder();

        //find accumulation from current execution
        Integer accumulatedQuantity = clientOrder.getHolding().getQuantity() + execution.getQuantity();

        //if executed fully
        if (accumulatedQuantity == clientOrder.getQuantity()) {
            return true;
        }
        return false;
    }


    public void performProfitCheckForPartialExecution(Execution execution, Holding holding)
    {
        //Execution by execution based updating of holding profit
        log.info("Calculating accumulated profit on holding: {}", holding);

        Double profit = (execution.getPrice() - execution.getClientOrder().getPrice()) * execution.getQuantity();

        log.info("Calculated profit is {}", profit);

        log.info("Updating accumulated profit on holding: {}", holding);

        holding.setAccumulatedProfit(holding.getAccumulatedProfit() + profit);
        holdingService.updateHolding(holding.getHoldingId(), holding);

        log.info("Accumulated profit updated successfully on holding: {}", holding);
    }


    public void performBuyExecutionProcedure(Execution execution, ClientOrder clientOrder) {
        log.info("Buy order execution, updating portfolio balance.");

        updatePortfolioBalance(execution, clientOrder);

        //add the stock to holdings
        //execution creates holding
        if (Objects.isNull(clientOrder.getHolding())) {
            log.info("Creating holding and attaching to portfolio: {}", clientOrder.getPortfolio());

            HoldingRegistrationRequest request = new HoldingRegistrationRequest(
                    clientOrder.getTicker(),
                    execution.getQuantity()
            );

            Holding holding = holdingService.saveHolding(clientOrder.getPortfolio(), request);

            log.info("Holding created successfully with details: {}", holding);


            //update client order
            clientOrder.setHolding(holding);

            log.info("Holding inserted successfully into order with details: {}", holding);

            //change order status to partially fulfilled if pending
            if (clientOrder.getOrderStatus() == OrderStatus.PENDING) {
                log.info("Order executed partially. Setting order status to partially fulfilled.");

                clientOrder.setOrderStatus(OrderStatus.PARTIALLY_FULFILLED);

                log.info("Order status successfully set to partially fulfilled");
            }
            return;
        }


        if(checkForFullOrderExecution(execution))
        {
            log.info("Order executed fully. updating client holding");
            //Client's holding
            Holding holding = clientOrder.getHolding();
            holding.setQuantity(holding.getQuantity() + execution.getQuantity());

            log.info("Client holding updated successfully");


            log.info("Setting order status to fulfilled.");
            clientOrder.setOrderStatus(OrderStatus.FULFILLED);
            clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

            log.info("Order status successfully set to fulfilled");
        }
        else {

            log.info("Order executed fully. updating client holding");
            //Client's holding
            Holding holding = clientOrder.getHolding();
            holding.setQuantity(holding.getQuantity() + execution.getQuantity());

            log.info("Client holding updated successfully");

            //change order status to partially fulfilled if pending
            if (clientOrder.getOrderStatus() == OrderStatus.PENDING) {
                log.info("Order executed partially. Setting order status to partially fulfilled.");

                clientOrder.setOrderStatus(OrderStatus.PARTIALLY_FULFILLED);

                log.info("Order status successfully set to partially fulfilled");
            }

            clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

            log.info("Order updated successfully");
        }

    }


    public void performSellExecutionProcedure(Execution execution, ClientOrder clientOrder)
    {
        log.info("Sell order execution received with details: {}", execution);

        //check that the execution is possible for the quantity of the asset
        log.info("Checking that the execution quantity is possible");

        Holding holding = clientOrder.getHolding();

        if(execution.getQuantity() <= holding.getQuantity())
        {
            log.info("Execution quantity valid, proceeding with execution");

            //if executed fully
            if (checkForFullOrderExecution(execution)) {

                //update holding
                log.info("updating holding to reflect execution");

                holding.setStatus(HoldingStatus.SOLD);
                holding.setQuantity(holding.getQuantity() - execution.getQuantity());
                holdingService.updateHolding(holding.getHoldingId(), holding);

                log.info("holding quantity updated successfully");



                // change order status to fulfilled;
                log.info("Order executed fully. Setting order status to fulfilled.");

                clientOrder.setOrderStatus(OrderStatus.FULFILLED);

                clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

                log.info("Order status successfully set to fulfilled");



                //calculate the profit
                performProfitCheckForFullExecution(execution, clientOrder);
            }
            else
            {
                //change asset quantity in holding
                log.info("updating holding quantity to reflect execution");

                holding.setQuantity(holding.getQuantity() - execution.getQuantity());
                holdingService.updateHolding(holding.getHoldingId(), holding);

                log.info("holding quantity updated successfully");


                //change order status to partially fulfilled
                if(clientOrder.getOrderStatus() == OrderStatus.PENDING)
                {
                    log.info("Order executed partially. Setting order status to partially fulfilled.");

                    clientOrder.setOrderStatus(OrderStatus.PARTIALLY_FULFILLED);

                    clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

                    log.info("Order status successfully set to partially fulfilled");
                }


                //check for profit
                performProfitCheckForPartialExecution(execution, holding);
            }

            //update portfolio balance to reflect execution
            updatePortfolioBalance(execution, clientOrder);
        }


        log.info("Profit of {} realised by engine", execution.getEngineProfit());
    }


    public void populateClientOrderHoldingOnBuyIfNull(Execution execution, ClientOrder clientOrder)
    {
        log.info("Creating holding and attaching to portfolio: {}", clientOrder.getPortfolio());

        HoldingRegistrationRequest request = new HoldingRegistrationRequest(
                clientOrder.getTicker(),
                execution.getQuantity()
        );

        Holding holding = holdingService.saveHolding(clientOrder.getPortfolio(), request);

        log.info("Holding created successfully with details: {}", holding);


        //update client order
        clientOrder.setHolding(holding);

        log.info("Holding inserted successfully into order with details: {}", holding);
    }
}
