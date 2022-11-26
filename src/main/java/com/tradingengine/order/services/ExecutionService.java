package com.tradingengine.order.services;

import com.tradingengine.order.models.ClientOrder;
import com.tradingengine.order.models.Execution;
import com.tradingengine.order.models.ExecutionRegistrationRequest;
import com.tradingengine.order.repositories.ExecutionRepository;
import com.tradingengine.order.services.ClientOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExecutionService {

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private ClientOrderService clientOrderService;

    public Execution saveExecution(Long orderId, ExecutionRegistrationRequest request)
    {
        ClientOrder clientOrder = clientOrderService.fetchOrderByOrderId(orderId);

        Double engineProfit = clientOrder.getPrice() - request.price();

        Execution execution = Execution.builder()
                .clientOrder(clientOrder)
                .createdAt(LocalDate.now())
                .quantity(request.quantity())
                .price(request.price())
                .engineProfit(engineProfit)
                .build();

        return execution;
    }

    public List<Execution> fetchExecutionByOrder(Long orderId)
    {
        ClientOrder clientOrder = clientOrderService.fetchOrderByOrderId(orderId);

        return executionRepository.findAllByClientOrder(clientOrder);
    }
}
