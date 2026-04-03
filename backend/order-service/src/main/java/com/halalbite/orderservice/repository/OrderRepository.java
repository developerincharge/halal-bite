package com.halalbite.orderservice.repository;

import com.halalbite.orderservice.entity.Order;
import com.halalbite.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Customer's order history — paginated
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    // Restaurant's incoming orders — paginated
    Page<Order> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId, Pageable pageable);

    // Restaurant's active orders (not delivered or cancelled)
    List<Order> findByRestaurantIdAndStatusIn(UUID restaurantId, List<OrderStatus> statuses);

    // Customer can only view their own order
    Optional<Order> findByIdAndCustomerId(UUID id, UUID customerId);

    // Restaurant can only view their own orders
    Optional<Order> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
