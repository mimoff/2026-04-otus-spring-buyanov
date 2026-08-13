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

    @NotBlank(message = "{name-field-should-not-be-blank}")
    @Size(min = 2, max = 10, message = "{name-field-should-has-expected-size}")
    private String fullName;
    /*    private List<String> hobby;

    public String hobbyAsString() {
        if (isEmpty(hobby)){
            return "";
        }
        return String.join(", ", hobby);
    }*/

    public Author toDomainObject(){
        return new Author(id, fullName);
    }

    public static AuthorDto fromDomainObject(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }
}
