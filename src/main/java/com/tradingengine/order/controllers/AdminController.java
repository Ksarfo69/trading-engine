package com.tradingengine.order.controllers;

import com.tradingengine.order.models.Ticker;
import com.tradingengine.order.services.TickerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @Autowired
    private TickerService tickerService;

    @PostMapping("/ticker")
    public Ticker saveTicker(@RequestBody Ticker ticker)
    {
        return tickerService.saveTicker(ticker);
    }
}
