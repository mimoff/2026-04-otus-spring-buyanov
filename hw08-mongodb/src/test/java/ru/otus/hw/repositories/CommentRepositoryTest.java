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
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями ")
@DataMongoTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository repository;

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
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbComments")
    void shouldReturnCorrectCommentById(Comment expectedComment) {
        var actualBook = repository.findById(expectedComment.getId());
        assertThat(actualBook).isPresent()
                .get()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен загружать комментарии по id книги")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbBooks")
    void shouldReturnCorrectCommentByBookId(Book expectedBook) {
        var actualComments = repository.findAllByBookId(expectedBook.getId());
        var expectedComments = TestUtils.getDbComments().stream()
                .filter(c -> c.getBook().getId().equals(expectedBook.getId()))
                .collect(Collectors.toList());

        assertThat(actualComments).containsExactlyElementsOf(expectedComments);
        actualComments.forEach(System.out::println);
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var expectedComment = new Comment(null, "CommentTitle_10500", TestUtils.getDbBooks().get(2));
        var returnedComment = repository.save(expectedComment);
        assertThat(returnedComment).isNotNull()
                .matches(comment -> !comment.getId().isBlank())
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedComment);

        assertThat(repository.findById(returnedComment.getId()))
                .isPresent()
                .get()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var expectedComment = new Comment("c1", "CommentTitle_10500", TestUtils.getDbBooks().get(2));

        assertThat(repository.findById(expectedComment.getId()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isNotEqualTo(expectedComment);

        var returnedComment = repository.save(expectedComment);
        assertThat(returnedComment).isNotNull()
                .matches(comment -> !comment.getId().isBlank())
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expectedComment);

        assertThat(repository.findById(returnedComment.getId()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(returnedComment);
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteComment() {
        assertThat(repository.findById("c1")).isPresent();
        repository.deleteById("c1");
        assertThat(repository.findById("c1")).isEmpty();
    }

 }