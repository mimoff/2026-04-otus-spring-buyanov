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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.BookService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/books")
@CrossOrigin
public class BookController {

    private final BookService bookService;

    @GetMapping
    public Flux<BookDto> findAll()  {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<BookDto> findById(@PathVariable String id) {
        return bookService.findById(id);
    }

    @PostMapping
    public Mono<ResponseEntity<BookDto>> createBook(@Valid @RequestBody BookUpdateDto bookUpdateDto) {
        return bookService.insert(bookUpdateDto).map(
                newBook -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(newBook)
        );
    }

    @PutMapping("/{id}")
    public Mono<BookDto> saveBook(@PathVariable String id, @Valid @RequestBody BookUpdateDto bookUpdateDto) {
        bookUpdateDto.setId(id);

        return bookService.update(bookUpdateDto);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable String id) {
        return bookService.deleteById(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}

