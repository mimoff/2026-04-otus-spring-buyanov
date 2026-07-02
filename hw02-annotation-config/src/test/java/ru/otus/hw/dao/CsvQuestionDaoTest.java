package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.otus.hw.TestUtils.getExpectedQuestions;

@ExtendWith(MockitoExtension.class)
class CsvQuestionDaoTest {

    @Mock
    TestFileNameProvider fileNameProvider;
    @InjectMocks
    CsvQuestionDao csvQuestionDao;

    @BeforeEach
    void setUp() {
        when(fileNameProvider.getTestFileName()).thenReturn("questionsTest.csv");
    }

    @Test
    void shouldThrowQuestionReadExceptionIfResourceNotExists() {
        when(fileNameProvider.getTestFileName()).thenReturn("nonexistentFile.csv");
        assertThatThrownBy(() -> csvQuestionDao.findAll()).isInstanceOf(QuestionReadException.class);
    }

    @Test
    void findAll() {
        var expectedQuestions = getExpectedQuestions();

        var actualQuestions =  csvQuestionDao.findAll();

        verify(fileNameProvider, times(1)).getTestFileName();
        assertThat(actualQuestions)
                .hasSize(1)
                .usingRecursiveComparison()
                .isEqualTo(expectedQuestions);
    }
}