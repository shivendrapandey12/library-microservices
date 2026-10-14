package com.shivendra.library.circulation.client;

import com.shivendra.library.circulation.exception.DownstreamUnavailableException;
import feign.FeignException;

final class Downstream {

    private Downstream() {
    }

    /**
     * A fallback runs for every failure, including real 404/409 answers from the other service.
     * Those must reach the caller unchanged, so only infrastructure failures (timeouts,
     * connection errors, open circuit) become a "service unavailable" error.
     */
    static RuntimeException translate(String service, Throwable cause) {
        if (cause instanceof FeignException fe && fe.status() >= 400 && fe.status() < 500) {
            return fe;
        }
        return new DownstreamUnavailableException(service, cause);
    }
}
