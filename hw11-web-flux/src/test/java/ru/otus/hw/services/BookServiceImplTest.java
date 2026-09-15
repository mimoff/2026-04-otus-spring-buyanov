package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.TestUtils;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @MethodSource("ru.otus.hw.TestUtils#getDbBooksDto")
    void shouldReturnBookByIdAndAllowRelationsAccessOutsideServiceTransaction(BookDto expectedBook) {
        var actualBook = bookService.findById(expectedBook.getId());

        assertThat(actualBook)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);

        assertThatCode(() -> {
            actualBook.getAuthor().getFullName();
            actualBook.getGenres().forEach(GenreDto::getName);
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен загружать все книги и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldReturnAllBooksAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualBooks = bookService.findAll();
        var expectedBooks = TestUtils.getDbBooksDto();

        assertThat(actualBooks).containsExactlyElementsOf(expectedBooks);

        assertThatCode(() -> actualBooks.forEach(book -> {
            book.getAuthor().getFullName();
            book.getGenres().forEach(GenreDto::getName);
        })).doesNotThrowAnyException();
    }

    @DisplayName("должен сохранять новую книгу и позволять использовать ее связи вне транзакции сервиса")
    @Test
    void shouldInsertBookAndAllowRelationsAccessOutsideServiceTransaction() {
        var expectedBook = new Book(null, "BookTitle_10500", TestUtils.getDbAuthors().get(0),
                List.of(TestUtils.getDbGenres().get(0), TestUtils.getDbGenres().get(2)));
        var expectedGenres = expectedBook.getGenres().stream()
                .map(Genre::getId).collect(Collectors.toSet());
        var newBook = new BookUpdateDto(null, expectedBook.getTitle(), expectedBook.getAuthor().getId(), expectedGenres);
        var actualBook = bookService.insert(newBook);

        assertThat(actualBook).isNotNull()
                .matches(book -> !book.getId().isEmpty())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedBook);

        assertThatCode(() -> {
            actualBook.getAuthor().getFullName();
            actualBook.getGenres().forEach(GenreDto::getName);
        }).doesNotThrowAnyException();

        assertThatCode(() -> {
            bookService.findById(actualBook.getId());
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен обновлять книгу и позволять использовать ее связи вне транзакции сервиса")
    @Test
    void shouldUpdateBookAndAllowRelationsAccessOutsideServiceTransaction() {
        var expectedBook = TestUtils.getDbBooksDto().get(0);
        expectedBook.setTitle("UpdatedTitle");
        var expectedGenres = expectedBook.getGenres().stream()
                .map(GenreDto::getId).collect(Collectors.toSet());

        var actualBook = bookService.findById(expectedBook.getId());
        assertThat(actualBook)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isNotEqualTo(expectedBook);

        var bookUpdateDto = new BookUpdateDto(expectedBook.getId(), expectedBook.getTitle(), expectedBook.getAuthor().getId(), expectedGenres);
        var returnedBook = bookService.update(bookUpdateDto);

        assertThat(returnedBook).isNotNull()
                .matches(book -> !book.getId().isEmpty())
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expectedBook);

        assertThatCode(() -> {
            returnedBook.getAuthor().getFullName();
            returnedBook.getGenres().forEach(GenreDto::getName);
        }).doesNotThrowAnyException();
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        var createdBook = bookService.insert(new BookUpdateDto(null,"BookTitle_ToDelete", "a2", Set.of("g3", "g4")));
        assertThat(bookService.findById(createdBook.getId())).isNotNull();

        bookService.deleteById(createdBook.getId());

//        assertThat(bookService.findById(createdBook.getId())).isEmpty();

        assertThatThrownBy(() -> bookService.findById(createdBook.getId()))
                .isInstanceOf(EntityNotFoundException.class);

    }
}
