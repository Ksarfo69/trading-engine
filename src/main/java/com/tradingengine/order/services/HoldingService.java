package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.HoldingRepository;
import com.tradingengine.order.repositories.PortfolioRepository;
import com.tradingengine.order.repositories.TickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HoldingService {

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private TickerRepository tickerRepository;

    public Holding saveHolding(Portfolio portfolio, HoldingRegistrationRequest request)
    {

        Holding holding = Holding.builder()
                .portfolio(portfolio)
                .ticker(request.ticker())
                .accumulatedProfit(0d)
                .quantity(request.quantity())
                .createdAt(LocalDate.now())
                .status(HoldingStatus.AVAILABLE)
                .build();

        return holdingRepository.save(holding);
    }

    public List<Holding> fetchAllHoldingByPortfolio(Portfolio portfolio)
    {
        return holdingRepository.findAllByPortfolio(portfolio);
    }
}
