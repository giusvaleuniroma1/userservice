/**
 * MIT No Attribution
 *
 *Copyright 2024 Giuseppe Valente <valentepeppe@gmail.com>
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy of this
 *software and associated documentation files (the "Software"), to deal in the Software
 *without restriction, including without limitation the rights to use, copy, modify,
 *merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 *permit persons to whom the Software is furnished to do so.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 *PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package it.uniroma1.userservice.security;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class JwtUtil {

    @Value("${jwt.symmetric.key}")
    private String secretKey;

    public String extractUsername(String token) throws UnsupportedEncodingException {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) throws UnsupportedEncodingException {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws UnsupportedEncodingException {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) throws UnsupportedEncodingException {
        SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        return Jwts.parser().verifyWith(secret_key).build().parseSignedClaims(token).getPayload();
    }

    public Boolean isTokenExpired(String token) throws UnsupportedEncodingException {
        return extractExpiration(token).before(new Date());
    }

    /**
     * In the JWT will be insert:
     * 1) The username
     * 2) The list of authorities
     * 3) The information about the enable
     * 
     * The JWT will be sign with these information
     * 
     * @param user The user to sign in JWT
     * @return The Signed JWT
     * @throws Exception
     */
    public String generateToken(UserDetails user) throws Exception {

        Map<String, Object> claims = new HashMap<>();
        if(user == null || user.getUsername() == null || user.getUsername().trim().equals("")) {
            throw new Exception("Invalid user");
        }

        claims.put("username", user.getUsername());
        claims.put("enabled", user.isEnabled());
        List<String> roles = new ArrayList<String>();
        for(GrantedAuthority ga : user.getAuthorities()) {
            if(ga != null) {
                roles.add(ga.getAuthority());
            }
        }

        claims.put("roles", roles);
        return createToken(claims, "user");
    }

    /**
     * Sign the JWT token
     * 
     * @param claims The claims
     * @param subject The subject
     * @return The signed JWT
     * @throws UnsupportedEncodingException
     */
    public String createToken(Map<String, Object> claims, String subject) throws UnsupportedEncodingException {

        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 2)) // 2 minutes validation
                .signWith(key).compact();
    }

    public Boolean validateToken(String token, UserDetails user) throws UnsupportedEncodingException {
        final String userName = extractUsername(token);
        return (userName.equals(user.getUsername()) && !isTokenExpired(token));
    }
}