package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Comment> findById(String id) {
        var comment = commentRepository.findById(id);

        if (!comment.isEmpty()) {
            comment.get().getBook().getAuthor().getFullName();
            comment.get().getBook().getGenres().size();
        }

        return comment;
    }

    @Override
    public List<Comment> findByBookId(String bookId) {
        var comments = commentRepository.findAllByBookId(bookId);

        for (var comment : comments) {
            comment.getBook().getAuthor().getFullName();
            comment.getBook().getGenres().size();
        }

        return comments;
    }

    @Override
    public Comment insert(String text, String bookId) {
        return save(null, text, bookId);
    }

    @Override
    public Comment update(String id, String text, String bookId) {
        return save(id, text, bookId);
    }

    private Comment save(String id, String text, String bookId) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));

        Comment comment = new Comment(id, text, book);
        return commentRepository.save(comment);
    }

    @Override
    public void deleteById(String id) {
        commentRepository.deleteById(id);
    }
}
