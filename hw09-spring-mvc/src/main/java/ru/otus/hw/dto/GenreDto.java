package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.otus.hw.models.Genre;

@Data
@AllArgsConstructor
public class GenreDto {

    private long id;

    @NotBlank(message = "{name-field-should-not-be-blank}")
    @Size(min = 2, max = 20, message = "{name-field-should-has-expected-size}")
    private String name;

    public Genre toDomainObject(){
        return new Genre(id, name);
    }

    public static GenreDto fromDomainObject(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}
