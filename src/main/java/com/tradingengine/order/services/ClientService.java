package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientOrderService clientOrderService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private TickerService tickerService;


    public Client saveClient(Client client)
    {
        log.info("saving client with details: {}", client);

        Client repClient = clientRepository.save(client);

        log.info("Client saved successfully: {}", repClient);

        return repClient;
    }


//    public Client findClientByUserName(String username)
//    {
//        return clientRepository.findClientByUsername(username);
//    }

//    public List<Client> fetchAllClients()
//    {
//        return clientRepository.findAll();
//    }


    public Portfolio createPortfolio(String username, PortfolioRegistrationRequest request)
    {

        log.info("Creating portfolio with details: {}", request);

        Portfolio portfolio = portfolioService.savePortfolio(username, request);

        log.info("Portfolio saved successfully: {}", portfolio);

        return portfolio;
    }


    public List<Portfolio> fetchAllClientPortfolios(String username)
    {
        log.info("Fetching Client: {} portfolios", username);

        List<Portfolio> portfolios = portfolioService.fetchAllByClient(username);

        log.info("Portfolio list for : {} fetched successfully: {}", username, portfolios);

        return portfolios;
    }


    public Holding createNewHolding(Portfolio portfolio, HoldingRegistrationRequest holdingRegistrationRequest)
    {
        log.info("Creating new holding with details: {}", holdingRegistrationRequest);

        Holding holding = holdingService.saveHolding(portfolio, holdingRegistrationRequest);

        log.info("Holding created successfully with details: {}", holding);
        return holding;
    }


    public List<Holding> findHoldingsByPortfolio(Long portfolioId)
    {
        log.info("Fetching portfolio with id: {}", portfolioId);

        Portfolio portfolio = portfolioService.fetchPortfolioById(portfolioId);

        log.info("Portfolio with id: {} found successfully with details {}", portfolioId, portfolio);


        log.info("Fetching holdings with portfolio : {}", portfolio);

        List<Holding> holdings = holdingService.fetchAllHoldingByPortfolio(portfolio);

        log.info("Holdings list for portfolio : {} found successfully with details : {}", portfolio, holdings);

        return holdings;
    }


    public ClientOrder createOrder(Long portfolioId, ClientOrderRegistrationRequest request)
    {
        log.info("Creating order with details: {}", request);


        log.info("Fetching portfolio with id: {}", portfolioId);

        Portfolio portfolio = portfolioService.fetchPortfolioById(portfolioId);

        log.info("Portfolio with id: {} found successfully with details {}", portfolioId, portfolio);


        log.info("Fetching ticker with name: {}", request.tickerName());

        Ticker ticker = tickerService.findTickerByTickerName(request.tickerName());

        log.info("Ticker with name: {} found successfully with details {}", request.tickerName(), ticker);



        //add the stock to holdings
        Holding holding = null;
        if(request.side() == Side.BUY)
        {
            log.info("Order side == buy, inserting asset : {} into portfolio : {} as holding", ticker, portfolio);
            holding = createNewHolding(portfolio, new HoldingRegistrationRequest(
                    ticker,
                    request.quantity()));
            log.info("Assert inserted successfully with holding details: {}", holding);
        }


        log.info("Creating order with holding: {}", holding);
        //create the order
        ClientOrder clientOrder = clientOrderService.saveOrder(holding, request);

        log.info("Order created successfully");


        //return the order
        return clientOrder;
    }




}
