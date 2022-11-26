package com.tradingengine.order.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ClientOrder {
    @Id
    @SequenceGenerator(
            name = "order_id_sequence",
            sequenceName = "order_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "order_id_sequence"
    )
    private Long orderId;

    @ManyToOne
    @JoinColumn(referencedColumnName = "holdingId")
    private Holding holding;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "side")
    private Side side;

    @Enumerated(EnumType.STRING)
    @Column(name = "orderStatus")
    private  OrderStatus orderStatus;

    @Column(name = "profit")
    private Double profit;
}