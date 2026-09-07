package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
@CrossOrigin
public class BookController {

    private final BookService bookService;

    @GetMapping
    public List<BookDto> findAll()  {
        List<BookDto> books = bookService.findAll().stream()
                .map(BookDto::fromDomainObject).toList();
        return books;
    }

    @GetMapping("/{id}")
    public BookDto findById(@PathVariable Long id) {
        return BookDto.fromDomainObject(bookService.findById(id).orElseThrow());
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookUpdateDto bookUpdateDto) {
        var newBook = bookService.insert(bookUpdateDto.getTitle(),
                bookUpdateDto.getAuthorId(), bookUpdateDto.getGenreIds());
        var responseBook = BookDto.fromDomainObject(newBook);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBook);
    }

    @PutMapping("/{id}")
    public BookDto saveBook(@PathVariable Long id, @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        var savedBook = bookService.update(id, bookUpdateDto.getTitle(),
                bookUpdateDto.getAuthorId(), bookUpdateDto.getGenreIds());

        var responseBook = BookDto.fromDomainObject(savedBook);
        return responseBook;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}

