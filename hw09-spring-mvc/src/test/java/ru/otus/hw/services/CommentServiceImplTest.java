package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Сервис для работы с комментариями к книгам ")
@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@Import({CommentServiceImpl.class})
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @DisplayName("должен загружать комментарий по id и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldReturnCommentByIdAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualComment = commentService.findById(1L);

        assertThat(actualComment).isPresent()
                .get()
                .matches(comment -> comment.getId() == 1L)
                .matches(comment -> comment.getText().equals("Comment_1"));

        assertThatCode(() -> {
            var comment = actualComment.orElseThrow();
            comment.getBook().getTitle();
            comment.getBook().getAuthor().getFullName();
            comment.getBook().getGenres().forEach(genre -> genre.getName());
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен загружать все комментарии к книге и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldReturnCommentsByBookIdAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualComments = commentService.findByBookId(1L);

        assertThat(actualComments).hasSize(2)
                .extracting(comment -> comment.getText())
                .containsExactly("Comment_1", "Comment_2");

        assertThatCode(() -> actualComments.forEach(comment -> {
            comment.getBook().getTitle();
            comment.getBook().getAuthor().getFullName();
            comment.getBook().getGenres().forEach(genre -> genre.getName());
        })).doesNotThrowAnyException();
    }

    @DisplayName("должен сохранять новый комментарий и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldInsertCommentAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualComment = commentService.insert("Comment_10500", 1L);
        try {
            assertThat(actualComment).isNotNull()
                    .matches(comment -> comment.getId() > 0)
                    .matches(comment -> comment.getText().equals("Comment_10500"));

            assertThatCode(() -> {
                actualComment.getBook().getTitle();
                actualComment.getBook().getAuthor().getFullName();
                actualComment.getBook().getGenres().forEach(genre -> genre.getName());
            }).doesNotThrowAnyException();

            assertThat(commentService.findById(actualComment.getId())).isPresent();
        } finally {
            commentService.deleteById(actualComment.getId());
        }
    }

    @DisplayName("должен обновлять комментарий и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldUpdateCommentAndAllowRelationsAccessOutsideServiceTransaction() {
        var createdComment = commentService.insert("Comment_ToUpdate", 1L);
        try {
            var actualComment = commentService.update(createdComment.getId(), "Comment_edited", 3L);

            assertThat(actualComment).isNotNull()
                    .matches(comment -> comment.getId() == createdComment.getId())
                    .matches(comment -> comment.getText().equals("Comment_edited"));

            assertThatCode(() -> {
                actualComment.getBook().getTitle();
                actualComment.getBook().getAuthor().getFullName();
                actualComment.getBook().getGenres().forEach(genre -> genre.getName());
            }).doesNotThrowAnyException();

            assertThat(actualComment.getBook().getId()).isEqualTo(3L);
            assertThat(actualComment.getBook().getAuthor().getFullName()).isEqualTo("Author_3");
            assertThat(actualComment.getBook().getGenres())
                    .extracting(genre -> genre.getName())
                    .containsExactlyInAnyOrder("Genre_5", "Genre_6");
        } finally {
            commentService.deleteById(createdComment.getId());
        }
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteCommentById() {
        var createdComment = commentService.insert("Comment_ToDelete", 1L);
        assertThat(commentService.findById(createdComment.getId())).isPresent();

        commentService.deleteById(createdComment.getId());

        assertThat(commentService.findById(createdComment.getId())).isEmpty();
    }
}
