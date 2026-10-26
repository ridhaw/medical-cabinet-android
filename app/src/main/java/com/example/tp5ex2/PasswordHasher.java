package com.example.tp5ex2;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import android.util.Base64;

/**
 * Classe pour le hachage sécurisé des mots de passe avec SHA-256
 * Exercice 2 du TP9
 */
public class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Hache un mot de passe avec SHA-256
     * @param password Le mot de passe en clair
     * @return Le mot de passe haché en format hexadécimal
     */
    public static String hashPassword(String password) {
        try {
            // Créer une instance de MessageDigest avec SHA-256
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

            // Appliquer le hachage
            byte[] hash = digest.digest(password.getBytes());

            // Convertir en hexadécimal
            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Hache un mot de passe avec un salt pour plus de sécurité
     * @param password Le mot de passe en clair
     * @param salt Le salt à utiliser
     * @return Le mot de passe haché
     */
    public static String hashPasswordWithSalt(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

            // Ajouter le salt au mot de passe
            String saltedPassword = password + salt;

            // Appliquer le hachage
            byte[] hash = digest.digest(saltedPassword.getBytes());

            return bytesToHex(hash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Génère un salt aléatoire
     * @return Un salt en format Base64
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    /**
     * Vérifie si un mot de passe correspond à un hash
     * @param password Le mot de passe en clair
     * @param hashedPassword Le hash stocké
     * @return true si le mot de passe correspond
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = hashPassword(password);
        return hashedInput != null && hashedInput.equals(hashedPassword);
    }

    /**
     * Vérifie un mot de passe avec salt
     */
    public static boolean verifyPasswordWithSalt(String password, String salt, String hashedPassword) {
        String hashedInput = hashPasswordWithSalt(password, salt);
        return hashedInput != null && hashedInput.equals(hashedPassword);
    }

    /**
     * Convertit un tableau de bytes en chaîne hexadécimale
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}