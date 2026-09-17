package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/authors")
@CrossOrigin
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public List<AuthorDto> findAll() {
        List<AuthorDto> authors = authorService.findAll().stream()
                .map(AuthorDto::fromDomainObject).toList();
        return authors;
    }
}
