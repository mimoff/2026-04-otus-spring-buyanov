package ru.otus.hw.changelog;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@ChangeUnit(id = "insertGenres", order = "003", author = "mimoff", runAlways = true)
public class GenresChangeUnit {

    private final GenreRepository genreRepository;

    public GenresChangeUnit(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Execution
    public void execute() {
        genreRepository.saveAll(List.of(
                new Genre("g1", "Genre_1"),
                new Genre("g2", "Genre_2"),
                new Genre("g3", "Genre_3"),
                new Genre("g4", "Genre_4"),
                new Genre("g5", "Genre_5"),
                new Genre("g6", "Genre_6")
        ));
    }

    @RollbackExecution
    public void rollback() {
        genreRepository.deleteAllById(List.of("g1", "g2", "g3", "g4", "g5", "g6"));
    }
}
