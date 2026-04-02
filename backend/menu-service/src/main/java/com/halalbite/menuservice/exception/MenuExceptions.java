package com.halalbite.menuservice.exception;

public class MenuExceptions {

    public static class CategoryNotFoundException extends RuntimeException {
        public CategoryNotFoundException(String msg) { super(msg); }
    }

    public static class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(String msg) { super(msg); }
    }

    public static class UnauthorizedMenuAccessException extends RuntimeException {
        public UnauthorizedMenuAccessException(String msg) { super(msg); }
    }

    public static class DuplicateCategoryException extends RuntimeException {
        public DuplicateCategoryException(String msg) { super(msg); }
    }
}

