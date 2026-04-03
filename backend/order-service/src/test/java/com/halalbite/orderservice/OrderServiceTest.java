package com.halalbite.orderservice.service;

import com.halalbite.orderservice.dto.OrderDto;
import com.halalbite.orderservice.entity.Order;
import com.halalbite.orderservice.entity.OrderStatus;
import com.halalbite.orderservice.exception.OrderExceptions;
import com.halalbite.orderservice.mapper.OrderMapper;
import com.halalbite.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MenuServiceClient menuServiceClient;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock private OrderMapper orderMapper;
    @InjectMocks private OrderService orderService;

    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID MENU_ITEM_ID = UUID.randomUUID();
    private final UUID ORDER_ID = UUID.randomUUID();

    private Order testOrder;
    private MenuServiceClient.ItemPriceResponse menuItem;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
            .id(ORDER_ID)
            .customerId(CUSTOMER_ID)
            .restaurantId(RESTAURANT_ID)
            .status(OrderStatus.PENDING)
            .subtotal(new BigDecimal("12.99"))
            .deliveryFee(new BigDecimal("2.99"))
            .platformFeeAmount(new BigDecimal("1.95"))
            .totalAmount(new BigDecimal("15.98"))
            .build();

        menuItem = new MenuServiceClient.ItemPriceResponse(
            MENU_ITEM_ID,
            "Big Halal Burger",
            new BigDecimal("12.99"),
            null,
            true,
            RESTAURANT_ID
        );
    }

    @Test
    @DisplayName("createOrder — should create order with correct totals")
    void createOrder_success() {
        OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
            .restaurantId(RESTAURANT_ID)
            .items(List.of(OrderDto.OrderLineItemRequest.builder()
                .menuItemId(MENU_ITEM_ID).quantity(1).build()))
            .deliveryStreetAddress("123 Main St")
            .deliveryCity("Chicago")
            .deliveryPostalCode("60601")
            .build();

        OrderDto.OrderResponse response = OrderDto.OrderResponse.builder()
            .id(ORDER_ID).totalAmount(new BigDecimal("15.98")).build();

        when(menuServiceClient.getItemPrice(MENU_ITEM_ID)).thenReturn(menuItem);
        when(orderRepository.save(any())).thenReturn(testOrder);
        when(orderMapper.toResponse(testOrder)).thenReturn(response);

        OrderDto.OrderResponse result = orderService.createOrder(request, CUSTOMER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo("15.98");
        verify(orderRepository).save(any(Order.class));
        verify(kafkaTemplate).send(eq("order.placed"), anyString(), any());
    }

    @Test
    @DisplayName("createOrder — should throw if item is unavailable")
    void createOrder_unavailableItem_throws() {
        menuItem.setIsAvailable(false);

        OrderDto.CreateOrderRequest request = OrderDto.CreateOrderRequest.builder()
            .restaurantId(RESTAURANT_ID)
            .items(List.of(OrderDto.OrderLineItemRequest.builder()
                .menuItemId(MENU_ITEM_ID).quantity(1).build()))
            .deliveryStreetAddress("123 Main St")
            .deliveryCity("Chicago")
            .deliveryPostalCode("60601")
            .build();

        when(menuServiceClient.getItemPrice(MENU_ITEM_ID)).thenReturn(menuItem);

        assertThatThrownBy(() -> orderService.createOrder(request, CUSTOMER_ID))
            .isInstanceOf(OrderExceptions.MenuItemUnavailableException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("cancelOrder — should throw if order is already PREPARING")
    void cancelOrder_alreadyPreparing_throws() {
        testOrder.setStatus(OrderStatus.PREPARING);
        when(orderRepository.findByIdAndCustomerId(ORDER_ID, CUSTOMER_ID))
            .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID, CUSTOMER_ID))
            .isInstanceOf(OrderExceptions.OrderNotCancellableException.class);
    }

    @Test
    @DisplayName("updateOrderStatus — should throw for invalid transition")
    void updateStatus_invalidTransition_throws() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findByIdAndRestaurantId(ORDER_ID, RESTAURANT_ID))
            .thenReturn(Optional.of(testOrder));

        OrderDto.UpdateStatusRequest request = OrderDto.UpdateStatusRequest.builder()
            .status(OrderStatus.PREPARING).build();

        assertThatThrownBy(() ->
            orderService.updateOrderStatus(ORDER_ID, request, RESTAURANT_ID))
            .isInstanceOf(OrderExceptions.InvalidOrderStatusException.class);
    }
}
