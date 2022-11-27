package com.tradingengine.order.models;

public record FetchOrderResponse(
        Long orderId,
        String tickerName,
        Integer quantity,
        Double price,
        Side side,
        OrderStatus orderStatus,
        Double profit
) {
}
