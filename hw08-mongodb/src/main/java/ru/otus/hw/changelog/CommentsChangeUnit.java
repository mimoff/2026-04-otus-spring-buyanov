package ru.otus.hw.changelog;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.BookRepository;

import java.util.List;

@ChangeUnit(id = "insertComments", order = "005", author = "mimoff", runAlways = true)
public class CommentsChangeUnit {

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    public CommentsChangeUnit(BookRepository bookRepository, CommentRepository commentRepository) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
    }

    @Execution
    public void execute() {
        var book1 = bookRepository.findById("b1").orElseThrow();
        var book2 = bookRepository.findById("b2").orElseThrow();
        var book3 = bookRepository.findById("b3").orElseThrow();

        commentRepository.saveAll(List.of(
                new Comment("c1", "Comment_1", book1),
                new Comment("c2", "Comment_2", book2),
                new Comment("c3", "Comment_3", book2),
                new Comment("c4", "Comment_4", book3),
                new Comment("c5", "Comment_5", book3),
                new Comment("c6", "Comment_6", book3)
        ));
    }

    @RollbackExecution
    public void rollback() {
        commentRepository.deleteAllById(List.of("c1", "c2", "c3", "c4", "c5", "c6"));
    }
}
