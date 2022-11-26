package com.tradingengine.order.models;

public record ClientOrderRegistrationRequest(
        Long holdingId,
        String tickerName,
        Integer quantity,
        Double price,
        Side side
) {
}
