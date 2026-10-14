package com.shivendra.library.member.dto;

public record AuthResponse(String token, String role, Long memberId) {
}
