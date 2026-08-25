package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    @Override
    public Optional<Book> findById(String id) {
        return bookRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        var books = bookRepository.findAll();

        for (var book : books) {
            book.getGenres().size();
        }

        return books;
    }

    @Override
    public Book insert(String title, String authorId, Set<String> genresIds) {
        return save(null, title, authorId, genresIds);
    }

    @Override
    public Book update(String id, String title, String authorId, Set<String> genresIds) {
        return save(id, title, authorId, genresIds);
    }

    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }

    @EventListener
    public void handleBeforeDelete(BeforeDeleteEvent<Book> event) {
        Document document = event.getDocument();
        if (document != null && document.containsKey("_id")) {
            Object bookId = document.get("_id");

            commentRepository.deleteAllByBookId(bookId.toString());
        }
    }

    private Book save(String id, String title, String authorId, Set<String> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(authorId)));
        var genres = genreRepository.findAllByIds(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        var book = new Book(id, title, author, genres);
        return bookRepository.save(book);
    }
}
