package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<BookDto> findById(String id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Book with id %s not found".formatted(id))))
                .map(BookDto::fromDomainObject);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .map(BookDto::fromDomainObject);
    }

    @Override
    public Mono<BookDto> insert(BookUpdateDto bookUpdateDto) {
        return save(null, bookUpdateDto.getTitle(), bookUpdateDto.getAuthorId(),
                bookUpdateDto.getGenreIds());
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto bookUpdateDto) {
        return bookRepository.existsById(bookUpdateDto.getId())
                .flatMap(exists -> exists
                        ? save(bookUpdateDto.getId(), bookUpdateDto.getTitle(), bookUpdateDto.getAuthorId(),
                        bookUpdateDto.getGenreIds())
                        : Mono.error(new EntityNotFoundException("Book with id %s not found"
                        .formatted(bookUpdateDto.getId()))));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return bookRepository.deleteById(id);
    }

    private Mono<BookDto> save(String id, String title, String authorId, Set<String> genresIds) {
        if (isEmpty(genresIds)) {
            return Mono.error(new IllegalArgumentException("Genres ids must not be null"));
        }

        return Mono.zip(
                        getAuthor(authorId),
                        getGenres(genresIds))
                .flatMap(refs -> bookRepository.save(
                        new Book(id, title, refs.getT1(), refs.getT2())))
                .map(BookDto::fromDomainObject);
    }

    private Mono<Author> getAuthor(String authorId) {
        return authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(() ->
                        new EntityNotFoundException("Author with id %s not found".formatted(authorId))));
    }

    private Mono<List<Genre>> getGenres(Set<String> genreIds) {
        return genreRepository.findAllById(genreIds)
                .collectList()
                .flatMap(genres -> {
                    if (isEmpty(genres) || genreIds.size() != genres.size()) {
                        return Mono.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found".formatted(genreIds)));
                    }

                    return Mono.just(genres);
                });
    }
}
