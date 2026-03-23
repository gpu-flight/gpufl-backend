package com.gpuflight.gpuflbackend.service;

import com.gpuflight.gpuflbackend.dao.DictionaryDao;
import com.gpuflight.gpuflbackend.model.input.DictionaryUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DictionaryServiceImplTest {

    @Mock private DictionaryDao dictionaryDao;

    private DictionaryServiceImpl service;

    private static final String SESSION = "sess-1";

    @BeforeEach
    void setUp() {
        service = new DictionaryServiceImpl(dictionaryDao);
    }

    // ── mergeDictionary ────────────────────────────────────────────────────────

    @Test
    void mergeDictionary_persistsEachEntryToDao() {
        DictionaryUpdateEvent event = new DictionaryUpdateEvent(
                SESSION,
                Map.of("1", "vectorAdd", "2", "matrixMul"),
                Map.of("1", "main_loop"),
                Map.of(),
                Map.of()
        );

        service.mergeDictionary(SESSION, event);

        verify(dictionaryDao).upsert(SESSION, "kernel",     1, "vectorAdd");
        verify(dictionaryDao).upsert(SESSION, "kernel",     2, "matrixMul");
        verify(dictionaryDao).upsert(SESSION, "scope_name", 1, "main_loop");
        verifyNoMoreInteractions(dictionaryDao);
    }

    @Test
    void mergeDictionary_nullDict_skipsWithoutError() {
        DictionaryUpdateEvent event = new DictionaryUpdateEvent(SESSION, null, null, null, null);

        service.mergeDictionary(SESSION, event);

        verifyNoInteractions(dictionaryDao);
    }

    @Test
    void mergeDictionary_emptyDict_skipsWithoutError() {
        DictionaryUpdateEvent event = new DictionaryUpdateEvent(
                SESSION, Map.of(), Map.of(), Map.of(), Map.of());

        service.mergeDictionary(SESSION, event);

        verifyNoInteractions(dictionaryDao);
    }

    // ── resolveKernel ──────────────────────────────────────────────────────────

    @Test
    void resolveKernel_afterMerge_returnsCachedName() {
        DictionaryUpdateEvent event = new DictionaryUpdateEvent(
                SESSION, Map.of("5", "myKernel"), Map.of(), Map.of(), Map.of());
        service.mergeDictionary(SESSION, event);

        String name = service.resolveKernel(SESSION, 5);

        assertThat(name).isEqualTo("myKernel");
        // DB should NOT be consulted on cache hit
        verify(dictionaryDao, never()).loadDict(any(), any());
    }

    @Test
    void resolveKernel_cacheMiss_loadsFromDbAndReturnsFallback() {
        when(dictionaryDao.loadDict(SESSION, "kernel")).thenReturn(Map.of());

        String name = service.resolveKernel(SESSION, 99);

        assertThat(name).isEqualTo("kernel#99");
        verify(dictionaryDao).loadDict(SESSION, "kernel");
    }

    @Test
    void resolveKernel_cacheMiss_dbHasEntry_returnsDbName() {
        when(dictionaryDao.loadDict(SESSION, "kernel")).thenReturn(Map.of(7, "dbKernel"));

        String name = service.resolveKernel(SESSION, 7);

        assertThat(name).isEqualTo("dbKernel");
    }

    // ── resolveScopeName ───────────────────────────────────────────────────────

    @Test
    void resolveScopeName_zeroId_returnsNull() {
        assertThat(service.resolveScopeName(SESSION, 0)).isNull();
        verifyNoInteractions(dictionaryDao);
    }

    @Test
    void resolveScopeName_knownId_returnsName() {
        DictionaryUpdateEvent event = new DictionaryUpdateEvent(
                SESSION, Map.of(), Map.of("3", "my_scope"), Map.of(), Map.of());
        service.mergeDictionary(SESSION, event);

        assertThat(service.resolveScopeName(SESSION, 3)).isEqualTo("my_scope");
    }

    @Test
    void resolveScopeName_unknownId_returnsFallback() {
        when(dictionaryDao.loadDict(SESSION, "scope_name")).thenReturn(Map.of());

        assertThat(service.resolveScopeName(SESSION, 42)).isEqualTo("scope#42");
    }

    // ── resolveFunction ────────────────────────────────────────────────────────

    @Test
    void resolveFunction_zeroId_returnsNull() {
        assertThat(service.resolveFunction(SESSION, 0)).isNull();
        verifyNoInteractions(dictionaryDao);
    }

    @Test
    void resolveFunction_knownId_returnsName() {
        DictionaryUpdateEvent event = new DictionaryUpdateEvent(
                SESSION, Map.of(), Map.of(), Map.of("2", "myFunc"), Map.of());
        service.mergeDictionary(SESSION, event);

        assertThat(service.resolveFunction(SESSION, 2)).isEqualTo("myFunc");
    }

    @Test
    void resolveFunction_unknownId_returnsFallback() {
        when(dictionaryDao.loadDict(SESSION, "function")).thenReturn(Map.of());

        assertThat(service.resolveFunction(SESSION, 8)).isEqualTo("function#8");
    }

    // ── resolveMetric ──────────────────────────────────────────────────────────

    @Test
    void resolveMetric_zeroId_returnsNull() {
        assertThat(service.resolveMetric(SESSION, 0)).isNull();
        verifyNoInteractions(dictionaryDao);
    }

    @Test
    void resolveMetric_knownId_returnsName() {
        DictionaryUpdateEvent event = new DictionaryUpdateEvent(
                SESSION, Map.of(), Map.of(), Map.of(), Map.of("1", "smsp__sass_inst_executed"));
        service.mergeDictionary(SESSION, event);

        assertThat(service.resolveMetric(SESSION, 1)).isEqualTo("smsp__sass_inst_executed");
    }

    @Test
    void resolveMetric_unknownId_returnsNull() {
        when(dictionaryDao.loadDict(SESSION, "metric")).thenReturn(Map.of());

        // metric fallback is null (unlike kernel/scope/function which use a string fallback)
        assertThat(service.resolveMetric(SESSION, 55)).isNull();
    }

    // ── cache isolation across sessions ───────────────────────────────────────

    @Test
    void resolveKernel_differentSessions_areIndependent() {
        DictionaryUpdateEvent e1 = new DictionaryUpdateEvent(
                "sess-A", Map.of("1", "kernelA"), Map.of(), Map.of(), Map.of());
        DictionaryUpdateEvent e2 = new DictionaryUpdateEvent(
                "sess-B", Map.of("1", "kernelB"), Map.of(), Map.of(), Map.of());

        service.mergeDictionary("sess-A", e1);
        service.mergeDictionary("sess-B", e2);

        assertThat(service.resolveKernel("sess-A", 1)).isEqualTo("kernelA");
        assertThat(service.resolveKernel("sess-B", 1)).isEqualTo("kernelB");
    }
}
