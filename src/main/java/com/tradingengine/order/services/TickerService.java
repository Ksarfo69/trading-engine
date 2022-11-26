package com.tradingengine.order.services;

import com.tradingengine.order.models.Ticker;
import com.tradingengine.order.repositories.TickerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TickerService {

    @Autowired
    private TickerRepository tickerRepository;

    public Ticker saveTicker(Ticker ticker)
    {
        return tickerRepository.save(ticker);
    }


    public Ticker findTickerByTickerName(String tickerName)
    {
        return tickerRepository.findTickerByTickerName(tickerName);
    }


}