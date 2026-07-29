package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static ru.otus.hw.TestUtils.getExpectedQuestions;

@SpringBootTest
class TestServiceImplTest {

    Student student = new Student("studentFirstName","studentLastName");
    @MockitoBean
    LocalizedIOService ioService;
    @Autowired
    TestServiceImpl testService;

    @Test
    void test_executeTestSucceed() {
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString())).thenReturn(1);
        var expectedTestResult = getExpectedTestResult(student, true);

        var actualTestResult = testService.executeTestFor(student);

        assertEquals(expectedTestResult, actualTestResult);
    }

    @Test
    void test_executeTestFailed() {
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString())).thenReturn(2);
        var expectedTestResult = getExpectedTestResult(student, false);

        var actualTestResult = testService.executeTestFor(student);

        assertEquals(expectedTestResult, actualTestResult);
    }

    private TestResult getExpectedTestResult(Student student, boolean validResult) {
        var testResult = new TestResult(student);
        testResult.applyAnswer(getExpectedQuestions().get(0), validResult);
        return testResult;
    }

}