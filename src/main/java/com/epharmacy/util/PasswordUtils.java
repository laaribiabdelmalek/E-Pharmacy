package com.epharmacy.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitaire de hachage et vérification de mots de passe
 * utilisant l'algorithme BCrypt.
 * <p>
 * BCrypt intègre automatiquement un sel aléatoire dans le hash,
 * offrant une protection robuste contre les attaques par dictionnaire
 * et par force brute.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public final class PasswordUtils {

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private PasswordUtils() {
        throw new UnsupportedOperationException("Classe utilitaire non instanciable");
    }

    /**
     * Hache un mot de passe en clair avec BCrypt.
     *
     * @param plainText le mot de passe en clair à hacher
     * @return le hash BCrypt du mot de passe
     * @throws IllegalArgumentException si le mot de passe est {@code null} ou vide
     */
    public static String hashPassword(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    /**
     * Vérifie si un mot de passe en clair correspond à un hash BCrypt.
     *
     * @param plainText le mot de passe en clair à vérifier
     * @param hashed    le hash BCrypt stocké en base de données
     * @return {@code true} si le mot de passe correspond, {@code false} sinon
     */
    public static boolean checkPassword(String plainText, String hashed) {
        if (plainText == null || hashed == null) {
            return false;
        }
        return BCrypt.checkpw(plainText, hashed);
    }
}
