package com.tegel.util;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    // secure key
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // toke expire time (7 days)
    private static long EXPIRATION_TIME = 86400000 * 7;

    // create token with subject (userID) and claims (role, email)
    public static String generateToken(String subject, Map<String, Object> claims, long expirationTime) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // check token and return claim values
    public static Jws<Claims> validateToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);
    }

    // try to get the claims without crashing
    public static Claims getClaims(String token) {
        try {
            return validateToken(token).getBody();
        } catch (JwtException e) {
            return null;
        }
    }
}
