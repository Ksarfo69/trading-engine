package com.tradingengine.order.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Exchange {
    @Id
    @SequenceGenerator(
            name = "exchange_id_sequence",
            sequenceName = "exchange_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "exchange_id_sequence"
    )
    private Long exchangeId;

    @Column(name = "exchangeName")
    private String exchangeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "exchangeStatus")
    private ExchangeStatus exchangeStatus;
}
