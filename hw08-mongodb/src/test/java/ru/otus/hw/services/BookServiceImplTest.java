package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.TestUtils;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Сервис для работы с книгами ")
@DataMongoTest
@Import({BookServiceImpl.class})
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

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

    @DisplayName("должен загружать книгу по id и позволять использовать связи вне транзакции сервиса")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbBooks")
    void shouldReturnBookByIdAndAllowRelationsAccessOutsideServiceTransaction(Book expectedBook) {
        var actualBook = bookService.findById(expectedBook.getId());

        assertThat(actualBook).isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expectedBook);

        assertThatCode(() -> {
            var book = actualBook.orElseThrow();
            book.getAuthor().getFullName();
            book.getGenres().forEach(Genre::getName);
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен загружать все книги и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldReturnAllBooksAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualBooks = bookService.findAll();
        var expectedBooks = TestUtils.getDbBooks();

        assertThat(actualBooks).containsExactlyElementsOf(expectedBooks);

        assertThatCode(() -> actualBooks.forEach(book -> {
            book.getAuthor().getFullName();
            book.getGenres().forEach(Genre::getName);
        })).doesNotThrowAnyException();
    }

    @DisplayName("должен сохранять новую книгу и позволять использовать ее связи вне транзакции сервиса")
    @Test
    void shouldInsertBookAndAllowRelationsAccessOutsideServiceTransaction() {
        var expectedBook = new Book("b1", "BookTitle_10500", TestUtils.getDbAuthors().get(0),
                List.of(TestUtils.getDbGenres().get(0), TestUtils.getDbGenres().get(2)));
        var expectedGenres = expectedBook.getGenres().stream()
                .map(Genre::getId).collect(Collectors.toSet());
        var actualBook = bookService.insert(expectedBook.getTitle(), expectedBook.getAuthor().getId(), expectedGenres);

        assertThat(actualBook).isNotNull()
                .matches(book -> !book.getId().isBlank())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedBook);

        assertThatCode(() -> {
            actualBook.getAuthor().getFullName();
            actualBook.getGenres().forEach(Genre::getName);
        }).doesNotThrowAnyException();

        assertThat(bookService.findById(actualBook.getId())).isPresent();
    }

    @DisplayName("должен обновлять книгу и позволять использовать ее связи вне транзакции сервиса")
    @Test
    void shouldUpdateBookAndAllowRelationsAccessOutsideServiceTransaction() {
        var expectedBook = new Book("b1", "BookTitle_Updated", TestUtils.getDbAuthors().get(2),
                List.of(TestUtils.getDbGenres().get(4), TestUtils.getDbGenres().get(5)));
        var expectedGenres = expectedBook.getGenres().stream()
                .map(Genre::getId).collect(Collectors.toSet());

        var actualBook = bookService.findById(expectedBook.getId());
        assertThat(actualBook)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isNotEqualTo(expectedBook);

        var returnedBook = bookService.update(expectedBook.getId(), expectedBook.getTitle(), expectedBook.getAuthor().getId(), expectedGenres);

        assertThat(returnedBook).isNotNull()
                .matches(book -> !book.getId().isBlank())
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expectedBook);

        assertThatCode(() -> {
            returnedBook.getAuthor().getFullName();
            returnedBook.getGenres().forEach(Genre::getName);
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        var createdBook = bookService.insert("BookTitle_ToDelete", "a2", Set.of("g3", "g4"));
        assertThat(bookService.findById(createdBook.getId())).isPresent();

        bookService.deleteById(createdBook.getId());

        assertThat(bookService.findById(createdBook.getId())).isEmpty();
    }
}
