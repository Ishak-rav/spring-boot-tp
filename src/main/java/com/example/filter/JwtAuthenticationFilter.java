package com.example.filter;

import com.example.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = getTokenFromRequest(request);

            if (StringUtils.hasText(token) && !authService.isTokenExpired(token)) {
                authenticateUser(token, request);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT du header Authorization
     * 
     * @param request la requête HTTP
     * @return le token JWT ou null s'il n'est pas présent
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * Configure l'authentification dans le contexte de sécurité Spring
     * 
     * @param token   le token JWT valide
     * @param request la requête HTTP
     */
    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            // Validation et extraction des informations du token
            Claims claims = authService.validateToken(token);
            String username = claims.getSubject();
            Integer userId = claims.get("userId", Integer.class);
            Boolean isAdmin = claims.get("admin", Boolean.class);

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(isAdmin != null && isAdmin ? "ADMIN" : "USER"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null,
                    authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            JwtAuthenticationDetails details = new JwtAuthenticationDetails(request, userId, isAdmin);
            authentication.setDetails(details);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Utilisateur authentifié: {} (Admin: {})", username, isAdmin);

        } catch (Exception e) {
            log.error("Erreur lors de la configuration de l'authentification: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Détermine si ce filtre doit être appliqué à la requête
     * 
     * @param request la requête HTTP
     * @return true si le filtre doit être appliqué
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth/login") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/api/tickets/unresolved") ||
                path.equals("/api/tickets/public");
    }

    /**
     * Classe pour stocker des détails supplémentaires d'authentification
     */
    public static class JwtAuthenticationDetails extends WebAuthenticationDetailsSource {
        private final Integer userId;
        private final Boolean isAdmin;
        private final String remoteAddress;
        private final String sessionId;

        public JwtAuthenticationDetails(HttpServletRequest request, Integer userId, Boolean isAdmin) {
            this.userId = userId;
            this.isAdmin = isAdmin;
            this.remoteAddress = request.getRemoteAddr();
            this.sessionId = request.getSession(false) != null ? request.getSession().getId() : null;
        }

        public Integer getUserId() {
            return userId;
        }

        public Boolean getIsAdmin() {
            return isAdmin;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }

        public String getSessionId() {
            return sessionId;
        }

        @Override
        public String toString() {
            return "JwtAuthenticationDetails{" +
                    "userId=" + userId +
                    ", isAdmin=" + isAdmin +
                    ", remoteAddress='" + remoteAddress + '\'' +
                    ", sessionId='" + sessionId + '\'' +
                    '}';
        }
    }
}