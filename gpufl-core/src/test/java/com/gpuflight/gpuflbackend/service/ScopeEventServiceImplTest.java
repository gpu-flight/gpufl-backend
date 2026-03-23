package com.gpuflight.gpuflbackend.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.gpuflight.gpuflbackend.dao.ScopeEventDao;
import com.gpuflight.gpuflbackend.entity.ScopeEventEntity;
import com.gpuflight.gpuflbackend.model.EventWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScopeEventServiceImplTest {

    @Mock private ScopeEventDao scopeEventDao;

    private ScopeEventServiceImpl service;

    private static final String SCOPE_BEGIN_JSON = """
            {
              "pid": 100, "app": "demo",
              "session_id": "session-1",
              "name": "training-step", "tag": "phase1",
              "ts_ns": 1000000000,
              "user_scope": "global|training-step", "scope_depth": 1
            }
            """;

    private static final String SCOPE_END_JSON = """
            {
              "pid": 100, "app": "demo",
              "session_id": "session-1",
              "name": "training-step",
              "ts_ns": 2000000000,
              "user_scope": "global|training-step", "scope_depth": 1
            }
            """;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        service = new ScopeEventServiceImpl(scopeEventDao, mapper);
    }

    @Test
    void addScopeEventBegin_minimal_savesScopeEvent() {
        EventWrapper wrapper = new EventWrapper(SCOPE_BEGIN_JSON, 0L, "host", "127.0.0.1");

        service.addScopeEventBegin(wrapper);

        verify(scopeEventDao).saveScopeEvent(any(ScopeEventEntity.class));
    }

    @Test
    void addScopeEventBegin_invalidJson_throwsRuntimeException() {
        EventWrapper wrapper = new EventWrapper("invalid", 0L, "host", "127.0.0.1");

        assertThrows(RuntimeException.class, () -> service.addScopeEventBegin(wrapper));
        verifyNoInteractions(scopeEventDao);
    }

    @Test
    void addScopeEventEnd_validJson_updatesScope() {
        EventWrapper wrapper = new EventWrapper(SCOPE_END_JSON, 0L, "host", "127.0.0.1");

        service.addScopeEventEnd(wrapper);

        verify(scopeEventDao).updateScopeEventEnd(any(ScopeEventEntity.class));
    }

    @Test
    void addScopeEventEnd_invalidJson_throwsRuntimeException() {
        EventWrapper wrapper = new EventWrapper("invalid", 0L, "host", "127.0.0.1");

        assertThrows(RuntimeException.class, () -> service.addScopeEventEnd(wrapper));
        verifyNoInteractions(scopeEventDao);
    }
}
