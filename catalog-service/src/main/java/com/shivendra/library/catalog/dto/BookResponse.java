package com.shivendra.library.catalog.dto;

import com.shivendra.library.catalog.model.Book;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        String author,
        int totalCopies,
        int availableCopies) {

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getAuthor().getName(),
                book.getTotalCopies(),
                book.getAvailableCopies());
    }
}
