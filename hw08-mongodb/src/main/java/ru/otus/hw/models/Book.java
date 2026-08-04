package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Field(name = "title")
    private String title;

    @DocumentReference(lazy = true)
    @Field("author")
    private Author author;

    @DocumentReference(lazy = true)
    @Field("genres")
    private List<Genre> genres;
}
