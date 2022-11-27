package com.tradingengine.order.services;

import com.tradingengine.order.models.*;
import com.tradingengine.order.repositories.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
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


    /** Receives a ticker from the request body and saves it into the repository
     * @param ticker
     * @return "Ticker saved successfully" string
     */
    public String newTicker(Ticker ticker)
    {
        log.info("Saving ticker with details: {}", ticker);

        Ticker repTicker = tickerRepository.save(ticker);

        log.info("Ticker saved successfully with details: {}", ticker);

        return "Ticker saved successfully";
    }


    /** Receives an orderId from the path variable and an ExecutionRegistration request
     * from the Request body.
     * It calls @method saveExecution and after that passes the execution to the order processing service.
     * @param orderId
     * @param request
     * @return "Execution processed successfully"
     */
    public String newExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        log.info("Execution received with details: {}", request);

        Execution repExecution = saveExecution(orderId, request).get();

        orderProcessingService.processExecution(repExecution);

        return "Execution processed successfully";
    }


    /** Receives an orderId and request from @method newExecution. validates the
     * execution and saves it if valid.
     * @param orderId
     * @param request
     * @return Optional execution depending on if the execution quantity is valid.
     */
    public Optional<Execution> saveExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        ClientOrder clientOrder = clientOrderRepository.findById(orderId).get();

        Holding holding = clientOrder.getHolding();

        //if execution quantity than stock available
        if(Objects.isNull(holding)
                && request.quantity() <= clientOrder.getQuantity()
                || Objects.nonNull(holding)
                && request.quantity() <= clientOrder.getQuantity() - holding.getQuantity()
        )
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
