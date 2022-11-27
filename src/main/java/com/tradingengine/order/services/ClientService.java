package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private ClientOrderRepository clientOrderRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private TickerRepository tickerRepository;


    /**
     * @param client
     * @return
     */
    public String saveClient(Client client)
    {
        log.info("saving client with details: {}", client);

        Client repClient = clientRepository.save(client);

        log.info("Client saved successfully: {}", repClient);

        return "Client saved successfully";
    }


    /**
     * @param username
     * @param request
     * @return
     */
    public String createPortfolio(String username, PortfolioRegistrationRequest request)
    {
        log.info("Creating portfolio with details: {}", request);

        Client client = clientRepository.findClientByUsername(username);

        Portfolio portfolio = Portfolio.builder()
                .portfolioName(request.portfolioName())
                .client(client)
                .balance(request.balance())
                .build();

        log.info("Portfolio saved successfully: {}", portfolio);

        portfolioRepository.save(portfolio);

        return "Portfolio Created Successfully";
    }


    /**
     * @param username
     * @return
     */
    public List<FetchPortfolioResponse> fetchAllClientPortfolios(String username)
    {
        log.info("Fetching Client: {} portfolios", username);

        Client client = clientRepository.findClientByUsername(username);

        List<Portfolio> portfolios = portfolioRepository.findAllByClient(client)
                .stream().limit(10).collect(Collectors.toList());

        List<FetchPortfolioResponse> responseList = new ArrayList<>();

        for(Portfolio portfolio : portfolios)
        {
            responseList.add(
                    new FetchPortfolioResponse(
                            portfolio.getPortfolioName(),
                            portfolio.getBalance()
                    )
            );
        }

        log.info("Portfolio list for : {} fetched successfully: {}", username, portfolios);

        return responseList;
    }


    /**
     * @param portfolioId
     * @return
     */
    public List<Holding> findHoldingsByPortfolio(Long portfolioId)
    {
        log.info("Fetching portfolio with id: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId).get();

        log.info("Portfolio with id: {} found successfully with details {}", portfolioId, portfolio);


        log.info("Fetching holdings with portfolio : {}", portfolio);

        List<Holding> holdings = holdingRepository.findAllByPortfolio(portfolio);

        log.info("Holdings list for portfolio : {} found successfully with details : {}", portfolio, holdings);

        return holdings;
    }


    /**
     * @param portfolioId
     * @param request
     * @return
     */
    public String createOrder(Long portfolioId, ClientOrderRegistrationRequest request)
    {
        //create the order
        ClientOrder clientOrder;

        //get the portfolio
        log.info("Fetching portfolio with id: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId).get();

        log.info("Portfolio fetched successfully with details: {}", portfolio);


        log.info("Fetching ticker with name: {}", request.tickerName());

        Ticker ticker = tickerRepository.findTickerByTickerName(request.tickerName());

        log.info("Ticker fetched successfully with details: {}", ticker);


        if(request.side() == Side.BUY)
        {
            log.info("Creating client buy side order.");
            clientOrder = ClientOrder.builder()
                    .portfolio(portfolio)
                    .ticker(ticker)
                    .quantity(request.quantity())
                    .price(request.price())
                    .side(request.side())
                    .orderStatus(OrderStatus.PENDING)
                    .build();
        }
        else
        {
            log.info("Creating client buy side order.");

            log.info("Retrieving holding info with id: {}", request.holdingId());

            Holding holding = holdingRepository.findById(request.holdingId()).get();

            log.info("Holding info retrieved successfully with details: {}", holding);


            clientOrder = ClientOrder.builder()
                    .portfolio(portfolio)
                    .ticker(ticker)
                    .holding(holding)
                    .quantity(request.quantity())
                    .price(request.price())
                    .side(request.side())
                    .profit(0d)
                    .orderStatus(OrderStatus.PENDING)
                    .build();
        }

        ClientOrder repOrder = clientOrderRepository.save(clientOrder);

        log.info("Client order created successfully with details: {}", repOrder);
        return repOrder.getOrderId().toString();
    }


    /**
     * @param portfolioId
     * @return
     */
    public List<FetchOrderResponse> fetchAllClientHoldingByPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).get();

        List<FetchOrderResponse> responseList = new ArrayList<>();

        List<ClientOrder> repOrders = clientOrderRepository.findAllByPortfolio(portfolio)
                .stream().limit(10).collect(Collectors.toList());

        for(ClientOrder order : repOrders)
        {
            responseList.add(new FetchOrderResponse(
                    order.getOrderId(), order.getTicker().getTickerName(),
                    order.getQuantity(), order.getPrice(), order.getSide(),
                    order.getOrderStatus(), order.getProfit()
            ));
        }

        return responseList;
    }


    /**
     * @param orderId
     * @return
     */
    public List<FetchExecutionResponse> fetchAllExecutionsForClientOrder(Long orderId) {

        log.info("Fetching all execution for order: {}", orderId);
        ClientOrder clientOrder = clientOrderRepository.findById(orderId).get();

        log.info("Client order fetched successfully with details: {}", clientOrder);


        List<FetchExecutionResponse> responseList = new ArrayList<>();

        List<Execution> repExecutions = executionRepository.findAllByClientOrder(clientOrder)
                .stream().limit(10).collect(Collectors.toList());

        log.info("Executions received from repository: {}", repExecutions);

        for(Execution execution : repExecutions)
        {
            responseList.add(
                    new FetchExecutionResponse(
                            execution.getTimestamp(),
                            execution.getQuantity(),
                            execution.getPrice()
                    )
            );
        }

        log.info("Fetched all executions for order: {} with details", orderId, responseList);
        return responseList;
    }
}
