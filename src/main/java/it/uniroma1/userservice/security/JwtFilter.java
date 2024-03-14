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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import it.uniroma1.userservice.entities.Role;
import it.uniroma1.userservice.entities.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Convert the claims into User object
     * 
     * @param claims The claims of the token
     * @return
     */
    private User createUserbyClaims(Claims claims) {
        if(claims != null) {
            
            User u = new User();
            u.setUsername(claims.get("username", String.class));
            u.setEnabled(claims.get("enabled", Boolean.class));
            Set<Role> authorities = new HashSet<Role>();
            List<String> stringaAthorities = claims.get("roles", List.class);
            for(String tmp : stringaAthorities) {
                Role r = new Role();
                r.setAuthority(tmp);
                authorities.add(r);
            }
            u.setAuthorities(authorities);
            return u;
        
        }
        return null;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            if(!isPublicUrl(request)) {
                String token = extractToken(request.getHeader("Authorization"));
                if(token != null) {
                    //1. Check token signature and extract all information
                    Claims claims = jwtUtil.extractAllClaims(token);
                    //2. Check if the token is not expired
                    boolean isTokenExpired = jwtUtil.isTokenExpired(token);
                    //3. Create the user and insert into Security Context
                    if(!isTokenExpired && claims != null) {
                        User u = createUserbyClaims(claims);
                        if(u != null) {
                               UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(u.getUsername(), null, u.getAuthorities());
                               SecurityContextHolder.getContext().setAuthentication(auth); //Authenticate the user
                        }
                    } else {
                        SecurityContextHolder.clearContext();
                        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");    
                    }
    
                } else {
                    SecurityContextHolder.clearContext();
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");
                }
            } 
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response); //Go to next filter chain
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Check if the request is allowed without login
     * 
     * @param request The HTTP Request
     * @return
     */
    private boolean isPublicUrl(HttpServletRequest request) {
        
        ArrayList<String> skippableUrls = new ArrayList<String>();
        skippableUrls.add("/");
        return skippableUrls.contains(request.getRequestURI());
    }
    
}