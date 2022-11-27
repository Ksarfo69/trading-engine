package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.*;
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
    private ExecutionRepository executionRepository;

    @Autowired
    private ClientOrderRepository clientOrderRepository;

    @Autowired
    private TickerRepository tickerRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private HoldingRepository holdingRepository;


    public String newTicker(Ticker ticker)
    {
        log.info("Saving ticker with details: {}", ticker);

        Ticker repTicker = tickerRepository.save(ticker);

        log.info("Ticker saved successfully with details: {}", ticker);

        return "Ticker saved successfully";
    }

    public String newExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        log.info("Execution received with details: {}", request);

        Execution repExecution = saveExecution(orderId, request).get();

        processExecution(repExecution);

        return "Execution processed successfully";
    }


    public Optional<Execution> saveExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        ClientOrder clientOrder = clientOrderRepository.findById(orderId).get();

        //if execution quantity not greater that order quantity
        if(request.quantity() <= clientOrder.getQuantity())
        {
            Double engineProfit;
            if(clientOrder.getSide() == Side.BUY)
            {
                engineProfit = clientOrder.getPrice() - request.price();
            }
            else
            {
                engineProfit = request.price() - clientOrder.getPrice();
            }

            Execution execution = Execution.builder()
                    .clientOrder(clientOrder)
                    .createdAt(LocalDate.now())
                    .quantity(request.quantity())
                    .price(request.price())
                    .engineProfit(engineProfit)
                    .build();

            Execution repExecution = executionRepository.save(execution);

            return Optional.of(repExecution);
        }

        return Optional.empty();
    }

    public Execution processExecution(Execution repExecution)
    {
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


        Portfolio repPortfolio = updatePortfolio(portfolio.getPortfolioId(), portfolio);

        log.info("Client balance updated successfully to {}", repPortfolio.getBalance());

    }

    public Portfolio updatePortfolio(Long portfolioId, Portfolio portfolio) {
        Portfolio repPortfolio = portfolioRepository.findById(portfolioId).get();

        if(Objects.nonNull(portfolio.getPortfolioName()) && !"".equalsIgnoreCase(portfolio.getPortfolioName()))
        {
            repPortfolio.setPortfolioName(portfolio.getPortfolioName());
        }

        if(Objects.nonNull(portfolio.getBalance()))
        {
            repPortfolio.setBalance(portfolio.getBalance());
        }

        return portfolioRepository.save(repPortfolio);
    }



    public Boolean checkForFullOrderExecution(Execution execution) {

        ClientOrder clientOrder = execution.getClientOrder();

        //if executed fully for buy
        if (clientOrder.getSide() == Side.BUY &&
                clientOrder.getHolding().getQuantity()  == clientOrder.getQuantity())
        {
            return true;
        }
        else if(clientOrder.getSide() == Side.SELL &&
                clientOrder.getHolding().getQuantity()  <= 0)
        {
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
        updateHolding(holding.getHoldingId(), holding);

        log.info("Accumulated profit updated successfully on holding: {}", holding);
    }

    public Holding updateHolding(Long holdingId, Holding holding)
    {
        Holding repHolding = holdingRepository.findById(holdingId).get();

        if (Objects.nonNull(holding.getAccumulatedProfit()))
        {
            repHolding.setAccumulatedProfit(holding.getAccumulatedProfit());
        }

        if(Objects.nonNull(holding.getStatus()))
        {
            repHolding.setStatus(holding.getStatus());
        }

        if (Objects.nonNull(holding.getUpdatedAt()))
        {
            repHolding.setUpdatedAt(holding.getUpdatedAt());
        }

        return holdingRepository.save(repHolding);

    }


    public void performBuyExecutionProcedure(Execution execution, ClientOrder clientOrder) {
        log.info("Buy order execution, updating portfolio balance.");

        updatePortfolioBalance(execution, clientOrder);

        //add the stock to holdings
        //execution creates holding
        if (Objects.isNull(clientOrder.getHolding())) {

            populateClientHoldingOnBuyIfNull(execution, clientOrder);

            //change order status to partially fulfilled if pending
            if(checkForFullOrderExecution(execution))
            {
                performStatusUpdateOnFulfilledOrders(clientOrder);
            }
            else if (clientOrder.getOrderStatus() == OrderStatus.PENDING) {
                performStatusUpdateOnPartiallyFulfilledOrders(clientOrder);
            }
            return;
        }

        //update holding
        updateClientHoldingOnBuy(execution, clientOrder.getHolding());

        if(checkForFullOrderExecution(execution))
        {
            //update order status
            performStatusUpdateOnFulfilledOrders(clientOrder);
        }
        else {
            //change order status to partially fulfilled if pending
            if (clientOrder.getOrderStatus() == OrderStatus.PENDING) {
                performStatusUpdateOnPartiallyFulfilledOrders(clientOrder);
            }
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

            //update holding
            updateClientHoldingOnSell(execution, holding);

            //if executed fully
            if (checkForFullOrderExecution(execution)) {

                holding.setStatus(HoldingStatus.SOLD);
                updateHolding(holding.getHoldingId(), holding);

                //insert profit of fulfilled sell order to order table;
                clientOrder.setProfit(holding.getAccumulatedProfit());
                updateClientOrder(clientOrder.getOrderId(), clientOrder);

                // change order status to fulfilled;
                performStatusUpdateOnFulfilledOrders(clientOrder);

            }
            else
            {
                //change order status to partially fulfilled
                if(clientOrder.getOrderStatus() == OrderStatus.PENDING)
                {
                    performStatusUpdateOnPartiallyFulfilledOrders(clientOrder);
                }
            }

            //check for profit
            performProfitCheckForPartialExecution(execution, holding);

            //update portfolio balance to reflect execution
            updatePortfolioBalance(execution, clientOrder);
        }


        log.info("Profit of {} realised by engine", execution.getEngineProfit());
    }

    public String updateClientOrder(Long orderId, ClientOrder clientOrder) {

        ClientOrder repClientOrder = clientOrderRepository.findById(orderId).get();

        if(Objects.nonNull(clientOrder.getOrderStatus()))
        {
            repClientOrder.setOrderStatus(clientOrder.getOrderStatus());
        }

        if(Objects.nonNull(clientOrder.getProfit()))
        {
            repClientOrder.setProfit(clientOrder.getProfit());
        }

        clientOrderRepository.save(repClientOrder);

        return "ClientOrder updated successfully";
    }

    public void populateClientHoldingOnBuyIfNull(Execution execution, ClientOrder clientOrder)
    {
        log.info("Creating holding and attaching to portfolio: {}", clientOrder.getPortfolio());

        HoldingRegistrationRequest request = new HoldingRegistrationRequest(
                execution.getQuantity()
        );

        Holding holding = saveHolding(clientOrder.getPortfolio(), request);

        log.info("Holding created successfully with details: {}", holding);


        //update client order
        clientOrder.setHolding(holding);

        log.info("Holding inserted successfully into order with details: {}", holding);
    }

    public Holding saveHolding(Portfolio portfolio, HoldingRegistrationRequest request)
    {
        Holding holding = Holding.builder()
                .portfolio(portfolio)
                .quantity(request.quantity())
                .accumulatedProfit(0d)
                .createdAt(LocalDate.now())
                .status(HoldingStatus.AVAILABLE)
                .build();

        return holdingRepository.save(holding);
    }

    public void performStatusUpdateOnPartiallyFulfilledOrders(ClientOrder clientOrder)
    {
        log.info("Order executed partially. Setting order status to partially fulfilled.");

        clientOrder.setOrderStatus(OrderStatus.PARTIALLY_FULFILLED);

        updateClientOrder(clientOrder.getOrderId(), clientOrder);

        log.info("Order status successfully set to partially fulfilled");
    }

    public void performStatusUpdateOnFulfilledOrders(ClientOrder clientOrder)
    {
        log.info("Order executed fully. Setting order status to fulfilled.");

        clientOrder.setOrderStatus(OrderStatus.FULFILLED);

        updateClientOrder(clientOrder.getOrderId(), clientOrder);

        log.info("Order status successfully set to fulfilled");
    }

    public void updateClientHoldingOnBuy(Execution execution, Holding holding)
    {
        log.info("Updating client holding");

        //Client's holding
        holding.setQuantity(holding.getQuantity() + execution.getQuantity());

        log.info("Client holding updated successfully");
    }

    public void updateClientHoldingOnSell(Execution execution, Holding holding)
    {
        log.info("updating holding to reflect execution");

        holding.setQuantity(holding.getQuantity() - execution.getQuantity());

        log.info("holding quantity updated successfully");
    }
}
