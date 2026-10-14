package com.shivendra.library.circulation.dto;

import jakarta.validation.constraints.NotNull;

public record IssueRequest(@NotNull Long memberId, @NotNull Long bookId) {
}
