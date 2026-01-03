package com.gpuflight.gpuflbackend.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.gpuflight.gpuflbackend.model.input.KernelBeginEvent;
import com.gpuflight.gpuflbackend.model.input.KernelEndEvent;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = InitEvent.class, name = "init"),
    @JsonSubTypes.Type(value = KernelBeginEvent.class, name = "kernel_start"),
    @JsonSubTypes.Type(value = KernelEndEvent.class, name = "kernel_end"),
    @JsonSubTypes.Type(value = ScopeBeginEvent.class, name = "scope_begin"),
    @JsonSubTypes.Type(value = ScopeEndEvent.class, name = "scope_end"),
    @JsonSubTypes.Type(value = ShutdownEvent.class, name = "shutdown"),
    @JsonSubTypes.Type(value = SystemSampleEvent.class, name = "system_sample"),
    @JsonSubTypes.Type(value = SystemSampleEvent.class, name = "system_start"),
    @JsonSubTypes.Type(value = SystemSampleEvent.class, name = "system_stop")
})
public interface MetricEvent {
    MetricType type();
    int pid();
    String app();
    long tsNs();
}
