package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.TestUtils;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("BookController должен")
@WebMvcTest(BookController.class)
class BookControllerTest {

    private static final Long BOOK_ID = 1l;

    private static final String BOOK_TITLE = "Any title";

    private static final Long AUTHOR_ID = 2l;

    private static final Long GENRE_ID_1 = 3l;

    private static final Long GENRE_ID_2 = 4l;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private CommentService commentService;

    @Test
    @DisplayName("вернуть страницу с книгами")
    void shouldReturnBooksListPage() throws Exception {
        var books = TestUtils.getDbBooks();
        when(bookService.findAll()).thenReturn(books);
        var expectedBooks = books.stream().map(BookDto::fromDomainObject).collect(Collectors.toList());

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-list"))
                .andExpect(model().attribute("books", hasSize(expectedBooks.size())))
                .andExpect(model().attribute("books", expectedBooks))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedBooks.get(0).getTitle())));

        verify(bookService).findAll();
    }

    @Test
    @DisplayName("вернуть страницу с созданием книги")
    void shouldReturnCreateBookPage() throws Exception {
        var authors = TestUtils.getDbAuthors();
        when(authorService.findAll()).thenReturn(authors);

        var genres = TestUtils.getDbGenres();
        when(genreService.findAll()).thenReturn(genres);

        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-edit"))
                .andExpect(model().attributeExists("book", "authors", "genres"))
                .andExpect(model().attribute("authors", hasSize(authors.size())))
                .andExpect(model().attribute("genres", hasSize(genres.size())));

        verify(authorService).findAll();
        verify(genreService).findAll();
    }

    @Test
    @DisplayName("вернуть страницу редактирования книги")
    void shouldReturnEditBookPage() throws Exception {
        var book = TestUtils.getDbBooks().get(0);
        when(bookService.findById(book.getId())).thenReturn(Optional.of(book));
        var genreIds = book.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());

        var authors = TestUtils.getDbAuthors();
        when(authorService.findAll()).thenReturn(authors);

        var genres = TestUtils.getDbGenres();
        when(genreService.findAll()).thenReturn(genres);

        mockMvc.perform(get("/books/{id}/edit", book.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("book-edit"))
                .andExpect(model().attributeExists("book", "authors", "genres"))
                .andExpect(model().attribute("book", hasProperty("id", is(book.getId()))))
                .andExpect(model().attribute("book", hasProperty("title", is(book.getTitle()))))
                .andExpect(model().attribute("book", hasProperty("authorId", is(book.getAuthor().getId()))))
                .andExpect(model().attribute("book", hasProperty("genreIds", is(genreIds))));

        verify(bookService).findById(book.getId());
        verify(authorService).findAll();
        verify(genreService).findAll();
    }

    @Test
    @DisplayName("создать книгу и перенаправить на страницу с книгами")
    void shouldCreateBookAndRedirect() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", BOOK_TITLE)
                        .param("authorId", AUTHOR_ID.toString())
                        .param("genreIds", GENRE_ID_1.toString(), GENRE_ID_2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).insert(BOOK_TITLE, AUTHOR_ID, Set.of(GENRE_ID_1, GENRE_ID_2));
    }

    @Test
    @DisplayName("обновить книгу и перенаправить на страницу с книгами")
    void shouldUpdateBookAndRedirect() throws Exception {
        mockMvc.perform(post("/books/edit")
                        .param("id", BOOK_ID.toString())
                        .param("title", BOOK_TITLE)
                        .param("authorId", AUTHOR_ID.toString())
                        .param("genreIds", GENRE_ID_1.toString(), GENRE_ID_2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).update(BOOK_ID, BOOK_TITLE, AUTHOR_ID, Set.of(GENRE_ID_1, GENRE_ID_2));
    }

    @Test
    @DisplayName("удалить книгу и перенаправить на страницу с книгами")
    void shouldDeleteBookAndRedirect() throws Exception {
        var bookId = 1l;
        mockMvc.perform(post("/books/{id}/delete", bookId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteById(bookId);
    }

    @DisplayName("должен возвращать 404 при редактировании несуществующей книги")
    @Test
    void shouldReturnNotFoundForMissingBookEdit() throws Exception {
        given(bookService.findById(1000L)).willThrow(new EntityNotFoundException("Book with id 100 not found"));

        mockMvc.perform(get("/books/1000/edit"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }
}
