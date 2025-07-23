package com.example.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String[] passwords = {
                "admin123",
                "user123",
                "support123",
                "alice123",
                "bob123",
                "charlie123",
                "diana123",
                "tech_lead123"
        };

        System.out.println("=".repeat(60));
        System.out.println("GÉNÉRATION DES HASHES BCRYPT POUR LES SEEDERS");
        System.out.println("=".repeat(60));

        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.printf("%-15s -> %s%n", password, hash);
        }

        System.out.println("=".repeat(60));
        System.out.println("Copiez ces hashes dans votre fichier data.sql");
        System.out.println("=".repeat(60));

        System.out.println("\nSQL PRÊT À UTILISER :");
        System.out.println("---------------------");

        String[] users = {
                "admin", "user", "support", "alice", "bob", "charlie", "diana", "tech_lead"
        };

        boolean[] isAdmin = {
                true, false, true, false, false, false, false, true
        };

        for (int i = 0; i < users.length; i++) {
            String hash = encoder.encode(passwords[i]);
            System.out.printf("    (%d, '%s', '%s', %s)%s%n",
                    i + 1,
                    users[i],
                    hash,
                    isAdmin[i] ? "true" : "false",
                    i < users.length - 1 ? "," : ";");
        }
    }
}