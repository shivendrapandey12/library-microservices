package com.shivendra.library.circulation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** Resolved by service name through Eureka and load-balanced; wrapped by a circuit breaker. */
@FeignClient(name = "catalog-service", fallbackFactory = CatalogClientFallbackFactory.class)
public interface CatalogClient {

    @PostMapping("/api/books/{id}/reserve")
    BookInfo reserve(@PathVariable("id") Long id);

    @PostMapping("/api/books/{id}/release")
    BookInfo release(@PathVariable("id") Long id);
}
