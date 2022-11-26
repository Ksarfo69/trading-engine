package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientOrderService {

    @Autowired
    private ClientOrderRepository clientOrderRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private TickerRepository tickerRepository;

    @Autowired
    private ClientRepository clientRepository;

    public ClientOrder saveOrder(Holding holding, ClientOrderRegistrationRequest request)
    {
        ClientOrder clientOrder;


        Ticker ticker = tickerRepository.findTickerByTickerName(request.tickerName());

        if(request.side() == Side.BUY)
        {
            clientOrder = ClientOrder.builder()
                    .holding(holding)
                    .ticker(ticker)
                    .quantity(request.quantity())
                    .price(request.price())
                    .side(request.side())
                    .orderStatus(OrderStatus.PENDING)
                    .build();
        }
        else
        {
            Holding sellingHolding = holdingRepository.findById(request.holdingId()).get();

            clientOrder = ClientOrder.builder()
                    .holding(sellingHolding)
                    .ticker(ticker)
                    .quantity(request.quantity())
                    .price(request.price())
                    .side(request.side())
                    .orderStatus(OrderStatus.PENDING)
                    .build();
        }


        return clientOrderRepository.save(clientOrder);
    }


    public ClientOrder fetchOrderByOrderId(Long orderId)
    {
        return clientOrderRepository.findById(orderId).get();
    }

    public List<ClientOrder> fetchAllOrders()
    {
        return clientOrderRepository.findAll();
    }


    public List<ClientOrder> fetchAllOrdersByUsername(String username)
    {
        Client client = clientRepository.findClientByUsername(username);

        Portfolio portfolio = portfolioRepository.findOneByClient(client);

        return clientOrderRepository.findAllByHolding(portfolio);
    }
}
