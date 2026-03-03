package com.gpuflight.gpuflbackend.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KernelEventValidatorTest {

    private KernelEventValidator validator;

    @BeforeEach
    void setUp() {
        validator = new KernelEventValidator();
    }

    @Test
    void validate_emptyExtraParams_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validate("cuda", Collections.emptyMap()));
    }

    @Test
    void validate_validCudaFields_doesNotThrow() {
        Map<String, Object> params = new HashMap<>();
        params.put("grid", "(4,1,1)");
        params.put("block", "(256,1,1)");
        params.put("num_regs", 16);

        assertDoesNotThrow(() -> validator.validate("cuda", params));
    }

    @Test
    void validate_invalidField_throwsIllegalArgumentException() {
        Map<String, Object> params = new HashMap<>();
        params.put("unknown_field", "value");

        assertThrows(IllegalArgumentException.class, () -> validator.validate("cuda", params));
    }

    @Test
    void validate_mixedFields_throwsForInvalidOnes() {
        Map<String, Object> params = new HashMap<>();
        params.put("grid", "(1,1,1)");
        params.put("hacked_field", "bad");

        assertThrows(IllegalArgumentException.class, () -> validator.validate("cuda", params));
    }

    @Test
    void validate_nullPlatform_appliesCudaValidationByDefault() {
        Map<String, Object> params = new HashMap<>();
        params.put("bad_field", "value");

        assertThrows(IllegalArgumentException.class, () -> validator.validate(null, params));
    }

    @Test
    void validate_nullPlatform_validFields_doesNotThrow() {
        Map<String, Object> params = new HashMap<>();
        params.put("occupancy", 0.75);

        assertDoesNotThrow(() -> validator.validate(null, params));
    }
}
