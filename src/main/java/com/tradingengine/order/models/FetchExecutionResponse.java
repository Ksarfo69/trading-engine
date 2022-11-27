package com.tradingengine.order.models;

import java.time.LocalDate;

public record FetchExecutionResponse(
        LocalDate createdAt,
        Integer quantity,
        Double price
) {
}
