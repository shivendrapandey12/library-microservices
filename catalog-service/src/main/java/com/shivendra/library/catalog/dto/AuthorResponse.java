package com.shivendra.library.catalog.dto;

import com.shivendra.library.catalog.model.Author;

public record AuthorResponse(Long id, String name) {

    public static AuthorResponse from(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
