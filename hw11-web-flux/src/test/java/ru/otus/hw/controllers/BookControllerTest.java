package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.TestUtils;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@DisplayName("BookController должен")
@WebFluxTest(BookController.class)
class BookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("должен возвращать список всех книг")
    void shouldReturnAllBooks() {
        var books = TestUtils.getDbBooksDto();
        when(bookService.findAll()).thenReturn(Flux.just(books.get(0)));

        webTestClient.get().uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(books.get(0).getId())
                .jsonPath("$[0].title").isEqualTo(books.get(0).getTitle())
                .jsonPath("$[0].author.fullName").isEqualTo(books.get(0).getAuthor().getFullName())
                .jsonPath("$[0].genres[0].name").isEqualTo(books.get(0).getGenres().get(0).getName());

        verify(bookService).findAll();
    }

    @Test
    @DisplayName("должен возвращать книгу по id")
    void shouldReturnBookById() {
        var expectedBook = TestUtils.getDbBooksDto().get(0);
        when(bookService.findById(expectedBook.getId())).thenReturn(Mono.just(expectedBook));

        webTestClient.get().uri("/api/books/{id}", expectedBook.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(expectedBook.getId())
                .jsonPath("$.title").isEqualTo(expectedBook.getTitle())
                .jsonPath("$.author.fullName").isEqualTo(expectedBook.getAuthor().getFullName())
                .jsonPath("$.genres[0].name").isEqualTo(expectedBook.getGenres().get(0).getName());

        verify(bookService).findById(expectedBook.getId());
    }

    @DisplayName("должен создать книгу и вернуть созданную книгу")
    @Test
    void shouldCreateBook() {
        var expectedBook = TestUtils.getDbBooksDto().get(0);
        var bookUpdateDto = BookUpdateDto.fromDomainObject(TestUtils.getDbBooks().get(0));
        when(bookService.insert(bookUpdateDto)).thenReturn(Mono.just(expectedBook));

        webTestClient.post().uri("/api/books")
                .contentType(APPLICATION_JSON)
                .bodyValue(bookUpdateDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(expectedBook.getId())
                .jsonPath("$.title").isEqualTo(expectedBook.getTitle())
                .jsonPath("$.author.id").isEqualTo(expectedBook.getAuthor().getId())
                .jsonPath("$.author.fullName").isEqualTo(expectedBook.getAuthor().getFullName())
                .jsonPath("$.genres[0].name").isEqualTo(expectedBook.getGenres().get(0).getName());

        verify(bookService).insert(bookUpdateDto);
    }

    @Test
    @DisplayName("должен обновить книгу и вернуть обновленную книгу")
    void shouldUpdateBook() {
        var expectedBook = TestUtils.getDbBooksDto().get(0);
        var bookUpdateDto = BookUpdateDto.fromDomainObject(TestUtils.getDbBooks().get(0));
        when(bookService.update(bookUpdateDto))
                .thenReturn(Mono.just(expectedBook));

        webTestClient.put().uri("/api/books/{id}", bookUpdateDto.getId())
                .contentType(APPLICATION_JSON)
                .bodyValue(bookUpdateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(expectedBook.getId())
                .jsonPath("$.title").isEqualTo(expectedBook.getTitle())
                .jsonPath("$.author.id").isEqualTo(expectedBook.getAuthor().getId())
                .jsonPath("$.author.fullName").isEqualTo(expectedBook.getAuthor().getFullName())
                .jsonPath("$.genres[0].name").isEqualTo(expectedBook.getGenres().get(0).getName());

        verify(bookService).update(bookUpdateDto);
    }

    @Test
    @DisplayName("удалить книгу")
    void shouldDeleteBook() {
        var bookId = "b1";
        when(bookService.deleteById(bookId))
                .thenReturn(Mono.empty());
        webTestClient.delete().uri("/api/books/{id}", bookId)
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService).deleteById(bookId);
    }

    @DisplayName("должен возвращать 404 при удалении несуществующей книги")
    @Test
    void shouldReturnNotFoundForMissingBookDelete() {
        doThrow(new EntityNotFoundException("Book with id b1000 not found"))
                .when(bookService).deleteById("b1000");

        webTestClient.delete().uri("/api/books/b1000")
                .exchange()
                .expectStatus().isNotFound();
    }
}