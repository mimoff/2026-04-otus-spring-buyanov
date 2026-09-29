package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.TestUtils;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы жанрами")
@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
class JpaGenreRepositoryTest {

    @Autowired
    private GenreRepository repositoryJpa;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var actualGenres = repositoryJpa.findAll();
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
        var actualGenres = repositoryJpa.findAllByIds(setOfIds);

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        actualGenres.forEach(System.out::println);
    }

    @DisplayName("должен загружать жанры по списку единичных id")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbGenres")
    void shouldReturnCorrectGenreById(Genre expectedGenre) {
        var setOfIds = Set.of(expectedGenre.getId());
        var actualGenres = repositoryJpa.findAllByIds(setOfIds);
        var expectedGenres = List.of(expectedGenre);

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        actualGenres.forEach(System.out::println);
    }

}