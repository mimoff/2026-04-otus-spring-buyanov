package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public Flux<CommentDto> findAll() {
        return commentService.findAll();
    }

}
