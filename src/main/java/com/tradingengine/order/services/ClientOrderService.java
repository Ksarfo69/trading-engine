package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@Transactional
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

    public ClientOrder saveOrder(Long portfolioId, ClientOrderRegistrationRequest request)
    {
        ClientOrder clientOrder;

        //get the portfolio
        log.info("Fetching portfolio with id: {}", portfolioId);

        Portfolio portfolio = portfolioRepository.findById(portfolioId).get();

        log.info("Portfolio fetched successfully with details: {}", portfolio);


        log.info("Fetching ticker with name: {}", request.tickerName());

        Ticker ticker = tickerRepository.findTickerByTickerName(request.tickerName());

        log.info("Ticker fetched successfully with details: {}", ticker);


        if(request.side() == Side.BUY)
        {
            clientOrder = ClientOrder.builder()
                    .portfolio(portfolio)
                    .ticker(ticker)
                    .quantity(request.quantity())
                    .price(request.price())
                    .side(request.side())
                    .orderStatus(OrderStatus.PENDING)
                    .build();
        }
        else
        {
            Holding holding = holdingRepository.findById(request.holdingId()).get();

            clientOrder = ClientOrder.builder()
                    .portfolio(portfolio)
                    .ticker(ticker)
                    .holding(holding)
                    .quantity(request.quantity())
                    .price(request.price())
                    .side(request.side())
                    .profit(0d)
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
