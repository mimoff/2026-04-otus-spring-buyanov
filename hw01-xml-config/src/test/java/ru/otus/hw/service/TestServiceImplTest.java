package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static ru.otus.hw.TestUtils.getExpectedQuestions;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    IOService ioService;
    @Mock
    QuestionDao questionDao;
    @InjectMocks
    TestServiceImpl testService;

    @Test
    void executeTest() {
        when(questionDao.findAll()).thenReturn(getExpectedQuestions());
        var captorPrintLines = ArgumentCaptor.forClass(String.class);
        var captorPrintFormattedLines = ArgumentCaptor.forClass(String.class);
        var expectedPrintLines = getExpectedPrintLines();
        var expectedPrintFormattedLines = getExpectedPrintFormattedLines();

        testService.executeTest();

        verify(questionDao, times(1)).findAll();
        verify(ioService, times(3)).printLine(captorPrintLines.capture());
        assertThat(captorPrintLines.getAllValues())
                .hasSize(3)
                .usingRecursiveComparison()
                .isEqualTo(expectedPrintLines);
        verify(ioService, times(4)).printFormattedLine(captorPrintFormattedLines.capture());
        assertThat(captorPrintFormattedLines.getAllValues())
                .hasSize(4)
                .usingRecursiveComparison()
                .isEqualTo(expectedPrintFormattedLines);
    }

    private List<String> getExpectedPrintLines() {
        var expectedPrintLines = new ArrayList<String>();
        expectedPrintLines.add("");
        expectedPrintLines.add("TestQuestion1");
        expectedPrintLines.add("");
        return expectedPrintLines;
    }

    private List<String> getExpectedPrintFormattedLines() {
        var expectedPrintFormattedLines = new ArrayList<String>();
        expectedPrintFormattedLines.add("Please answer the questions below%n");
        expectedPrintFormattedLines.add("  1. Answer1");
        expectedPrintFormattedLines.add("  2. Answer2");
        expectedPrintFormattedLines.add("  3. Answer3");
        return expectedPrintFormattedLines;
    }
}