package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    @GetMapping("/books")
    public String listPage(Model model) {
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

    @PostMapping
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

    @GetMapping("/books/edit")
    public String editPage(@RequestParam("id") long id, Model model) {
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

        bookService.update(bookUpdateDto.getId(), bookUpdateDto.getTitle(), bookUpdateDto.getAuthorId(), bookUpdateDto.getGenreIds());
        return "redirect:/books";
    }

    @PostMapping("/books/delete")
    public String deleteBook(@RequestParam("id") long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

}
