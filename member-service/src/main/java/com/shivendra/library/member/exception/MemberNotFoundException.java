package com.shivendra.library.member.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long id) {
        super("Member " + id + " not found");
    }
}
