package com.tradingengine.order.models;

public record HoldingRegistrationRequest(
        Ticker ticker,
        Integer quantity
) {
}
