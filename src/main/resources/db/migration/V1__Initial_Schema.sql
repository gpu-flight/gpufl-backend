-- V1__Universal_Schema.sql
-- Database: PostgreSQL (TimescaleDB)
-- NOTE: The "gpuflight" database must be created manually or via docker-compose before running the application.
-- Flyway migrations run within the database specified in the connection URL.

-- Ensure TimescaleDB extension is enabled
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;

-- 1. SESSIONS (Metadata)
-- Stores one row per application run.
CREATE TABLE sessions (
    session_id VARCHAR PRIMARY KEY,
    app_name VARCHAR NOT NULL,
    hostname VARCHAR,
    pid INTEGER,
    start_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 2. DEVICES (Hardware Inventory)
-- Stores static hardware info.
-- PK is (session_id, uuid) because a user might upgrade drivers/firmware 
-- between sessions, changing the static properties.
CREATE TABLE devices (
    session_id VARCHAR NOT NULL,
    uuid VARCHAR NOT NULL,          -- "GPU-e885..."
    device_id INTEGER NOT NULL, -- "this is the GPU's index in the system"
    
    vendor VARCHAR NOT NULL,        -- "NVIDIA", "AMD"
    name VARCHAR NOT NULL,          -- "RTX 4090"
    memory_total_mib BIGINT,

    properties JSONB,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, uuid)
);

-- 3. SCOPE EVENTS (Logical Application Ranges)
-- Captures "Init", "Scope Begin/End", "System Start/Stop"
CREATE TABLE scope_events (
    id BIGSERIAL,
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    ts_ns BIGINT NOT NULL,     -- High precision timestamp
    
    session_id VARCHAR NOT NULL,
    type VARCHAR NOT NULL,        -- "SCOPE_BEGIN", "SCOPE_END", "MARKER"
    name VARCHAR,                 -- Scope name (e.g., "TrainingStep")
    tag VARCHAR,                  -- Optional user tag
    
    -- Host Metrics (Denormalized here as they are usually tied to scope context)
    host_cpu_pct DOUBLE PRECISION,
    host_ram_used_mib BIGINT,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
-- Convert to TimescaleDB hypertable
SELECT create_hypertable('scope_events', 'time', if_not_exists => TRUE);


-- 4. KERNEL EVENTS (The "Lightning Strikes")
-- High-frequency execution traces
CREATE TABLE kernel_events (
    id BIGSERIAL,
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    start_ns BIGINT NOT NULL,
    end_ns BIGINT,
    duration_ns BIGINT,
    
    session_id VARCHAR NOT NULL,
    device_uuid VARCHAR NOT NULL,
    
    name VARCHAR NOT NULL,        -- Kernel Name
    corr_id BIGINT,            -- Correlation ID to match start/end
    cuda_error VARCHAR,
    has_details BOOLEAN,
    extra_params JSONB,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time),
    
    -- Optional: Foreign key constraint (can disable for write performance)
    CONSTRAINT fk_kernel_device FOREIGN KEY (session_id, device_uuid) 
        REFERENCES devices (session_id, uuid)
);

-- Index for fast correlation of kernel start/end
CREATE INDEX idx_kernel_correlation ON kernel_events (session_id, corr_id);
-- Convert to TimescaleDB hypertable
SELECT create_hypertable('kernel_events', 'time', if_not_exists => TRUE);


-- 5. SYSTEM METRICS (The "Weather")
-- Low-frequency environmental sampling (Power, Temp, Util)
CREATE TABLE system_metrics (
    id BIGSERIAL,
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    ts_ns BIGINT NOT NULL,
    
    session_id VARCHAR NOT NULL,
    device_uuid VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    
    -- Universal Metrics (First-class columns)
    power_watts DOUBLE PRECISION,   -- Normalized (NVIDIA mW -> W, AMD uW -> W)
    temp_c INTEGER,
    util_gpu_pct INTEGER,
    util_mem_pct INTEGER,
    mem_used_mib BIGINT,

    -- Host Metrics
    host_cpu_pct DOUBLE PRECISION,
    host_ram_used_mib BIGINT,
    
    -- VENDOR SPECIFICS (JSONB)
    -- NVIDIA: { "throttle": ["THERM", "PWR"], "fan": 45, "clock_sm": 1800, "pcie_rx": 1000 }
    -- AMD:    { "sclk": 1800, "mclk": 1000, "vddgfx": 900 }
    extended_metrics JSONB,
    
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time),
    
    CONSTRAINT fk_metric_device FOREIGN KEY (session_id, device_uuid) 
        REFERENCES devices (session_id, uuid)
);
-- Convert to TimescaleDB hypertable
SELECT create_hypertable('system_metrics', 'time', if_not_exists => TRUE);


-- 6. INITIAL EVENTS (The "Birth Certificate")
-- Stores the original init event JSON per session.
-- Guaranteed unique per session_id.
CREATE TABLE initial_events (
    session_id VARCHAR PRIMARY KEY,
    time TIMESTAMPTZ NOT NULL,
    ts_ns BIGINT NOT NULL,
    event_json JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 7. INDICES (Optimized for Dashboard Queries)

-- Dashboard: "Show me all sessions for App X"
CREATE INDEX idx_sessions_app ON sessions (app_name, start_time DESC);

-- Timeline: "Show me kernels for Session S between T1 and T2"
CREATE INDEX idx_kernel_timeline ON kernel_events (session_id, time DESC);

-- Heatmap: "Show me kernels named 'gemm' to see if they got faster"
CREATE INDEX idx_kernel_name ON kernel_events (name, time DESC);

-- Charts: "Show me Power/Temp for Session S and Device D"
CREATE INDEX idx_metrics_device ON system_metrics (session_id, device_uuid, time DESC);
