package com.epharmacy.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Utilitaire de validation des entrées utilisateur.
 * <p>
 * Fournit des méthodes statiques pour valider les formats
 * d'email, de téléphone marocain, de nombres, ainsi que
 * des méthodes de formatage de prix et de dates.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public final class ValidationUtils {

    /** Pattern de validation d'adresse email simple */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Pattern de validation de numéro de téléphone marocain.
     * Formats acceptés :
     * <ul>
     *     <li>06XXXXXXXX, 07XXXXXXXX, 05XXXXXXXX, 08XXXXXXXX (10 chiffres)</li>
     *     <li>+212 6XXXXXXXX, +212 7XXXXXXXX, +212 5XXXXXXXX, +212 8XXXXXXXX</li>
     *     <li>00212 6XXXXXXXX, 00212 7XXXXXXXX, etc.</li>
     * </ul>
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(0[5-8]\\d{8}|\\+212\\s?[5-8]\\d{8}|00212\\s?[5-8]\\d{8})$"
    );

    /** Formateur de date au format français dd/MM/yyyy */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Formateur de date et heure au format français dd/MM/yyyy HH:mm */
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Constructeur privé pour empêcher l'instanciation.
     */
    private ValidationUtils() {
        throw new UnsupportedOperationException("Classe utilitaire non instanciable");
    }

    /**
     * Vérifie si une chaîne est {@code null} ou vide (après trim).
     *
     * @param s la chaîne à vérifier
     * @return {@code true} si la chaîne est {@code null} ou vide
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Vérifie si une adresse email a un format valide.
     *
     * @param email l'adresse email à valider
     * @return {@code true} si le format est valide, {@code false} sinon
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Vérifie si un numéro de téléphone est au format marocain valide.
     * <p>
     * Les espaces et tirets sont supprimés avant la validation.
     * </p>
     *
     * @param phone le numéro de téléphone à valider
     * @return {@code true} si le format est valide, {@code false} sinon
     */
    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        // Supprimer les espaces et tirets pour la validation
        String cleaned = phone.trim().replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    /**
     * Vérifie si une chaîne représente un nombre décimal positif.
     *
     * @param s la chaîne à vérifier
     * @return {@code true} si la chaîne est un nombre positif, {@code false} sinon
     */
    public static boolean isPositiveNumber(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }
        try {
            double value = Double.parseDouble(s.trim());
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Vérifie si une chaîne représente un entier positif.
     *
     * @param s la chaîne à vérifier
     * @return {@code true} si la chaîne est un entier positif, {@code false} sinon
     */
    public static boolean isPositiveInteger(String s) {
        if (isNullOrEmpty(s)) {
            return false;
        }
        try {
            int value = Integer.parseInt(s.trim());
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Formate un prix en Dirhams marocains.
     *
     * @param price le montant à formater
     * @return le prix formaté (ex: "125.50 DH")
     */
    public static String formatPrice(double price) {
        return String.format(java.util.Locale.US, "%.2f DH", price);
    }

    /**
     * Formate un montant BigDecimal en Dirhams marocains.
     *
     * @param price le montant à formater
     * @return le prix formaté
     */
    public static String formatPrice(java.math.BigDecimal price) {
        if (price == null) {
            return "0.00 DH";
        }
        return String.format(java.util.Locale.US, "%.2f DH", price.doubleValue());
    }

    /**
     * Formate une date au format français dd/MM/yyyy.
     *
     * @param date la date à formater
     * @return la date formatée (ex: "15/03/2024")
     * @throws IllegalArgumentException si la date est {@code null}
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La date ne peut pas être null");
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formate une date et heure au format français dd/MM/yyyy HH:mm.
     *
     * @param dt la date et heure à formater
     * @return la date/heure formatée (ex: "15/03/2024 14:30")
     * @throws IllegalArgumentException si la date/heure est {@code null}
     */
    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) {
            throw new IllegalArgumentException("La date/heure ne peut pas être null");
        }
        return dt.format(DATETIME_FORMATTER);
    }
}
