package ru.otus.hw.converters;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Comment;

@Component
@AllArgsConstructor
public class CommentConverter {
    private final BookConverter bookConverter;

    public String commentToString(Comment comment) {
        var bookString = bookConverter.bookToString(comment.getBook());
        return "Id: %d, Text: %s, BookId: %d".formatted(comment.getId(), comment.getText(), comment.getBook().getId());
    }
}
