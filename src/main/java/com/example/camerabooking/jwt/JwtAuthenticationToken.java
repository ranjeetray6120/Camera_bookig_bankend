//package com.example.camerabooking.jwt;
//
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.core.userdetails.UserDetails;
//
//public class JwtAuthenticationToken extends AbstractAuthenticationToken {
//
//    private final UserDetails principal;
//    private final String token;
//
//    public JwtAuthenticationToken(UserDetails principal, String token) {
//        super(principal.getAuthorities());
//        this.principal = principal;
//        this.token = token;
//        setAuthenticated(true); // Mark as authenticated
//    }
//
//    @Override
//    public Object getCredentials() {
//        return token;
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return principal;
//    }
//
//    // Override toString() for better debugging
//    @Override
//    public String toString() {
//        return "JwtAuthenticationToken{principal=" + principal.getUsername() + ", token=****}";
//    }
//}
