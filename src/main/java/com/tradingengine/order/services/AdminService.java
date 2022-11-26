package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
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

        Execution repExecution = executionService.saveExecution(orderId, request);


        //buy order
        if(repExecution.getClientOrder().getSide() == Side.BUY)
        {

            log.info("Buy order execution, updating portfolio balance.");

            updatePortfolioBalance(repExecution);

            if(checkForFullOrderExecution(repExecution))
            {
                log.info("Order executed fully. Setting order status to fulfilled.");

                ClientOrder clientOrder = repExecution.getClientOrder();

                clientOrder.setOrderStatus(OrderStatus.FULFILLED);

                clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

                log.info("Order status successfully set to fulfilled");
            }
        }


        //sell order
        if(repExecution.getClientOrder().getSide() == Side.SELL) {
            log.info("Sell order execution received with details: {}", repExecution);


            //if executed fully
            if (checkForFullOrderExecution(repExecution)) {

                log.info("Order executed fully. Setting order status to fulfilled.");

                ClientOrder clientOrder = repExecution.getClientOrder();

                clientOrder.setOrderStatus(OrderStatus.FULFILLED);

                clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

                log.info("Order status successfully set to fulfilled");


                //calculate the profit
                runProfitCheckForFullExecution(repExecution, clientOrder);
            }
            else
            {
                runProfitCheckPerExecution(repExecution);
            }

            updatePortfolioBalance(repExecution);
        }


        log.info("Execution saved successfully with details: {}", repExecution);
        log.info("Profit of {} realised by engine", repExecution.getEngineProfit());

        return repExecution;
    }


    public Portfolio updatePortfolioBalance(Execution execution)
    {
        log.info("Fetching portfolio from order.");
        Portfolio portfolio = portfolioService.fetchPortfolioById(
                execution
                        .getClientOrder()
                        .getHolding()
                        .getPortfolio()
                        .getPortfolioId());

        log.info("Portfolio fetched successfully with details: {}", portfolio);


        log.info("Updating client portfolio balance. Initial : {}", portfolio.getBalance());

        portfolio.setBalance(portfolio.getBalance() - execution.getPrice() * execution.getQuantity());

        Portfolio repPortfolio = portfolioService.updatePortfolio(portfolio.getPortfolioId(), portfolio);

        log.info("Client balance updated successfully to {}", repPortfolio.getBalance());

        return repPortfolio;
    }

    public ClientOrder runProfitCheckForFullExecution(Execution execution, ClientOrder clientOrder)
    {
        log.info("Running P&L assessment on order : {}", clientOrder);

        Double profit = (execution.getPrice() - clientOrder.getPrice()) * execution.getQuantity();

        clientOrder.setProfit(clientOrder.getProfit() + profit);

        ClientOrder repOrder = clientOrderService.updateClientOrder(clientOrder.getOrderId(), clientOrder);

        log.info("P&L assessed and updated in order table successfully with details : {}", repOrder);

        return repOrder;
    }


    public Boolean checkForFullOrderExecution(Execution execution) {

        //if executed fully
        if (execution.getQuantity() == execution.getClientOrder().getQuantity()) {

            //if it was a sell side, upgrade holding status
            if(execution.getClientOrder().getSide() == Side.SELL)
            {
                Holding holding = execution
                        .getClientOrder()
                        .getHolding();

                holding.setStatus(HoldingStatus.SOLD);
                holdingService.updateHolding(holding.getHoldingId(), holding);
            }
            return true;
        }
        return false;
    }


    public void runProfitCheckPerExecution(Execution execution)
    {
        //Execution by execution based updating of holding profit
        log.info("Getting the holding details for execution: {}", execution);
        Holding holding = execution
                .getClientOrder()
                .getHolding();

        log.info("Holding details retrieved successfully with: {}", holding);

        log.info("Calculating accumulated profit on holding: {}", holding);

        Double profit = (execution.getPrice() - execution.getClientOrder().getPrice()) * execution.getQuantity();

        log.info("Calculated profit is {}", profit);

        log.info("Updating accumulated profit on holding: {}", holding);

        holding.setAccumulatedProfit(holding.getAccumulatedProfit() + profit);
        holdingService.updateHolding(holding.getHoldingId(), holding);

        log.info("Accumulated profit updated successfully on holding: {}", holding);
    }

}
