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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "comments")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Comment {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Field(name = "text")
    private String text;

    @DocumentReference(lazy = true)
    @Field("book")
    private Book book;
}
