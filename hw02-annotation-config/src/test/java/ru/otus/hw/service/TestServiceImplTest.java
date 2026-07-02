package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.otus.hw.TestUtils.getExpectedQuestions;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    Student student = new Student("studentFirstName","studentLastName");
    @Mock
    IOService ioService;
    @Mock
    QuestionDao questionDao;
    @InjectMocks
    TestServiceImpl testService;

    @Test
    void test_executeTestSucceed() {
        when(questionDao.findAll()).thenReturn(getExpectedQuestions());
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString())).thenReturn(1);
        var expectedTestResult = getExpectedTestResult(student, true);

        var actualTestResult = testService.executeTestFor(student);

        assertEquals(expectedTestResult, actualTestResult);
        verify(questionDao, times(1)).findAll();
    }

    @Test
    void test_executeTestFailed() {
        when(questionDao.findAll()).thenReturn(getExpectedQuestions());
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString())).thenReturn(2);
        var expectedTestResult = getExpectedTestResult(student, false);

        var actualTestResult = testService.executeTestFor(student);

        assertEquals(expectedTestResult, actualTestResult);
        verify(questionDao, times(1)).findAll();
    }

    private TestResult getExpectedTestResult(Student student, boolean validResult) {
        var testResult = new TestResult(student);
        testResult.applyAnswer(getExpectedQuestions().get(0), validResult);
        return testResult;
    }

}