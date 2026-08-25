package ru.otus.hw.changelog;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@ChangeUnit(id = "insertBooks", order = "004", author = "mimoff", runAlways = true)
public class BooksChangeUnit {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    public BooksChangeUnit(AuthorRepository authorRepository,
                           GenreRepository genreRepository,
                           BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
    }

    @Execution
    public void execute() {
        var author1 = authorRepository.findById("a1").orElseThrow();
        var author2 = authorRepository.findById("a2").orElseThrow();
        var author3 = authorRepository.findById("a3").orElseThrow();

        var genre1 = genreRepository.findById("g1").orElseThrow();
        var genre2 = genreRepository.findById("g2").orElseThrow();
        var genre3 = genreRepository.findById("g3").orElseThrow();
        var genre4 = genreRepository.findById("g4").orElseThrow();
        var genre5 = genreRepository.findById("g5").orElseThrow();
        var genre6 = genreRepository.findById("g6").orElseThrow();

        bookRepository.saveAll(List.of(
                new Book("b1", "BookTitle_1", author1, List.of(genre1, genre2)),
                new Book("b2", "BookTitle_2", author2, List.of(genre3, genre4)),
                new Book("b3", "BookTitle_3", author3, List.of(genre5, genre6))
        ));
    }

    @RollbackExecution
    public void rollback() {
        bookRepository.deleteAllById(List.of("b1", "b2", "b3"));
    }
}
