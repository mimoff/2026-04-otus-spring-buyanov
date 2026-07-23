package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Сервис для работы с книгами ")
@DataJpaTest
@Import({BookServiceImpl.class, JpaBookRepository.class, JpaAuthorRepository.class, JpaGenreRepository.class})
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать книгу по id и позволять использовать связи вне транзакции сервиса")
    @Test
    void shouldReturnBookByIdAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualBook = bookService.findById(1L);

        assertThat(actualBook).isPresent()
                .get()
                .matches(book -> book.getId() == 1L)
                .matches(book -> book.getTitle().equals("BookTitle_1"));

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

        assertThat(actualBooks).isNotEmpty()
                .extracting(Book::getId)
                .contains(1L, 2L, 3L);

        assertThatCode(() -> actualBooks.forEach(book -> {
            book.getAuthor().getFullName();
            book.getGenres().forEach(Genre::getName);
        })).doesNotThrowAnyException();
    }

    @DisplayName("должен сохранять новую книгу и позволять использовать ее связи вне транзакции сервиса")
    @Test
    void shouldInsertBookAndAllowRelationsAccessOutsideServiceTransaction() {
        var actualBook = bookService.insert("BookTitle_10500", 1L, Set.of(1L, 3L));

        assertThat(actualBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .matches(book -> book.getTitle().equals("BookTitle_10500"));

        assertThatCode(() -> {
            actualBook.getAuthor().getFullName();
            actualBook.getGenres().forEach(Genre::getName);
        }).doesNotThrowAnyException();

        assertThat(bookService.findById(actualBook.getId())).isPresent();
    }

    @DisplayName("должен обновлять книгу и позволять использовать ее связи вне транзакции сервиса")
    @Test
    void shouldUpdateBookAndAllowRelationsAccessOutsideServiceTransaction() {
        var createdBook = bookService.insert("BookTitle_ToUpdate", 1L, Set.of(1L, 2L));
        var actualBook = bookService.update(createdBook.getId(), "BookTitle_Updated", 3L, Set.of(5L, 6L));

        assertThat(actualBook).isNotNull()
                .matches(book -> book.getId() == createdBook.getId())
                .matches(book -> book.getTitle().equals("BookTitle_Updated"));

        assertThatCode(() -> {
            actualBook.getAuthor().getFullName();
            actualBook.getGenres().forEach(Genre::getName);
        }).doesNotThrowAnyException();

        assertThat(actualBook.getAuthor().getFullName()).isEqualTo("Author_3");
        assertThat(actualBook.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre_5", "Genre_6");
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
