package com.tradingengine.order.controllers;

import com.tradingengine.order.models.*;
import com.tradingengine.order.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/client")
    public Client saveClient(@RequestBody Client client)
    {
        return clientService.saveClient(client);
    }


    @GetMapping("/client/{username}/portfolio")
    public List<Portfolio> fetchAllClientPortfolios(@PathVariable(name = "username") String username)
    {
        return clientService.fetchAllClientPortfolios(username);
    }

    @PostMapping("/client/{username}/portfolio")
    public Portfolio createNewPortfolio(@PathVariable(name="username") String username, @RequestBody PortfolioRegistrationRequest request)
    {
        return clientService.createPortfolio(username, request);
    }


    @PostMapping("/client/{portfolioId}/order")
    public ClientOrder createNewOrder(@PathVariable(name="portfolioId") Long portfolioId, @RequestBody ClientOrderRegistrationRequest request)
    {
        return clientService.createOrder(portfolioId, request);
    }


    @GetMapping("/client/{portfolioId}/holding")
    public List<Holding> findHoldingByPortfolio(@PathVariable(name="portfolioId") Long portfolioId)
    {
        return clientService.findHoldingsByPortfolio(portfolioId);
    }


    @GetMapping("/client/{username}")
    public List<ClientOrder> fetchAllClientOrders(@PathVariable(name = "username") String username)
    {
        return clientService.fetchAllClientOrders(username);
    }


}
