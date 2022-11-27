package com.tradingengine.order.services;

import com.tradingengine.order.models.Client;
import com.tradingengine.order.models.Holding;
import com.tradingengine.order.models.Portfolio;
import com.tradingengine.order.models.PortfolioRegistrationRequest;
import com.tradingengine.order.repositories.ClientRepository;
import com.tradingengine.order.repositories.HoldingRepository;
import com.tradingengine.order.repositories.PortfolioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HoldingRepository holdingRepository;


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
}
