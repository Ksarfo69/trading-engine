package com.tradingengine.order.services;

import com.tradingengine.order.models.ClientOrder;
import com.tradingengine.order.models.Execution;
import com.tradingengine.order.models.ExecutionRegistrationRequest;
import com.tradingengine.order.models.Side;
import com.tradingengine.order.repositories.ExecutionRepository;
import com.tradingengine.order.services.ClientOrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExecutionService {

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private ClientOrderService clientOrderService;

    public Optional<Execution> saveExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        ClientOrder clientOrder = clientOrderService.fetchOrderByOrderId(orderId);

        Execution execution;
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



         execution = Execution.builder()
                .clientOrder(clientOrder)
                .createdAt(LocalDate.now())
                .quantity(request.quantity())
                .price(request.price())
                .engineProfit(engineProfit)
                .build();

            return Optional.of(execution);
        }

        return Optional.empty();
    }

    public List<Execution> fetchExecutionByOrder(Long orderId)
    {
        ClientOrder clientOrder = clientOrderService.fetchOrderByOrderId(orderId);

        return executionRepository.findAllByClientOrder(clientOrder);
    }
}
