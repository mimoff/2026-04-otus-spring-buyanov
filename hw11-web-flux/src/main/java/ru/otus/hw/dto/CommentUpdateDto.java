package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.otus.hw.models.Comment;

@Data
@AllArgsConstructor
public class CommentUpdateDto {

    private String id;

    private String text;

    private String bookId;

    public static CommentUpdateDto fromDomainObject(Comment comment) {
        return new CommentUpdateDto(comment.getId(), comment.getText(), comment.getBook().getId());
    }
}
