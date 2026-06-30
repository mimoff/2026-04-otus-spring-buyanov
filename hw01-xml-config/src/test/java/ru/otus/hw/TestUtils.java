package ru.otus.hw;

import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.ArrayList;
import java.util.List;

final public class TestUtils {
    public static List<Question> getExpectedQuestions() {
        var listQuestions = new ArrayList<Question>();
        var answers = new ArrayList<Answer>();
        answers.add(new Answer("Answer1", true));
        answers.add(new Answer("Answer2", false));
        answers.add(new Answer("Answer3", false));
        listQuestions.add(new Question("TestQuestion1", answers));
        return listQuestions;
    }
}