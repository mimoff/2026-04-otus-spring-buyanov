package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public BookDto findById(long id) {
        var book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
        return BookDto.fromDomainObject(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        var books = bookRepository.findAll().stream()
                .map(BookDto::fromDomainObject).collect(Collectors.toList());

        return books;
    }

    @Override
    @Transactional
    public BookDto insert(BookUpdateDto bookUpdateDto) {
        var book = save(0, bookUpdateDto.getTitle(), bookUpdateDto.getAuthorId(),
                bookUpdateDto.getGenreIds());
        return BookDto.fromDomainObject(book);
    }

    @Override
    @Transactional
    public BookDto update(BookUpdateDto bookUpdateDto) {
        var book = save(bookUpdateDto.getId(), bookUpdateDto.getTitle(), bookUpdateDto.getAuthorId(),
                bookUpdateDto.getGenreIds());
        return BookDto.fromDomainObject(book);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
        bookRepository.deleteById(id);
    }

    private Book save(long id, String title, long authorId, Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genres = genreRepository.findAllByIds(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        var book = new Book(id, title, author, genres);
        return bookRepository.save(book);
    }
}
