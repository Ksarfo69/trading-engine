package com.tradingengine.order.models;

public record ExecutionRegistrationRequest(
        Integer quantity,
        Double price
) {
}
