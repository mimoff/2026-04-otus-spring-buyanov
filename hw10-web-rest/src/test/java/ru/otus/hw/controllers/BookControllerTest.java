package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.TestUtils;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("BookController должен")
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @Test
    @DisplayName("должен возвращать список всех книг")
    void shouldReturnAllBooks() throws Exception {
        var books = TestUtils.getDbBooks();
        when(bookService.findAll()).thenReturn(books);
        var expectedBooks = books.stream().map(BookDto::fromDomainObject).collect(Collectors.toList());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(books.size()))
                .andExpect(jsonPath("$[0].id").value(expectedBooks.get(0).getId()))
                .andExpect(jsonPath("$[0].title").value(expectedBooks.get(0).getTitle()))
                .andExpect(jsonPath("$[0].author.fullName").value(expectedBooks.get(0).getAuthor().getFullName()))
                .andExpect(jsonPath("$[0].genres[0].name").value(expectedBooks.get(0).getGenres().get(0).getName()));

        verify(bookService).findAll();
    }

    @Test
    @DisplayName("должен возвращать книгу по id")
    void shouldReturnBookById() throws Exception {
        var expectedBook = TestUtils.getDbBooks().get(0);
        when(bookService.findById(expectedBook.getId())).thenReturn(Optional.of(expectedBook));

        mockMvc.perform(get("/api/books/{id}", expectedBook.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedBook.getId()))
                .andExpect(jsonPath("$.title").value(expectedBook.getTitle()))
                .andExpect(jsonPath("$.author.id").value(expectedBook.getAuthor().getId()))
                .andExpect(jsonPath("$.author.fullName").value(expectedBook.getAuthor().getFullName()))
                .andExpect(jsonPath("$.genres[0].name").value(expectedBook.getGenres().get(0).getName()));

        verify(bookService).findById(expectedBook.getId());
    }

    @DisplayName("должен создать книгу и вернуть созданную книгу")
    @Test
    void shouldCreateBook() throws Exception {
        var expectedBook = TestUtils.getDbBooks().get(0);
        var bookDto = BookUpdateDto.fromDomainObject(expectedBook);
        when(bookService.insert(bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreIds())).thenReturn(expectedBook);

        mockMvc.perform(post("/api/books")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedBook.getId()))
                .andExpect(jsonPath("$.title").value(expectedBook.getTitle()))
                .andExpect(jsonPath("$.author.id").value(expectedBook.getAuthor().getId()))
                .andExpect(jsonPath("$.author.fullName").value(expectedBook.getAuthor().getFullName()))
                .andExpect(jsonPath("$.genres[0].name").value(expectedBook.getGenres().get(0).getName()));

        verify(bookService).insert(bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreIds());
    }

    @Test
    @DisplayName("должен обновить книгу и вернуть обновленную книгу")
    void shouldUpdateBook() throws Exception {
        var expectedBook = TestUtils.getDbBooks().get(0);
        var bookDto = BookUpdateDto.fromDomainObject(expectedBook);
        when(bookService.update(bookDto.getId(), bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreIds()))
                .thenReturn(expectedBook);

        mockMvc.perform(put("/api/books/{id}", bookDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedBook.getId()))
                .andExpect(jsonPath("$.title").value(expectedBook.getTitle()))
                .andExpect(jsonPath("$.author.id").value(expectedBook.getAuthor().getId()))
                .andExpect(jsonPath("$.author.fullName").value(expectedBook.getAuthor().getFullName()))
                .andExpect(jsonPath("$.genres[0].name").value(expectedBook.getGenres().get(0).getName()));

        verify(bookService).update(bookDto.getId(), bookDto.getTitle(), bookDto.getAuthorId(), bookDto.getGenreIds());
    }

    @Test
    @DisplayName("удалить книгу")
    void shouldDeleteBook() throws Exception {
        var bookId = 1l;
        when(bookService.findById(bookId))
                .thenReturn(Optional.of(TestUtils.getDbBooks().get(0)));
        mockMvc.perform(delete("/api/books/{id}", bookId))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(bookId);
    }

    @DisplayName("должен возвращать 404 при удалении несуществующей книги")
    @Test
    void shouldReturnNotFoundForMissingBookDelete() throws Exception {
        when(bookService.findById(1000L))
                .thenThrow(new EntityNotFoundException("Book with id 1000 not found"));
        mockMvc.perform(delete("/api/books/1000"))
                .andExpect(status().isNotFound());
    }
}
