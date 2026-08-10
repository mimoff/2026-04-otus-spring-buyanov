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
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы жанрами")
@DataMongoTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository repository;

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

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repository.findAll();
        var expectedGenres = TestUtils.getDbGenres();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        actualGenres.forEach(System.out::println);
    }

    @DisplayName("должен загружать жанры по списку id")
    @Test
    void shouldReturnCorrectGenresListByAllIds() {
        var expectedGenres = TestUtils.getDbGenres();
        var setOfIds = expectedGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        var actualGenres = repository.findAllByIds(setOfIds);

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        actualGenres.forEach(System.out::println);
    }

    @DisplayName("должен загружать жанры по списку единичных id")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbGenres")
    void shouldReturnCorrectGenreById(Genre expectedGenre) {
        var setOfIds = Set.of(expectedGenre.getId());
        var actualGenres = repository.findAllByIds(setOfIds);
        var expectedGenres = List.of(expectedGenre);

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        actualGenres.forEach(System.out::println);
    }

}