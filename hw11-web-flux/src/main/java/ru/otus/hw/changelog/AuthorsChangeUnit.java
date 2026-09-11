package ru.otus.hw.changelog;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

@ChangeUnit(id = "insertAuthors", order = "002", author = "mimoff", runAlways = true)
public class AuthorsChangeUnit {

    private final AuthorRepository authorRepository;

    public AuthorsChangeUnit(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Execution
    public void execute() {
        authorRepository.saveAll(List.of(
                new Author("a1", "Author_1"),
                new Author("a2", "Author_2"),
                new Author("a3", "Author_3")
        ));
    }

    @RollbackExecution
    public void rollback() {
        authorRepository.deleteAllById(List.of("a1", "a2", "a3"));
    }
}
