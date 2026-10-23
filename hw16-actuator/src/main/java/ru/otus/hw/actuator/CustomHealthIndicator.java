package ru.otus.hw.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.BookRepository;

@Component
@RequiredArgsConstructor
public class CustomHealthIndicator implements HealthIndicator {
    private final BookRepository bookRepository;

    @Override
    public Health health() {
        // serverIsDown, когда кол-во книг в репозитории нечетное
        boolean serverIsDown = (bookRepository.count() % 2) == 1;

        if (serverIsDown) {
            return Health.down()
                    .status(Status.DOWN)
                    .build();
        } else {
            return Health.up().build();
        }
    }
}