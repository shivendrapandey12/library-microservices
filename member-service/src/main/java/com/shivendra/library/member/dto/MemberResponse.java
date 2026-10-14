package com.shivendra.library.member.dto;

import com.shivendra.library.member.model.Member;

public record MemberResponse(Long id, String name, String email, String role, boolean active) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole().name(),
                member.isActive());
    }
}
