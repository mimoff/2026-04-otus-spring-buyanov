package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Data
@AllArgsConstructor
public class AuthorDto {

    private long id;

    private String fullName;

    public Author toDomainObject(){
        return new Author(id, fullName);
    }

    public static AuthorDto fromDomainObject(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }
}
