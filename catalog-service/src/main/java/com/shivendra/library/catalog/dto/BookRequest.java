package com.shivendra.library.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequest(
        @NotBlank String title,
        @NotBlank String isbn,
        @NotNull Long authorId,
        @Min(1) int totalCopies) {
}
