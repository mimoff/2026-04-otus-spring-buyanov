package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.TestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Сервис для работы с комментариями к книгам ")
@DataMongoTest
@Import({CommentServiceImpl.class})
class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

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

    @DisplayName("должен загружать комментарий по id и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldReturnCommentByIdAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualComment = commentService.findById("c1");

        assertThat(actualComment).isPresent()
                .get()
                .matches(comment -> comment.getId().equals("c1"))
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
        var actualComments = commentService.findByBookId("b1");

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
        var actualComment = commentService.insert("Comment_10500", "b1");
        try {
            assertThat(actualComment).isNotNull()
                    .matches(comment -> !comment.getId().isBlank())
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
        var createdComment = commentService.insert("Comment_ToUpdate", "b1");
        try {
            var actualComment = commentService.update(createdComment.getId(), "Comment_edited", "b3");

            assertThat(actualComment).isNotNull()
                    .matches(comment -> comment.getId() == createdComment.getId())
                    .matches(comment -> comment.getText().equals("Comment_edited"));

            assertThatCode(() -> {
                actualComment.getBook().getTitle();
                actualComment.getBook().getAuthor().getFullName();
                actualComment.getBook().getGenres().forEach(genre -> genre.getName());
            }).doesNotThrowAnyException();

            assertThat(actualComment.getBook().getId()).isEqualTo("b3");
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
        var createdComment = commentService.insert("Comment_ToDelete", "b1");
        assertThat(commentService.findById(createdComment.getId())).isPresent();

        commentService.deleteById(createdComment.getId());

        assertThat(commentService.findById(createdComment.getId())).isEmpty();
    }
}
