package com.eve.booking.exception;

public class TestNotAvailableException extends RuntimeException {
    public TestNotAvailableException(Long id) { super("Test not available: " + id); }
}
