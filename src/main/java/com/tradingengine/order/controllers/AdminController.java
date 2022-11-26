package com.tradingengine.order.controllers;

import com.tradingengine.order.models.Execution;
import com.tradingengine.order.models.ExecutionRegistrationRequest;
import com.tradingengine.order.services.AdminService;
import com.tradingengine.order.services.ExecutionService;
import com.tradingengine.order.models.Ticker;
import com.tradingengine.order.services.TickerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/ticker")
    public Ticker saveTicker(@RequestBody Ticker ticker)
    {
        return adminService.newTicker(ticker);
    }

    @PostMapping("/execute/{orderId}")
    public Execution saveExecution(@PathVariable(name = "orderId") Long orderId, @RequestBody ExecutionRegistrationRequest request)
    {
        return adminService.newExecution(orderId, request);
    }
}
