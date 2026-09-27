package com.eve.payment.security;

import org.springframework.stereotype.Service;

@Service
public class JwtService {
    public String generateToken(String subject) {
        return "token-for-" + subject;
    }

    public boolean validateToken(String token) {
        return token != null && token.startsWith("token-for-");
    }
}
