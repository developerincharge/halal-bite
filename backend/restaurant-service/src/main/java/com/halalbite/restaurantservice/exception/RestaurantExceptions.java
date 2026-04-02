package com.halalbite.restaurantservice.exception;

public class RestaurantExceptions {

    public static class RestaurantNotFoundException extends RuntimeException {
        public RestaurantNotFoundException(String message) { super(message); }
    }

    public static class RestaurantAlreadyExistsException extends RuntimeException {
        public RestaurantAlreadyExistsException(String message) { super(message); }
    }

    public static class UnauthorizedRestaurantAccessException extends RuntimeException {
        public UnauthorizedRestaurantAccessException(String message) { super(message); }
    }

    public static class InvalidStatusTransitionException extends RuntimeException {
        public InvalidStatusTransitionException(String message) { super(message); }
    }
}
