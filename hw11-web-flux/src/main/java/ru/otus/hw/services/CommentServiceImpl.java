package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Flux<CommentDto> findAll() {
        return commentRepository.findAll()
                .map(CommentDto::fromDomainObject);
    }

    @Override
    public Mono<CommentDto> findById(String id) {
        return commentRepository.findById(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Comment with id %s not found"
                        .formatted(id))))
                .map(CommentDto::fromDomainObject);
    }

    @Override
    public Flux<CommentDto> findAllByBookId(String bookId) {
        return commentRepository.findAllByBookId(bookId)
                .map(CommentDto::fromDomainObject);
    }

    @Override
    public Mono<CommentDto> insert(CommentUpdateDto commentUpdateDto) {
        return save(null, commentUpdateDto.getText(), commentUpdateDto.getBookId());
    }

    @Override
    public Mono<CommentDto> update(CommentUpdateDto commentUpdateDto) {
        return save(commentUpdateDto.getId(), commentUpdateDto.getText(), commentUpdateDto.getBookId());
    }

    private Mono<CommentDto> save(String id, String text, String bookId) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                        "Book with id %s not found".formatted(bookId))))
                .flatMap(book -> {
                    var comment = new Comment(id, text, book);

                    return commentRepository.save(comment);
                })
                .map(CommentDto::fromDomainObject);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return commentRepository.deleteById(id);
    }
}
