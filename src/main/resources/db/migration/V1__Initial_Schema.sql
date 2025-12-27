-- V1__Universal_Schema.sql
-- Database: PostgreSQL (TimescaleDB)

-- 1. SESSIONS (Metadata)
-- Stores one row per application run.
CREATE TABLE sessions (
    session_id TEXT PRIMARY KEY,
    app_name TEXT NOT NULL,
    hostname TEXT,
    pid INTEGER,
    start_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMPTZ
);

-- 2. DEVICES (Hardware Inventory)
-- Stores static hardware info.
-- PK is (session_id, uuid) because a user might upgrade drivers/firmware 
-- between sessions, changing the static properties.
CREATE TABLE devices (
    session_id TEXT NOT NULL REFERENCES sessions(session_id),
    uuid TEXT NOT NULL,          -- "GPU-e885..."
    
    vendor TEXT NOT NULL,        -- "NVIDIA", "AMD"
    name TEXT NOT NULL,          -- "RTX 4090"
    memory_total_mib BIGINT,
    
    -- VENDOR SPECIFICS (JSONB)
    -- NVIDIA: { "compute_cap": "8.9", "l2_cache": 65536, "regs_per_block": 65536, "warp_size": 32 }
    -- AMD:    { "gcn_arch": "gfx90a", "simd_per_cu": 4 }
    static_properties JSONB,

    PRIMARY KEY (session_id, uuid)
);

-- 3. SCOPE EVENTS (Logical Application Ranges)
-- Captures "Init", "Scope Begin/End", "System Start/Stop"
CREATE TABLE scope_events (
    id BIGSERIAL,
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    ts_ns BIGINT NOT NULL,     -- High precision timestamp
    
    session_id TEXT NOT NULL REFERENCES sessions(session_id),
    type TEXT NOT NULL,        -- "SCOPE_BEGIN", "SCOPE_END", "MARKER"
    name TEXT,                 -- Scope name (e.g., "TrainingStep")
    tag TEXT,                  -- Optional user tag
    
    -- Host Metrics (Denormalized here as they are usually tied to scope context)
    host_cpu_pct DOUBLE PRECISION,
    host_ram_used_mib BIGINT,
    
    PRIMARY KEY (id, time)
);
-- Convert to TimescaleDB hypertable
-- SELECT create_hypertable('scope_events', 'time');


-- 4. KERNEL EVENTS (The "Lightning Strikes")
-- High-frequency execution traces
CREATE TABLE kernel_events (
    id BIGSERIAL,
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    start_ns BIGINT NOT NULL,
    end_ns BIGINT,
    duration_ns BIGINT,
    
    session_id TEXT NOT NULL,
    device_uuid TEXT NOT NULL,
    
    name TEXT NOT NULL,        -- Kernel Name
    corr_id BIGINT,            -- Correlation ID to match start/end
    cuda_error TEXT,
    
    -- Explicit params
    grid TEXT,
    block TEXT,
    dyn_shared_bytes BIGINT,
    num_regs INTEGER,
    static_shared_bytes BIGINT,
    local_bytes BIGINT,
    const_bytes BIGINT,
    occupancy DOUBLE PRECISION,
    max_active_blocks BIGINT,
    
    PRIMARY KEY (id, time),
    
    -- Optional: Foreign key constraint (can disable for write performance)
    CONSTRAINT fk_kernel_device FOREIGN KEY (session_id, device_uuid) 
        REFERENCES devices (session_id, uuid)
);

-- Index for fast correlation of kernel start/end
CREATE INDEX idx_kernel_correlation ON kernel_events (session_id, corr_id);
-- Convert to TimescaleDB hypertable
-- SELECT create_hypertable('kernel_events', 'time');


-- 5. SYSTEM METRICS (The "Weather")
-- Low-frequency environmental sampling (Power, Temp, Util)
CREATE TABLE system_metrics (
    id BIGSERIAL,
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    ts_ns BIGINT NOT NULL,
    
    session_id TEXT NOT NULL,
    device_uuid TEXT NOT NULL,
    
    -- Universal Metrics (First-class columns)
    power_watts DOUBLE PRECISION,   -- Normalized (NVIDIA mW -> W, AMD uW -> W)
    temp_c INTEGER,
    util_gpu_pct INTEGER,
    util_mem_pct INTEGER,
    mem_used_mib BIGINT,
    
    -- VENDOR SPECIFICS (JSONB)
    -- NVIDIA: { "throttle": ["THERM", "PWR"], "fan": 45, "clock_sm": 1800, "pcie_rx": 1000 }
    -- AMD:    { "sclk": 1800, "mclk": 1000, "vddgfx": 900 }
    extended_metrics JSONB,
    
    PRIMARY KEY (id, time),
    
    CONSTRAINT fk_metric_device FOREIGN KEY (session_id, device_uuid) 
        REFERENCES devices (session_id, uuid)
);
-- Convert to TimescaleDB hypertable
-- SELECT create_hypertable('system_metrics', 'time');


-- 6. INDICES (Optimized for Dashboard Queries)

-- Dashboard: "Show me all sessions for App X"
CREATE INDEX idx_sessions_app ON sessions (app_name, start_time DESC);

-- Timeline: "Show me kernels for Session S between T1 and T2"
CREATE INDEX idx_kernel_timeline ON kernel_events (session_id, time DESC);

-- Heatmap: "Show me kernels named 'gemm' to see if they got faster"
CREATE INDEX idx_kernel_name ON kernel_events (name, time DESC);

-- Charts: "Show me Power/Temp for Session S and Device D"
CREATE INDEX idx_metrics_device ON system_metrics (session_id, device_uuid, time DESC);
