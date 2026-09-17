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
@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@Import({BookServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

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
        var expectedBook = new Book(0, "BookTitle_10500", TestUtils.getDbAuthors().get(0),
                List.of(TestUtils.getDbGenres().get(0), TestUtils.getDbGenres().get(2)));
        var expectedGenres = expectedBook.getGenres().stream()
                .map(Genre::getId).collect(Collectors.toSet());
        var newBook = new BookUpdateDto(0, expectedBook.getTitle(), expectedBook.getAuthor().getId(), expectedGenres);
        var actualBook = bookService.insert(newBook);

        assertThat(actualBook).isNotNull()
                .matches(book -> book.getId() > 0)
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
                .matches(book -> book.getId() > 0)
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
        var createdBook = bookService.insert(new BookUpdateDto(0,"BookTitle_ToDelete", 2L, Set.of(3L, 4L)));
        assertThat(bookService.findById(createdBook.getId())).isNotNull();

        bookService.deleteById(createdBook.getId());

//        assertThat(bookService.findById(createdBook.getId())).isEmpty();

        assertThatThrownBy(() -> bookService.findById(createdBook.getId()))
                .isInstanceOf(EntityNotFoundException.class);

    }
}
