package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class AdminService {

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private ClientOrderRepository clientOrderRepository;

    @Autowired
    private TickerRepository tickerRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private OrderProcessingService orderProcessingService;


    /**
     * @param ticker
     * @return
     */
    public String newTicker(Ticker ticker)
    {
        log.info("Saving ticker with details: {}", ticker);

        Ticker repTicker = tickerRepository.save(ticker);

        log.info("Ticker saved successfully with details: {}", ticker);

        return "Ticker saved successfully";
    }


    public String newExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        log.info("Execution received with details: {}", request);

        Execution repExecution = saveExecution(orderId, request).get();

        orderProcessingService.processExecution(repExecution);

        return "Execution processed successfully";
    }


    public Optional<Execution> saveExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        ClientOrder clientOrder = clientOrderRepository.findById(orderId).get();

        //if execution quantity not greater that order quantity
        if(request.quantity() <= clientOrder.getQuantity())
        {
            Double engineProfit;
            if(clientOrder.getSide() == Side.BUY)
            {
                engineProfit = clientOrder.getPrice() - request.price();
            }
            else
            {
                engineProfit = request.price() - clientOrder.getPrice();
            }

            Execution execution = Execution.builder()
                    .clientOrder(clientOrder)
                    .timestamp(Timestamp.from(Instant.now()))
                    .quantity(request.quantity())
                    .price(request.price())
                    .engineProfit(engineProfit)
                    .build();

            Execution repExecution = executionRepository.save(execution);

            return Optional.of(repExecution);
        }

        return Optional.empty();
    }


}
