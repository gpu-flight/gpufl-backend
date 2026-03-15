package com.gpuflight.gpuflbackend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires a live PostgreSQL/TimescaleDB connection — run as an integration test")
class GpuflBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
