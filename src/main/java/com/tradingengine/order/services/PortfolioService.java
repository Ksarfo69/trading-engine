package com.tradingengine.order.services;

import com.tradingengine.order.models.Client;
import com.tradingengine.order.models.Portfolio;
import com.tradingengine.order.models.PortfolioRegistrationRequest;
import com.tradingengine.order.repositories.ClientRepository;
import com.tradingengine.order.repositories.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ClientRepository clientRepository;


    public Portfolio savePortfolio(String username, PortfolioRegistrationRequest request)
    {
        Client client = clientRepository.findClientByUsername(username);

        Portfolio portfolio = Portfolio.builder()
                .portfolioName(request.portfolioName())
                .client(client)
                .balance(request.balance())
                .build();

        return portfolioRepository.save(portfolio);
    }

    public Portfolio fetchPortfolioById(Long portfolioId)
    {
        return portfolioRepository.findById(portfolioId).get();
    }

    public List<Portfolio> fetchAllPortfolio()
    {
        return portfolioRepository.findAll();
    }


    public List<Portfolio> fetchAllByClient(String username)
    {
        Client client = clientRepository.findClientByUsername(username);

        return portfolioRepository.findAllByClient(client);
    }

    public Portfolio fetchOnePortfolioByUsername(String username)
    {
        Client client = clientRepository.findClientByUsername(username);

        return portfolioRepository.findOneByClient(client);
    }
}
