package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.TestUtils;
import ru.otus.hw.models.Author;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Mongo для работы авторами")
@DataMongoTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection("authors");
        mongoTemplate.dropCollection("books");
        mongoTemplate.dropCollection("genres");
        mongoTemplate.dropCollection("comments");

        TestUtils.getDbAuthors().forEach(author -> mongoTemplate.save(author, "authors"));
        TestUtils.getDbGenres().forEach(genre -> mongoTemplate.save(genre, "genres"));
        TestUtils.getDbBooks().forEach(book -> mongoTemplate.save(book, "books"));
        TestUtils.getDbComments().forEach(comment -> mongoTemplate.save(comment, "comments"));
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var actualAuthors = repository.findAll();
        var expectedAuthors = TestUtils.getDbAuthors();

        assertThat(actualAuthors).containsExactlyElementsOf(expectedAuthors);
        actualAuthors.forEach(System.out::println);
    }

    @DisplayName("должен загружать авторов по списку единичных id")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbAuthors")
    void shouldReturnCorrectAuthorById(Author expectedAuthor) {
        var actualAuthor = repository.findById(expectedAuthor.getId());

        assertThat(actualAuthor)
                .isPresent()
                .get()
                .isEqualTo(expectedAuthor);
        System.out.println(actualAuthor);
    }

}