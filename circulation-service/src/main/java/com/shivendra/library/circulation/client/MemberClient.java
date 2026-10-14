package com.shivendra.library.circulation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", fallbackFactory = MemberClientFallbackFactory.class)
public interface MemberClient {

    @GetMapping("/api/members/{id}")
    MemberInfo getMember(@PathVariable("id") Long id);
}
