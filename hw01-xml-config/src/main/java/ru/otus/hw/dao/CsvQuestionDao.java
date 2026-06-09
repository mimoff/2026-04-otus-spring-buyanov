package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        // Использовать CsvToBean
        // https://opencsv.sourceforge.net/#collection_based_bean_fields_one_to_many_mappings
        // Использовать QuestionReadException
        // Про ресурсы: https://mkyong.com/java/java-read-a-file-from-resources-folder/

        List<QuestionDto> questionDtoList = readResource();
        List<Question> questions = convertDtoToDomain(questionDtoList);

        return questions;
    }

    private List<QuestionDto> readResource() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileNameProvider.getTestFileName())) {

            List<QuestionDto> questionDtoList = new CsvToBeanBuilder(new InputStreamReader(Objects.requireNonNull(is)))
                    .withSeparator(';')
                    .withSkipLines(1)
                    .withType(QuestionDto.class)
                    .build()
                    .parse();

            return questionDtoList;
        } catch (Exception e) {
            throw new QuestionReadException("resource read error");
        }
    }

    private List<Question> convertDtoToDomain(List<QuestionDto> questionDtoList) {
        List<Question> questions = questionDtoList
                    .stream()
                    .map(QuestionDto::toDomainObject)
                    .toList();

        return questions;
    }

}
