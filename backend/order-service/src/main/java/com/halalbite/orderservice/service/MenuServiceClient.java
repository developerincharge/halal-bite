package com.halalbite.orderservice.service;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * MenuServiceClient — Feign client for calling menu-service
 *
 * What is Feign?
 * OpenFeign lets you call another microservice's REST API as if it
 * were a simple Java method call. You define an interface,
 * annotate it with the endpoint details, and Spring generates
 * the actual HTTP client code automatically.
 *
 * Instead of writing:
 *   RestTemplate restTemplate = new RestTemplate();
 *   String url = "http://menu-service/api/v1/menus/items/" + itemId + "/price";
 *   ItemPriceResponse response = restTemplate.getForObject(url, ItemPriceResponse.class);
 *
 * You just write:
 *   menuServiceClient.getItemPrice(itemId);
 *
 * The name "menu-service" in @FeignClient must match the
 * spring.application.name in menu-service's application.yml.
 * Eureka uses this name to find the service's address.
 */
@FeignClient(name = "menu-service", path = "/api/v1/menus")
public interface MenuServiceClient {

    @GetMapping("/items/{itemId}/price")
    ItemPriceResponse getItemPrice(@PathVariable UUID itemId);

    /**
     * Local DTO matching menu-service's ItemPriceResponse.
     * We only need the fields relevant to order processing.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ItemPriceResponse {
        private UUID id;
        private String name;
        private BigDecimal price;
        private BigDecimal discountedPrice;
        private Boolean isAvailable;
        private UUID restaurantId;

        // Returns discountedPrice if set, otherwise full price
        public BigDecimal getEffectivePrice() {
            return discountedPrice != null ? discountedPrice : price;
        }
    }
}
