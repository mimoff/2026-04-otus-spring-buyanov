package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/genres")
@CrossOrigin
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public List<GenreDto> findAll() {
        List<GenreDto> genres = genreService.findAll().stream()
                .map(GenreDto::fromDomainObject).toList();
        return genres;
    }

}
