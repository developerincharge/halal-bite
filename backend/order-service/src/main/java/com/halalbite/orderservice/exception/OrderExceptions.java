package com.halalbite.orderservice.exception;

public class OrderExceptions {

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String msg) { super(msg); }
    }

    public static class InvalidOrderStatusException extends RuntimeException {
        public InvalidOrderStatusException(String msg) { super(msg); }
    }

    public static class MenuItemUnavailableException extends RuntimeException {
        public MenuItemUnavailableException(String msg) { super(msg); }
    }

    public static class OrderNotCancellableException extends RuntimeException {
        public OrderNotCancellableException(String msg) { super(msg); }
    }
}