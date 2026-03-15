package com.gpuflight.gpuflbackend.validator;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gpuflight.gpuflbackend.config.Constants.CUDA_PLATFORM;

@Component
public class KernelEventValidator {
    private static final Set<String> ALLOWED_CUDA_FIELDS = Set.of(
            "grid", "block", "dyn_shared_bytes", "num_regs", "static_shared_bytes",
            "local_bytes", "const_bytes", "occupancy", "max_active_blocks"
    );

    public void validate(String platform, Map<String, Object> extraParams) {
        if (CUDA_PLATFORM.equalsIgnoreCase(platform) || platform == null) {
            Set<String> invalidFields = extraParams.keySet().stream()
                    .filter(field -> !ALLOWED_CUDA_FIELDS.contains(field))
                    .collect(Collectors.toSet());

            if (!invalidFields.isEmpty()) {
                throw new IllegalArgumentException("Invalid fields for CUDA platform: " + invalidFields);
            }
        }
    }
}
