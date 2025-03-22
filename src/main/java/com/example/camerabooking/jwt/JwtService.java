//package com.example.camerabooking.jwt;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Service
//public class JwtService {
//
//    private static final String SECRET_STRING = "your_secret_key_which_should_be_long_enough_for_HS256";
//    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
//
//    // Extract username from JWT Token
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    // Extract a specific claim
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        Claims claims = extractAllClaims(token);
//        return claims != null ? claimsResolver.apply(claims) : null;
//    }
//
//    // Extract all claims from JWT token
//    private Claims extractAllClaims(String token) {
//        try {
//            System.out.println("🔍 Parsing Token: " + token);
//            return Jwts.parserBuilder()
//                    .setSigningKey(SECRET_KEY)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (ExpiredJwtException e) {
//            System.err.println("⏳ Token Expired: " + e.getMessage());
//        } catch (UnsupportedJwtException e) {
//            System.err.println("❌ Unsupported JWT: " + e.getMessage());
//        } catch (MalformedJwtException e) {
//            System.err.println("⚠️ Malformed JWT: " + e.getMessage());
//        } catch (SignatureException e) {
//            System.err.println("🔑 Invalid JWT Signature: " + e.getMessage());
//        } catch (IllegalArgumentException e) {
//            System.err.println("⚠️ Illegal Argument: " + e.getMessage());
//        } catch (Exception e) {
//            System.err.println("🚨 Unexpected Error while parsing JWT: " + e.getMessage());
//        }
//        return null;
//    }
//
//    // Generate JWT Token
//    public String generateToken(UserDetails userDetails) {
//        return generateToken(new HashMap<>(), userDetails);
//    }
//
//    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
//        return Jwts.builder()
//                .setClaims(extraClaims)
//                .setSubject(userDetails.getUsername())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
//                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // Validate JWT Token
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        Date expiration = extractClaim(token, Claims::getExpiration);
//        return expiration != null && expiration.before(new Date());
//    }
//}
