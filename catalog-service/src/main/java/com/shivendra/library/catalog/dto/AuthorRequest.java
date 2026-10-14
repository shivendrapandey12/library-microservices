package com.shivendra.library.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthorRequest(@NotBlank String name) {
}
