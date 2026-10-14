package com.shivendra.library.circulation.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class CatalogClientFallbackFactory implements FallbackFactory<CatalogClient> {

    @Override
    public CatalogClient create(Throwable cause) {
        return new CatalogClient() {
            @Override
            public BookInfo reserve(Long id) {
                throw Downstream.translate("catalog-service", cause);
            }

            @Override
            public BookInfo release(Long id) {
                throw Downstream.translate("catalog-service", cause);
            }
        };
    }
}
