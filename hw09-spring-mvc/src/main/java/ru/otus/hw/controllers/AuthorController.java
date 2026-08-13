package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorRepository repository;

    @GetMapping("/")
    public String listPage(Model model) {
        List<AuthorDto> authors = repository.findAll().stream()
                .map(AuthorDto::fromDomainObject).toList();
        model.addAttribute("authors", authors);
        return "author-list";
    }

    @GetMapping("/edit")
    public String editPage(@RequestParam("id") long id, Model model) {
        AuthorDto author = repository.findById(id)
                .map(AuthorDto::fromDomainObject)
                .orElseThrow(NotFoundException::new);
        model.addAttribute("author", author);
        return "edit";
    }

    @PostMapping("/edit")
    public String saveAuthor(@Valid @ModelAttribute("author") AuthorDto author,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "edit";
        }

        repository.save(author.toDomainObject());
        return "redirect:/";
    }
}
