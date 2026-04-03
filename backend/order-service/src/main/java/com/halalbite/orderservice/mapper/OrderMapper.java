package com.halalbite.orderservice.mapper;

import com.halalbite.orderservice.dto.OrderDto;
import com.halalbite.orderservice.entity.Order;
import com.halalbite.orderservice.entity.OrderLineItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    OrderDto.OrderResponse toResponse(Order order);

    OrderDto.LineItemResponse toLineItemResponse(OrderLineItem lineItem);

    @Mapping(target = "itemCount",
             expression = "java(order.getLineItems().size())")
    OrderDto.OrderSummaryResponse toSummaryResponse(Order order);

    List<OrderDto.OrderSummaryResponse> toSummaryResponseList(List<Order> orders);
}
