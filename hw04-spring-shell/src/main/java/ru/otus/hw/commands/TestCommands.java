package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.service.TestRunnerService;

@RequiredArgsConstructor
@ShellComponent
public class TestCommands {
    private final TestRunnerService testRunnerService;

    @ShellMethod(value = "Test Run", key = "tr")
    public void testRun() {
        testRunnerService.run();
    }
}
