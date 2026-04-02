package com.halalbite.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Logging Filter
 *
 * What this does:
 * - Logs every incoming request (method + path)
 * - Logs every outgoing response (status code)
 * - Logs how long each request took (in milliseconds)
 *
 * Why is this useful?
 * When debugging, you can see exactly what the gateway received,
 * where it routed the request, and whether it succeeded.
 *
 * Example log output:
 *   --> GET /api/v1/restaurants [customer-app]
 *   <-- 200 OK GET /api/v1/restaurants (45ms)
 *
 * This is a GlobalFilter — it runs on EVERY request automatically.
 * You don't need to add it to individual routes.
 *
 * implements Ordered: priority 1 means it runs first, before
 * other filters in the chain.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        // Log the incoming request
        log.info("--> {} {} [{}]",
            request.getMethod(),
            request.getURI().getPath(),
            request.getRemoteAddress()
        );

        // Continue the filter chain, then log the response when it completes
        return chain.filter(exchange)
            .doFinally(signalType -> {
                long duration = System.currentTimeMillis() - startTime;
                log.info("<-- {} {} {} ({}ms)",
                    exchange.getResponse().getStatusCode(),
                    request.getMethod(),
                    request.getURI().getPath(),
                    duration
                );
            });
    }

    @Override
    public int getOrder() {
        // Run this filter first (lowest number = highest priority)
        return 1;
    }
}
