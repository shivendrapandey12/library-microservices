package com.shivendra.library.catalog.controller;

import com.shivendra.library.catalog.dto.BookRequest;
import com.shivendra.library.catalog.dto.BookResponse;
import com.shivendra.library.catalog.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<BookResponse> list(@RequestParam(required = false) String title) {
        return service.findAll(title);
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse create(@Valid @RequestBody BookRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/reserve")
    public BookResponse reserve(@PathVariable Long id) {
        return service.reserve(id);
    }

    @PostMapping("/{id}/release")
    public BookResponse release(@PathVariable Long id) {
        return service.release(id);
    }
}
