package com.wkom.banking.auth.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${jwt.secret:defaultSecretKeyWithAtLeast32CharactersForHmacSha256}")
    private String jwtSecret;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Mock authentication, typically validates against DB.
        if ("user".equals(request.username()) && "password".equals(request.password())) {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            String token = Jwts.builder()
                    .subject(request.username())
                    .claim("roles", List.of("ROLE_USER"))
                    .claim("customer_id", "CUST-12345")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                    .signWith(key)
                    .compact();
                    
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}

record LoginRequest(String username, String password) {}
