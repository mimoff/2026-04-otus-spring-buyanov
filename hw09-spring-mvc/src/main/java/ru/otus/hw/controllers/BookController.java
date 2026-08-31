package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    @GetMapping("/books")
    public String listBooks(Model model) {
        List<BookDto> books = bookService.findAll().stream()
                .map(BookDto::fromDomainObject).toList();
        model.addAttribute("books", books);
        return "book-list";
    }

    @GetMapping("/books/new")
    public String showCreateForm(Model model) {
        model.addAttribute("book", new BookUpdateDto());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book-edit";
    }

    @PostMapping("/books")
    public String createBook(@Valid @ModelAttribute("book") BookUpdateDto bookUpdateDto,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            return "book-edit";
        }

        bookService.insert(bookUpdateDto.getTitle(), bookUpdateDto.getAuthorId(), bookUpdateDto.getGenreIds());
        return "redirect:/books";
    }

    @GetMapping("/books/{id}")
    public String viewBook(@PathVariable long id, Model model) {
        BookDto book = bookService.findById(id)
                .map(BookDto::fromDomainObject)
                .orElseThrow(() -> new EntityNotFoundException("book id=%d not found".formatted(id)));
        List<CommentDto> comments = commentService.findByBookId(id).stream()
                .map(CommentDto::fromDomainObject)
                .collect(Collectors.toList());
        model.addAttribute("book", book);
        model.addAttribute("comments", comments);
        return "book-view";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable long id, Model model) {
        BookUpdateDto book = bookService.findById(id)
                .map(BookUpdateDto::fromDomainObject)
                .orElseThrow(() -> new EntityNotFoundException("book id=%d not found".formatted(id)));
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book-edit";
    }

    @PostMapping("/books/edit")
    public String saveBook(@Valid @ModelAttribute("book") BookUpdateDto bookUpdateDto,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "book-edit";
        }

        bookService.update(bookUpdateDto.getId(), bookUpdateDto.getTitle(),
                bookUpdateDto.getAuthorId(), bookUpdateDto.getGenreIds());
        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

}
