package com.shivendra.library.catalog.service;

import com.shivendra.library.catalog.dto.AuthorRequest;
import com.shivendra.library.catalog.dto.AuthorResponse;
import com.shivendra.library.catalog.model.Author;
import com.shivendra.library.catalog.repository.AuthorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {

    private final AuthorRepository authors;

    public AuthorService(AuthorRepository authors) {
        this.authors = authors;
    }

    @Transactional(readOnly = true)
    public List<AuthorResponse> findAll() {
        return authors.findAll().stream().map(AuthorResponse::from).toList();
    }

    @Transactional
    public AuthorResponse create(AuthorRequest request) {
        return AuthorResponse.from(authors.save(new Author(request.name())));
    }
}
