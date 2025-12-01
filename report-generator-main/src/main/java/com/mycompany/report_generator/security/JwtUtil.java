package com.mycompany.report_generator.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    // IMPORTANT: Cheie secretă extinsă (min. 512 biți / 64 octeți) pentru HS512.
    // Această cheie trebuie să fie lungă și secretă.
    private final String SECRET =
        "THIS_IS_A_VERY_LONG_AND_SECURE_SECRET_KEY_FOR_JWT_HS512_ALGORITHM_MINIMUM_512_BITS_REQUIRED";

    private Key getSigningKey() {
        // Asigurăm că cheia este decodată din Base64 pentru a obține formatul binar
        // Deși string-ul de mai sus nu este Base64, vom folosi o cheie generată pe baza string-ului.
        // Pentru o cheie hardcodată, o soluție mai simplă și mai robustă este să lăsăm Spring să o decodeze.
        // O metodă mai sigură ar fi:
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Extrage username-ul (doctorCode) din token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrage data expirării
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(
        String token,
        Function<Claims, T> claimsResolver
    ) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // Folosim `getSigningKey()` în locul metodei deprecated `setSigningKey`
        return Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generarea Tokenului
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            // Token valid 10 ore
            .expiration(
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)
            )
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }

    // Validarea Tokenului
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (
            username.equals(userDetails.getUsername()) && !isTokenExpired(token)
        );
    }
}
