package com.example.service;

import com.example.dao.UtilisateurDao;
import com.example.dto.AuthResponseDto;
import com.example.dto.LoginRequestDto;
import com.example.model.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurDao utilisateurDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}")
    private int jwtExpirationInSeconds;

    /**
     * Génère une clé secrète pour JWT
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Authentifie un utilisateur et génère un token JWT
     */
    public AuthResponseDto login(LoginRequestDto loginRequest) {
        Optional<Utilisateur> utilisateurOpt = utilisateurDao.findByPseudo(loginRequest.getPseudo());

        if (utilisateurOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), utilisateur.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        String token = generateToken(utilisateur);
        return new AuthResponseDto(token, utilisateur.getPseudo(), utilisateur.isAdmin());
    }

    /**
     * Génère un token JWT pour un utilisateur
     */
    public String generateToken(Utilisateur utilisateur) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInSeconds * 1000L);

        return Jwts.builder()
                .subject(utilisateur.getPseudo())
                .claim("userId", utilisateur.getId())
                .claim("admin", utilisateur.isAdmin())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valide un token JWT et retourne les claims
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Token invalide", e);
        }
    }

    /**
     * Extrait le pseudo de l'utilisateur du token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Extrait l'ID de l'utilisateur du token
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * Vérifie si l'utilisateur est admin à partir du token
     */
    public Boolean isAdminFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get("admin", Boolean.class);
    }

    /**
     * Vérifie si un token est expiré
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Hache un mot de passe
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Vérifie si un mot de passe correspond au hash
     */
    public boolean verifyPassword(String password, String hashedPassword) {
        return passwordEncoder.matches(password, hashedPassword);
    }

    /**
     * Récupère un utilisateur à partir du token
     */
    public Utilisateur getUserFromToken(String token) {
        String pseudo = getUsernameFromToken(token);
        return utilisateurDao.findByPseudo(pseudo)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /**
     * Vérifie si l'utilisateur a les droits d'administration
     */
    public boolean isUserAdmin(String token) {
        return Boolean.TRUE.equals(isAdminFromToken(token));
    }

    /**
     * Vérifie si l'utilisateur peut accéder à une ressource
     */
    public boolean canAccessResource(String token, Integer resourceUserId) {
        Integer userId = getUserIdFromToken(token);
        boolean isAdmin = isUserAdmin(token);

        // Un admin peut tout faire, un utilisateur ne peut accéder qu'à ses propres
        // ressources
        return isAdmin || userId.equals(resourceUserId);
    }

    /**
     * Crée un utilisateur avec mot de passe haché
     */
    public Utilisateur createUser(String pseudo, String password, Boolean admin) {
        if (utilisateurDao.existsByPseudo(pseudo)) {
            throw new RuntimeException("Un utilisateur avec ce pseudo existe déjà");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPseudo(pseudo);
        utilisateur.setPassword(hashPassword(password));
        utilisateur.setAdmin(admin != null ? admin : false);

        return utilisateurDao.save(utilisateur);
    }

    /**
     * Met à jour le mot de passe d'un utilisateur
     */
    public void updatePassword(Integer userId, String oldPassword, String newPassword) {
        Utilisateur utilisateur = utilisateurDao.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!verifyPassword(oldPassword, utilisateur.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        utilisateur.setPassword(hashPassword(newPassword));
        utilisateurDao.save(utilisateur);
    }

    /**
     * Renouvelle un token (refresh)
     */
    public AuthResponseDto refreshToken(String token) {
        if (isTokenExpired(token)) {
            throw new RuntimeException("Token expiré");
        }

        Utilisateur utilisateur = getUserFromToken(token);
        String newToken = generateToken(utilisateur);

        return new AuthResponseDto(newToken, utilisateur.getPseudo(), utilisateur.isAdmin(), "Token renouvelé");
    }
}