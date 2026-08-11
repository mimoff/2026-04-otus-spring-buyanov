package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.otus.hw.TestUtils;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями ")
@DataJpaTest
class JpaCommentRepositoryTest {

    @Autowired
    private CommentRepository repositoryJpa;

    @DisplayName("должен загружать комментарий по id")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbComments")
    void shouldReturnCorrectCommentById(Comment expectedComment) {
        var actualBook = repositoryJpa.findById(expectedComment.getId());
        assertThat(actualBook).isPresent()
                .get()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать комментарии по id книги")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbBooks")
    void shouldReturnCorrectCommentByBookId(Book expectedBook) {
        var actualComments = repositoryJpa.findByBookId(expectedBook.getId());
        var expectedComments = TestUtils.getDbComments().stream()
                .filter(c -> c.getBook().getId()==expectedBook.getId())
                .collect(Collectors.toList());

        assertThat(actualComments).containsExactlyElementsOf(expectedComments);
        actualComments.forEach(System.out::println);
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var expectedComment = new Comment(0, "CommentTitle_10500", TestUtils.getDbBooks().get(2));
        var returnedComment = repositoryJpa.save(expectedComment);
        assertThat(returnedComment).isNotNull()
                .matches(comment -> comment.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedComment);

        assertThat(repositoryJpa.findById(returnedComment.getId()))
                .isPresent()
                .get()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var expectedComment = new Comment(1L, "CommentTitle_10500", TestUtils.getDbBooks().get(2));

        assertThat(repositoryJpa.findById(expectedComment.getId()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isNotEqualTo(expectedComment);

        var returnedComment = repositoryJpa.save(expectedComment);
        assertThat(returnedComment).isNotNull()
                .matches(comment -> comment.getId() > 0)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expectedComment);

        assertThat(repositoryJpa.findById(returnedComment.getId()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteComment() {
        assertThat(repositoryJpa.findById(1L)).isPresent();
        repositoryJpa.deleteById(1L);
        assertThat(repositoryJpa.findById(1L)).isEmpty();
    }

 }