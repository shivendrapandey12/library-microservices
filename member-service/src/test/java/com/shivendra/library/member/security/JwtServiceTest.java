package com.shivendra.library.member.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shivendra.library.member.model.Member;
import com.shivendra.library.member.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-unit-test-secret-1234";

    @Test
    void generatedTokenCarriesEmailAndRole() {
        JwtService jwt = new JwtService(SECRET, 60);
        Member member = new Member("Asha", "asha@example.com", "hash", Role.ADMIN);

        Claims claims = jwt.parse(jwt.generate(member));

        assertEquals("asha@example.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService issuer = new JwtService(SECRET, 60);
        JwtService other = new JwtService("a-totally-different-secret-a-totally-different-secret-99", 60);
        String token = issuer.generate(new Member("Asha", "asha@example.com", "hash", Role.USER));

        assertThrows(JwtException.class, () -> other.parse(token));
    }
}
