package com.halalbite.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * OrderLineItem — one food item within an order
 *
 * Why snapshot the price?
 * itemUnitPrice stores the price AT THE MOMENT the order was placed.
 * This is essential — if a restaurant raises their prices next week,
 * your order history must still show what you actually paid.
 * Never calculate historical order amounts from current menu prices.
 *
 * lineTotal = itemUnitPrice × quantity
 */
@Entity
@Table(name = "order_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Reference to menu-service item — stored as UUID
    @Column(name = "menu_item_id", nullable = false)
    private UUID menuItemId;

    // Snapshotted at order time — never changes after order is placed
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemUnitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;  // itemUnitPrice × quantity

    @Column(name = "special_requests")
    private String specialRequests;  // e.g. "no onions"
}
