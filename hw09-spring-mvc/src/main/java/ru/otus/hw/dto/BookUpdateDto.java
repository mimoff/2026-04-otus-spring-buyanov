package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateDto {

    private long id;

//    @NotBlank(message = "{books.form.validation.title.notBlank}")
    private String title;

//    @NotNull(message = "Author cannot be null")
    private Long authorId;

//    @NotEmpty(message = "{books.form.validation.genres.notEmpty}")
    private Set<Long> genreIds;

    public static BookUpdateDto fromDomainObject(Book book) {
        return new BookUpdateDto(book.getId(),
                book.getTitle(),
                book.getAuthor().getId(),
                book.getGenres().stream()
                        .map(Genre::getId)
                        .collect(Collectors.toSet()));
    }
}