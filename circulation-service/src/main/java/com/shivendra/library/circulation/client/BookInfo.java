package com.shivendra.library.circulation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookInfo(Long id, String title, int availableCopies) {
}
