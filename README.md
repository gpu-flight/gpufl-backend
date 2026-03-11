# gpufl-backend

Spring Boot backend for GPU Flight — collects, stores, and serves GPU execution events and metrics produced by the `gpufl-client` library.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.1 |
| Database | PostgreSQL 16 + TimescaleDB |
| Data Access | Spring Data JDBC |
| Build | Gradle 9.3.1 |
| Serialization | Jackson (SNAKE_CASE for ingestion) |

## Prerequisites

- Java 25+
- Docker (for TimescaleDB)

## Getting Started

### 1. Start the database

```bash
docker-compose up -d
```

This starts a TimescaleDB container on `localhost:5432` with database `gpuflight` (user/password: `postgres`/`postgres`). Flyway applies `schema.sql` automatically on first boot.

### 2. Run the application

```bash
./gradlew bootRun
```

The server starts on port **8080**.

### 3. Run tests

```bash
./gradlew test
```

JaCoCo enforces a minimum **80% coverage** threshold. Generate the HTML report with:

```bash
./gradlew jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

## API

### Ingest an event

```
POST /api/v1/events/{eventType}
```

The body is a JSON `EventWrapper`:

```json
{
  "data": "<event JSON string>",
  "agentSendingTime": 1773115942016297887,
  "hostname": "my-host",
  "ipAddr": "192.168.1.1"
}
```

Supported `eventType` values:

| Type | Description |
|---|---|
| `init` | Session start — device hardware info, host metrics |
| `shutdown` | Session end |
| `kernel_event` | GPU kernel execution trace |
| `scope_begin` | Start of a named user scope |
| `scope_end` | End of a named user scope |
| `system_start` | System monitoring start |
| `system_stop` | System monitoring stop |
| `system_sample` | Periodic host + device metric snapshot |
| `memcpy_event` | GPU memory copy |
| `memset_event` | GPU memory set |
| `profile_sample` | PC sampling or SASS metric sample |
| `perf_metric_event` | Hardware performance counters (SM throughput, cache hit rates, DRAM BW) |

### Query events

```
GET /api/v1/events/init?dateFrom=<ISO-8601>&dateTo=<ISO-8601>
```

Returns a list of sessions with nested kernel, scope, and host metric summaries for the given time range (defaults to the last 24 hours).

```
GET /api/v1/events/system?sessionId=<id>&dateFrom=<ISO-8601>&dateTo=<ISO-8601>
```

Returns system events for a specific session.

## Database Schema

All time-series tables are TimescaleDB **hypertables** partitioned by `time` for efficient range queries on large datasets.

### Tables

| Table | Type | Description |
|---|---|---|
| `sessions` | Regular | One row per session — app name, hostname, IP, start/end time |
| `initial_events` | Regular | Init event metadata — pid, log path, system poll rate |
| `host_metrics` | Hypertable | CPU % and RAM snapshots over time |
| `cuda_static_devices` | Regular | GPU hardware properties — compute capability, L2 cache, shared memory, SM count |
| `kernel_events` | Hypertable | Kernel execution traces — timing, occupancy, register/memory usage, CUPTI fields |
| `scope_events` | Hypertable | Named user scope intervals (begin/end pairs) |
| `device_metrics` | Hypertable | GPU dynamic metrics — utilization %, clocks, temperature, power, PCIe bandwidth |
| `system_events` | Regular | System lifecycle markers (start/stop/sample) |
| `mem_events` | Hypertable | Memory copy and memset operations |
| `profile_samples` | Hypertable | PC sampling stall reasons and SASS instruction metrics |
| `perf_metric_events` | Hypertable | Hardware counter snapshots (SM throughput, L1/L2 hit rates, DRAM BW, tensor activity) |

### Field naming conventions

Units are encoded in column names to avoid ambiguity:

| Suffix | Unit |
|---|---|
| `_ns` | nanoseconds |
| `_ms` | milliseconds |
| `_mib` | mebibytes |
| `_bytes` | bytes |
| `_mhz` | megahertz |
| `_mw` | milliwatts |
| `_pct` | percent (0–100) |
| `_bps` | bytes per second |

## Project Structure

```
src/main/java/com/gpuflight/gpuflbackend/
├── config/          # Jackson ObjectMapper beans, constants
├── controller/      # REST endpoints (ingestion + retrieval)
├── service/         # Business logic per event type
├── dao/             # SQL queries (Spring JDBC)
├── entity/          # DB row objects (Lombok @Builder)
├── model/
│   ├── input/       # Incoming event records (deserialized from client JSON)
│   └── presentation/# Response DTOs
├── mapper/          # Entity ↔ DTO conversion
├── exception/       # Global exception handler
├── validator/       # Input validation
└── util/            # TimeUtils (nanosecond ↔ Instant)
```

## Data Flow

**Ingestion:**
```
POST /api/v1/events/{type}
  → EventIngestionController
  → EventProcessingService (routes by MetricType)
  → *ServiceImpl (deserializes EventWrapper.data via SNAKE_CASE ObjectMapper)
  → *DaoImpl (raw SQL INSERT into hypertable)
```

**Retrieval:**
```
GET /api/v1/events/init
  → EventController
  → InitEventService.getInitEvents()
  → DAO queries + entity assembly
  → Mapper → InitEventDto (with nested kernels, scopes, host metrics)
```

## Configuration

Key properties (see `src/main/resources/application.properties` and `application-dev.properties`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gpuflight
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.flyway.enabled=true
```

The `dev` profile enables `TRACE`-level logging for DAO, service, and controller layers.
