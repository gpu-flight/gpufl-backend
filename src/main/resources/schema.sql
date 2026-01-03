-- Database: PostgreSQL (TimescaleDB)
-- NOTE: The "gpuflight" database must be created manually or via docker-compose before running the application.
-- Flyway migrations run within the database specified in the connection URL.

-- Ensure TimescaleDB extension is enabled
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;
-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE sessions (
    id UUID DEFAULT gen_random_uuid(),
    session_id VARCHAR PRIMARY KEY,
    app_name VARCHAR NOT NULL,
    hostname VARCHAR,
    ip_addr VARCHAR,
    start_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE host_metrics (
    id UUID DEFAULT gen_random_uuid(),
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    ts_ns BIGINT NOT NULL,
    event_type VARCHAR NOT NULL,
    session_id VARCHAR NOT NULL,
    hostname VARCHAR,
    ip_addr VARCHAR,
    cpu_pct DOUBLE PRECISION,
    ram_used_mib BIGINT,
    ram_total_mib BIGINT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);

SELECT create_hypertable('host_metrics', 'time', if_not_exists => TRUE);

CREATE TABLE cuda_static_devices (
    id UUID DEFAULT gen_random_uuid(),
    session_id VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    uuid VARCHAR NOT NULL,
    device_id INTEGER,
    compute_major VARCHAR,
    compute_minor VARCHAR,
    l2_cache_size_bytes BIGINT,
    shared_mem_per_block_bytes BIGINT,
    regs_per_block INTEGER,
    multi_processor_count INTEGER,
    warp_size INTEGER,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, uuid)
);

CREATE TABLE scope_events (
    id UUID DEFAULT gen_random_uuid(),
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    start_ns BIGINT NOT NULL,     -- High precision timestamp
    end_ns BIGINT,     -- High precision timestamp

    session_id VARCHAR NOT NULL,
    name VARCHAR,                 -- Scope name (e.g., "TrainingStep")
    tag VARCHAR,                  -- Optional user tag

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
-- Convert to TimescaleDB hypertable
SELECT create_hypertable('scope_events', 'time', if_not_exists => TRUE);


-- 4. KERNEL EVENTS (The "Lightning Strikes")
-- High-frequency execution traces
CREATE TABLE kernel_events (
    id UUID DEFAULT gen_random_uuid(),
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    start_ns BIGINT NOT NULL,
    end_ns BIGINT,
    duration_ns BIGINT,

    session_id VARCHAR NOT NULL,
    device_id INTEGER NOT NULL,
    pid INTEGER,
    app VARCHAR,
    platform VARCHAR,

    grid VARCHAR,
    block VARCHAR,
    dyn_shared_bytes INTEGER,
    num_regs INTEGER,
    static_shared_bytes BIGINT,
    local_bytes BIGINT,
    const_bytes BIGINT,
    occupancy DOUBLE PRECISION,
    max_active_blocks INTEGER,

    name VARCHAR NOT NULL,        -- Kernel Name
    corr_id BIGINT,            -- Correlation ID to match start/end
    cuda_error VARCHAR,
    has_details BOOLEAN,
    extra_params JSONB,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);

-- Index for fast correlation of kernel start/end
CREATE INDEX idx_kernel_correlation ON kernel_events (session_id, corr_id);
-- Convert to TimescaleDB hypertable
SELECT create_hypertable('kernel_events', 'time', if_not_exists => TRUE);


-- 5. DEVICE METRICS (Dynamic Device Info)
CREATE TABLE device_metrics (
    id UUID DEFAULT gen_random_uuid(),
    time TIMESTAMPTZ NOT NULL, -- Hypertable key
    ts_ns BIGINT NOT NULL,
    event_type VARCHAR NOT NULL,

    session_id VARCHAR NOT NULL,
    uuid VARCHAR NOT NULL,
    device_id INTEGER,
    vendor VARCHAR,
    name VARCHAR,
    pci_bus INTEGER,

    used_mib BIGINT,
    free_mib BIGINT,
    total_mib BIGINT,
    util_gpu INTEGER,
    util_mem INTEGER,
    temp_c INTEGER,
    power_mw INTEGER,
    clk_gfx INTEGER,
    clk_sm INTEGER,
    clk_mem INTEGER,
    throttle_pwr INTEGER,
    throttle_therm INTEGER,
    pcie_rx_bw BIGINT,
    pcie_tx_bw BIGINT,

    extended_metrics TEXT,

    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
SELECT create_hypertable('device_metrics', 'time', if_not_exists => TRUE);


CREATE TABLE initial_events (
    id UUID DEFAULT gen_random_uuid(),
    session_id VARCHAR,
    pid INTEGER,
    app VARCHAR,
    log_path VARCHAR,
    system_rate_ms INTEGER,
    ts_ns BIGINT NOT NULL,
    shutdown_ts_ns BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, session_id, ts_ns)
);
CREATE UNIQUE INDEX uq_session_ts ON initial_events (session_id, ts_ns);

CREATE TABLE system_events (
    id UUID DEFAULT gen_random_uuid(),
    session_id VARCHAR,
    pid INTEGER,
    app VARCHAR,
    name VARCHAR,
    event_type VARCHAR,
    ts_ns BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, session_id, ts_ns)
);

-- Dashboard: "Show me all sessions for App X"
CREATE INDEX idx_sessions_app ON sessions (app_name, start_time DESC);

-- Timeline: "Show me kernels for Session S between T1 and T2"
CREATE INDEX idx_kernel_timeline ON kernel_events (session_id, time DESC);

-- Heatmap: "Show me kernels named 'gemm' to see if they got faster"
CREATE INDEX idx_kernel_name ON kernel_events (name, time DESC);

-- Charts: "Show me Power/Temp for Session S and Device D"
CREATE INDEX idx_device_metrics_device ON device_metrics (session_id, uuid, time DESC);
CREATE INDEX idx_host_metrics_session ON host_metrics (session_id, time DESC);
