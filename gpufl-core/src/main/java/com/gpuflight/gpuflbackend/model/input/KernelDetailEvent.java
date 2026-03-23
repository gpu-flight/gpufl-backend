package com.gpuflight.gpuflbackend.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KernelDetailEvent(
    @JsonProperty("session_id") String sessionId,
    int pid,
    String app,
    @JsonProperty("corr_id") long corrId,
    String grid,
    String block,
    @JsonProperty("static_shared") int staticShared,
    @JsonProperty("local_bytes") int localBytes,
    @JsonProperty("const_bytes") int constBytes,
    float occupancy,
    @JsonProperty("reg_occupancy") float regOccupancy,
    @JsonProperty("smem_occupancy") float smemOccupancy,
    @JsonProperty("warp_occupancy") float warpOccupancy,
    @JsonProperty("block_occupancy") float blockOccupancy,
    @JsonProperty("limiting_resource") String limitingResource,
    @JsonProperty("max_active_blocks") int maxActiveBlocks,
    @JsonProperty("local_mem_total_bytes") long localMemTotalBytes,
    @JsonProperty("local_mem_per_thread_bytes") long localMemPerThreadBytes,
    @JsonProperty("cache_config_requested") int cacheConfigRequested,
    @JsonProperty("cache_config_executed") int cacheConfigExecuted,
    @JsonProperty("shared_mem_executed_bytes") long sharedMemExecutedBytes,
    @JsonProperty("user_scope") String userScope,
    @JsonProperty("stack_trace") String stackTrace
) {}
