package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {

    private String token;
    private String type = "Bearer";
    private String pseudo;
    private Boolean admin;
    private String message;

    public AuthResponseDto(String token, String pseudo, Boolean admin) {
        this.token = token;
        this.pseudo = pseudo;
        this.admin = admin;
        this.message = "Connexion réussie";
    }

    public AuthResponseDto(String token, String pseudo, Boolean admin, String message) {
        this.token = token;
        this.pseudo = pseudo;
        this.admin = admin;
        this.message = message;
    }
}