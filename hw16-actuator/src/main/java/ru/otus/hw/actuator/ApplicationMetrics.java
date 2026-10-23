package ru.otus.hw.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationMetrics {
    private final MeterRegistry meterRegistry;

    public void userRequest(String entity, String method) {
        meterRegistry.counter("user.requests",
                        "entity", entity,
                        "method", method
                )
                .increment();
    }
}
