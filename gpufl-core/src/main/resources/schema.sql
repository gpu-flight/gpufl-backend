-- Database: PostgreSQL (TimescaleDB)
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;
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
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    retention_override_days INTEGER
);

CREATE TABLE session_dictionaries (
    session_id  VARCHAR      NOT NULL,
    dict_type   VARCHAR(20)  NOT NULL,
    dict_id     INTEGER      NOT NULL,
    name        TEXT         NOT NULL,
    PRIMARY KEY (session_id, dict_type, dict_id)
);
CREATE INDEX idx_dict_session ON session_dictionaries (session_id, dict_type);

CREATE TABLE host_metrics (
    id            UUID DEFAULT gen_random_uuid(),
    time          TIMESTAMPTZ      NOT NULL,
    session_id    VARCHAR          NOT NULL,
    ts_ns         BIGINT           NOT NULL,
    cpu_pct       DOUBLE PRECISION,
    ram_used_mib  BIGINT,
    ram_total_mib BIGINT,
    created_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
SELECT create_hypertable('host_metrics', 'time', if_not_exists => TRUE);
CREATE INDEX idx_host_metrics_session ON host_metrics (session_id, time DESC);

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
    id                UUID DEFAULT gen_random_uuid(),
    time              TIMESTAMPTZ NOT NULL,
    start_ns          BIGINT NOT NULL,
    end_ns            BIGINT,
    session_id        VARCHAR NOT NULL,
    name              VARCHAR,
    tag               VARCHAR,
    user_scope        VARCHAR,
    scope_depth       INTEGER,
    scope_instance_id BIGINT,
    created_at        TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
SELECT create_hypertable('scope_events', 'time', if_not_exists => TRUE);
CREATE INDEX idx_scope_instance ON scope_events (session_id, scope_instance_id);
CREATE INDEX idx_scope_name ON scope_events (session_id, name);

CREATE TABLE kernel_events (
    id                      UUID DEFAULT gen_random_uuid(),
    time                    TIMESTAMPTZ NOT NULL,
    start_ns                BIGINT NOT NULL,
    end_ns                  BIGINT,
    duration_ns             BIGINT,
    session_id              VARCHAR NOT NULL,
    device_id               INTEGER NOT NULL,
    pid                     INTEGER,
    app                     VARCHAR,
    platform                VARCHAR,
    grid                    VARCHAR,
    block                   VARCHAR,
    dyn_shared_bytes        INTEGER,
    num_regs                INTEGER,
    static_shared_bytes     BIGINT,
    local_bytes             BIGINT,
    const_bytes             BIGINT,
    occupancy               DOUBLE PRECISION,
    max_active_blocks       INTEGER,
    name                    VARCHAR NOT NULL,
    corr_id                 BIGINT,
    has_details             BOOLEAN,
    user_scope              VARCHAR,
    scope_depth             INTEGER,
    stack_trace             VARCHAR,
    stream_id               BIGINT,
    api_start_ns            BIGINT,
    api_exit_ns             BIGINT,
    reg_occupancy           DOUBLE PRECISION,
    smem_occupancy          DOUBLE PRECISION,
    warp_occupancy          DOUBLE PRECISION,
    block_occupancy         DOUBLE PRECISION,
    limiting_resource       VARCHAR,
    local_mem_total_bytes   BIGINT,
    local_mem_per_thread_bytes BIGINT,
    cache_config_requested  INTEGER,
    cache_config_executed   INTEGER,
    shared_mem_executed_bytes BIGINT,
    created_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
SELECT create_hypertable('kernel_events', 'time', if_not_exists => TRUE);
CREATE INDEX idx_kernel_correlation ON kernel_events (session_id, corr_id);
CREATE INDEX idx_kernel_timeline ON kernel_events (session_id, time DESC);
CREATE INDEX idx_kernel_name ON kernel_events (name, time DESC);

CREATE TABLE memcpy_events (
    id          UUID DEFAULT gen_random_uuid(),
    time        TIMESTAMPTZ NOT NULL,
    session_id  VARCHAR     NOT NULL,
    start_ns    BIGINT      NOT NULL,
    duration_ns BIGINT      NOT NULL,
    stream_id   BIGINT      NOT NULL,
    bytes       BIGINT      NOT NULL,
    copy_kind   INTEGER     NOT NULL,
    corr_id     BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
SELECT create_hypertable('memcpy_events', 'time', if_not_exists => TRUE);
CREATE INDEX idx_memcpy_session ON memcpy_events (session_id, time DESC);

CREATE TABLE device_metrics (
    id         UUID DEFAULT gen_random_uuid(),
    time       TIMESTAMPTZ NOT NULL,
    session_id VARCHAR     NOT NULL,
    ts_ns      BIGINT      NOT NULL,
    device_id  INTEGER     NOT NULL,
    gpu_util   INTEGER,
    mem_util   INTEGER,
    temp_c     INTEGER,
    power_mw   INTEGER,
    used_mib   BIGINT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, time)
);
SELECT create_hypertable('device_metrics', 'time', if_not_exists => TRUE);
CREATE INDEX idx_device_metrics_device ON device_metrics (session_id, device_id, time DESC);

CREATE TABLE initial_events (
    session_id    VARCHAR,
    pid           INTEGER,
    app           VARCHAR,
    log_path      VARCHAR,
    system_rate_ms INTEGER,
    time          TIMESTAMPTZ NOT NULL,
    ts_ns         BIGINT NOT NULL,
    shutdown_ts_ns BIGINT,
    created_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, ts_ns)
);

CREATE TABLE system_events (
    id         UUID DEFAULT gen_random_uuid(),
    session_id VARCHAR,
    pid        INTEGER,
    app        VARCHAR,
    name       VARCHAR,
    event_type VARCHAR,
    ts_ns      BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id, session_id, ts_ns)
);

CREATE TABLE profile_samples (
    id               UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id       VARCHAR NOT NULL,
    scope_name       VARCHAR(255),
    device_id        INTEGER,
    sample_kind      VARCHAR(20)  NOT NULL,
    function_name    VARCHAR(512),
    pc_offset        INTEGER,
    metric_name      VARCHAR(255),
    metric_value     BIGINT  NOT NULL DEFAULT 0,
    stall_reason     INTEGER,
    occurrence_count INTEGER NOT NULL DEFAULT 1,
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    updated_at       TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_profile_sample_key
        UNIQUE (session_id, scope_name, function_name, pc_offset, stall_reason, sample_kind, metric_name)
);
CREATE INDEX idx_profile_samples_session ON profile_samples (session_id);
CREATE INDEX idx_profile_samples_scope   ON profile_samples (session_id, scope_name);

CREATE TABLE IF NOT EXISTS users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE NOT NULL,
    username      VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    display_name  VARCHAR(255),
    bio           TEXT,
    avatar_url    VARCHAR(500),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS api_keys (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    key_hash     VARCHAR(64)  NOT NULL UNIQUE,
    key_prefix   VARCHAR(8)   NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ
);

CREATE INDEX idx_sessions_app ON sessions (app_name, start_time DESC);
