package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.TestUtils;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Сервис для работы с книгами ")
@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@Import({BookServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу по id и позволять использовать связи вне транзакции сервиса")
    @ParameterizedTest
    @MethodSource("ru.otus.hw.TestUtils#getDbBooks")
    void shouldReturnBookByIdAndAllowRelationsAccessOutsideServiceTransaction(Book expectedBook) {
        var actualBook = bookService.findById(expectedBook.getId());

        assertThat(actualBook).isPresent()
                .get()
                .usingRecursiveComparison()
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
        var expectedBook = new Book(0, "BookTitle_10500", TestUtils.getDbAuthors().get(0),
                List.of(TestUtils.getDbGenres().get(0), TestUtils.getDbGenres().get(2)));
        var expectedGenres = expectedBook.getGenres().stream()
                .map(Genre::getId).collect(Collectors.toSet());
        var actualBook = bookService.insert(expectedBook.getTitle(), expectedBook.getAuthor().getId(), expectedGenres);

        assertThat(actualBook).isNotNull()
                .matches(book -> book.getId() > 0)
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
        var expectedBook = new Book(1L, "BookTitle_Updated", TestUtils.getDbAuthors().get(2),
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
                .matches(book -> book.getId() > 0)
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
        var createdBook = bookService.insert("BookTitle_ToDelete", 2L, Set.of(3L, 4L));
        assertThat(bookService.findById(createdBook.getId())).isPresent();

        bookService.deleteById(createdBook.getId());

        assertThat(bookService.findById(createdBook.getId())).isEmpty();
    }
}
