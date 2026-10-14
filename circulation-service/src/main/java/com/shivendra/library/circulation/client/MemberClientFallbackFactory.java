package com.shivendra.library.circulation.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class MemberClientFallbackFactory implements FallbackFactory<MemberClient> {

    @Override
    public MemberClient create(Throwable cause) {
        return id -> {
            throw Downstream.translate("member-service", cause);
        };
    }
}
