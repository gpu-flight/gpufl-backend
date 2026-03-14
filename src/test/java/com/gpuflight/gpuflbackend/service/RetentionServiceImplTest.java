package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.config.RetentionProperties;
import com.gpuflight.gpuflbackend.dao.RetentionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetentionServiceImplTest {

    @Mock private RetentionDao retentionDao;

    private RetentionProperties props;
    private RetentionServiceImpl service;

    @BeforeEach
    void setUp() {
        props = new RetentionProperties();
        service = new RetentionServiceImpl(retentionDao, props);
    }

    @Test
    void runCleanup_withExpiredSessions_deletesAndReturnsCount() {
        List<String> expired = List.of("session-1", "session-2");
        when(retentionDao.findExpiredSessionIds(props.getDefaultDays())).thenReturn(expired);

        int result = service.runCleanup();

        assertEquals(2, result);
        verify(retentionDao).deleteBySessionIds(expired);
    }

    @Test
    void runCleanup_noExpiredSessions_returnsZeroWithoutDelete() {
        when(retentionDao.findExpiredSessionIds(props.getDefaultDays())).thenReturn(Collections.emptyList());

        int result = service.runCleanup();

        assertEquals(0, result);
        verify(retentionDao, never()).deleteBySessionIds(any());
    }

    @Test
    void scheduledCleanup_whenEnabled_callsRunCleanup() {
        props.setEnabled(true);
        when(retentionDao.findExpiredSessionIds(props.getDefaultDays())).thenReturn(List.of("session-1"));

        service.scheduledCleanup();

        verify(retentionDao).findExpiredSessionIds(props.getDefaultDays());
    }

    @Test
    void scheduledCleanup_whenDisabled_skipsCleanup() {
        props.setEnabled(false);

        service.scheduledCleanup();

        verifyNoInteractions(retentionDao);
    }
}
