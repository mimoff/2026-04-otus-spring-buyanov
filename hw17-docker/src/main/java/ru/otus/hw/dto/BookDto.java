package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class BookDto {

    private long id;

    private String title;

    private AuthorDto author;

    private List<GenreDto> genres;

    public Book toDomainObject() {
        return new Book(id, title, author.toDomainObject(),
                genres.stream()
                        .map(GenreDto::toDomainObject)
                        .collect(Collectors.toList()));
    }

    public static BookDto fromDomainObject(Book book) {
        return new BookDto(book.getId(),
                book.getTitle(),
                AuthorDto.fromDomainObject(book.getAuthor()),
                book.getGenres().stream()
                        .map(genre -> GenreDto.fromDomainObject(genre))
                        .collect(Collectors.toList()));
    }
}
