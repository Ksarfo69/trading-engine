package com.tradingengine.order.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Execution {
    @Id
    @SequenceGenerator(
            name = "execution_id_sequence",
            sequenceName = "execution_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "execution_id_sequence"
    )
    private Long executionId;

    @ManyToOne
    @JoinColumn(referencedColumnName = "orderId", nullable = false)
    private ClientOrder clientOrder;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Double price;

    @Column(name = "engineProfit")
    private Double engineProfit;
}
