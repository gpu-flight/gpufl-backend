package com.gpuflight.gpuflbackend.exception;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleEventProcessingException() {
        EventProcessingException exception = new EventProcessingException(
                "Database constraint violation",
                "init",
                "test-session-123"
        );

        ResponseEntity<Map<String, Object>> response = handler.handleEventProcessingException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Event processing failed", response.getBody().get("error"));
        assertEquals("Database constraint violation", response.getBody().get("message"));
        assertEquals("init", response.getBody().get("eventType"));
        assertEquals("test-session-123", response.getBody().get("sessionId"));
    }

    @Test
    void testHandleEventProcessingExceptionWithCause() {
        Exception cause = new RuntimeException("Root cause");
        EventProcessingException exception = new EventProcessingException(
                "Failed to process event",
                cause,
                "kernel_start",
                "session-456"
        );

        ResponseEntity<Map<String, Object>> response = handler.handleEventProcessingException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Failed to process event", response.getBody().get("message"));
        assertEquals("kernel_start", response.getBody().get("eventType"));
    }

    @Test
    void testHandleJsonProcessingException() {
        JsonProcessingException exception = new JsonProcessingException("Invalid JSON syntax") {};

        ResponseEntity<Map<String, Object>> response = handler.handleJsonProcessingException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid JSON format", response.getBody().get("error"));
        assertEquals("Invalid JSON syntax", response.getBody().get("message"));
    }

    @Test
    void testHandleDataAccessException() {
        DataAccessResourceFailureException exception = new DataAccessResourceFailureException(
                "Connection pool exhausted"
        );

        ResponseEntity<Map<String, Object>> response = handler.handleDataAccessException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Database operation failed", response.getBody().get("error"));
        assertEquals("An error occurred while accessing the database", response.getBody().get("message"));
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid event type: unknown_type");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().get("error"));
        assertEquals("Invalid event type: unknown_type", response.getBody().get("message"));
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new NullPointerException("Unexpected null value");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().get("error"));
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }
}
