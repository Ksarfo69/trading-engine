package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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



        if(request.side() == Side.BUY)
        {
            clientOrder = ClientOrder.builder()
                    .holding(holding)
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


    public ClientOrder updateClientOrder(Long orderId, ClientOrder clientOrder) {

        ClientOrder repClientOrder = clientOrderRepository.findById(orderId).get();

        if(Objects.nonNull(clientOrder.getOrderStatus()))
        {
            repClientOrder.setOrderStatus(clientOrder.getOrderStatus());
        }

        if(Objects.nonNull(clientOrder.getProfit()))
        {
            repClientOrder.setProfit(clientOrder.getProfit());
        }

        return clientOrderRepository.save(repClientOrder);
    }
}
