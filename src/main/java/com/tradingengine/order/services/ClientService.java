package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        return clientRepository.save(client);
    }


    public Client findClientByUserName(String username)
    {
        return clientRepository.findClientByUsername(username);
    }

    public List<Client> fetchAllClients()
    {
        return clientRepository.findAll();
    }


    public Portfolio createPortfolio(String username, PortfolioRegistrationRequest request)
    {
        return portfolioService.savePortfolio(username, request);
    }


    public List<Portfolio> fetchAllClientPortfolios(String username)
    {
        return portfolioService.fetchAllByClient(username);
    }


    public Holding createNewHolding(Portfolio portfolio, HoldingRegistrationRequest holdingRegistrationRequest)
    {
        return holdingService.saveHolding(portfolio, holdingRegistrationRequest);
    }


    public List<Holding> findHoldingsByPortfolio(Long portfolioId)
    {
        Portfolio portfolio = portfolioService.fetchPortfolioById(portfolioId);

        return holdingService.fetchAllHoldingByPortfolio(portfolio);
    }


    public ClientOrder createOrder(Long portfolioId, ClientOrderRegistrationRequest request)
    {
        Portfolio portfolio = portfolioService.fetchPortfolioById(portfolioId);
        Ticker ticker = tickerService.findTickerByTickerName(request.tickerName());

        //add the stock to holdings
        Holding holding = null;
        if(request.side() == Side.BUY)
        {
            holding = createNewHolding(portfolio, new HoldingRegistrationRequest(
                    ticker,
                    request.quantity()));
        }


        //create the order
        ClientOrder clientOrder = clientOrderService.saveOrder(holding, request);





        //return the order
        return clientOrder;
    }


    public List<ClientOrder> fetchAllClientOrders(String username)
    {
        return clientOrderService.fetchAllOrdersByUsername(username);
    }



}
