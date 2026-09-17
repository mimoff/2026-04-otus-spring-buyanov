package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.TestUtils;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCommentByIdAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualComment = commentService.findById("c1").block();

        assertThat(actualComment)
                .isNotNull()
                .matches(comment -> comment.getId().equals("c1"))
                .matches(comment -> comment.getText().equals("Comment_1"));
    }

    @DisplayName("должен загружать все комментарии к книге")
    @Test
    void shouldReturnCommentsByBookIdAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualComments = commentService.findAllByBookId("b1").collectList().block();

        assertThat(actualComments).hasSize(2)
                .extracting(comment -> comment.getText())
                .containsExactly("Comment_1", "Comment_2");
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldInsertCommentAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualComment = commentService.insert(new CommentUpdateDto(null,"Comment_10500", "b1")).block();
        try {
            assertThat(actualComment).isNotNull()
                    .matches(comment -> !comment.getId().isEmpty())
                    .matches(comment -> comment.getText().equals("Comment_10500"));

            assertThat(commentService.findById(actualComment.getId()).block()).isNotNull();
        } finally {
            commentService.deleteById(actualComment.getId());
        }
    }

    @DisplayName("должен обновлять комментарий")
    @Test
    void shouldUpdateCommentAndAllowRelationsAccessOutsideServiceTransaction() {
        var createdComment = commentService.insert( new CommentUpdateDto(null, "Comment_ToUpdate", "b1")).block();
        try {
            var actualComment = commentService.update(new CommentUpdateDto( createdComment.getId(), "Comment_edited", "b3")).block();

            assertThat(actualComment).isNotNull()
                    .matches(comment -> comment.getId().equals(createdComment.getId()))
                    .matches(comment -> comment.getText().equals("Comment_edited"));

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
        var createdComment = commentService.insert( new CommentUpdateDto(null, "Comment_ToDelete", "b1")).block();
        assertThat(commentService.findById(createdComment.getId()).block()).isNotNull();

        commentService.deleteById(createdComment.getId()).block();

        assertThatThrownBy(() -> commentService.findById(createdComment.getId()).block())
                .isInstanceOf(EntityNotFoundException.class);
    }
}
