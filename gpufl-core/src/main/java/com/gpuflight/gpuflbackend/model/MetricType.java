package com.gpuflight.gpuflbackend.model;

public enum MetricType {
    init,
    kernel_event,
    scope_begin,
    scope_end,
    shutdown,
    system_start,
    system_stop,
    system_sample,
    profile_sample,
    memcpy_event,
    memset_event,
    perf_metric_event
}
