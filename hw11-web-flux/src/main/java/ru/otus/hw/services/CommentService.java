package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;

public interface CommentService {
    Flux<CommentDto> findAll();

    Mono<CommentDto> findById(String id);

    Flux<CommentDto> findAllByBookId(String bookId);

    Mono<CommentDto> insert(CommentUpdateDto commentUpdateDto);

    Mono<CommentDto> update(CommentUpdateDto commentUpdateDto);

    Mono<Void> deleteById(String id);
}
