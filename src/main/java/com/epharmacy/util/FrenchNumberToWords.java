package com.epharmacy.util;

/**
 * Convertit un nombre en toutes lettres en français.
 * Utilisé pour les factures officielles.
 * @author E-PHARMACIE
 */
public final class FrenchNumberToWords {

    private static final String[] UNITS = {
        "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
        "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize",
        "dix-sept", "dix-huit", "dix-neuf"
    };

    private static final String[] TENS = {
        "", "dix", "vingt", "trente", "quarante", "cinquante",
        "soixante", "soixante", "quatre-vingt", "quatre-vingt"
    };

    private FrenchNumberToWords() {
        throw new UnsupportedOperationException("Classe utilitaire non instanciable");
    }

    /**
     * Convertit un montant BigDecimal en dirhams en toutes lettres.
     * @param amount le montant en BigDecimal
     * @return la chaîne en lettres
     */
    public static String convert(java.math.BigDecimal amount) {
        if (amount == null) return "Zéro Dirham";
        return convert(amount.doubleValue());
    }

    /**
     * Convertit un montant en dirhams en toutes lettres.
     * @param amount le montant (ex: 150.75)
     * @return la chaîne en lettres (ex: "Cent cinquante Dirhams et soixante-quinze centimes")
     */
    public static String convert(double amount) {
        long intPart = (long) Math.floor(Math.abs(amount));
        int centimes = (int) Math.round((Math.abs(amount) - intPart) * 100);

        StringBuilder sb = new StringBuilder();

        if (intPart == 0) {
            sb.append("Zéro");
        } else {
            sb.append(capitalize(convertNumber(intPart)));
        }
        sb.append(" Dirham");
        if (intPart > 1) sb.append("s");

        if (centimes > 0) {
            sb.append(" et ").append(convertNumber(centimes)).append(" centime");
            if (centimes > 1) sb.append("s");
        }

        return sb.toString();
    }

    private static String convertNumber(long number) {
        if (number == 0) return "zéro";
        if (number < 0) return "moins " + convertNumber(-number);

        StringBuilder result = new StringBuilder();

        if (number >= 1_000_000_000) {
            long milliards = number / 1_000_000_000;
            result.append(convertNumber(milliards)).append(" milliard");
            if (milliards > 1) result.append("s");
            number %= 1_000_000_000;
            if (number > 0) result.append(" ");
        }

        if (number >= 1_000_000) {
            long millions = number / 1_000_000;
            result.append(convertNumber(millions)).append(" million");
            if (millions > 1) result.append("s");
            number %= 1_000_000;
            if (number > 0) result.append(" ");
        }

        if (number >= 1_000) {
            long milliers = number / 1_000;
            if (milliers == 1) {
                result.append("mille");
            } else {
                result.append(convertNumber(milliers)).append(" mille");
            }
            number %= 1_000;
            if (number > 0) result.append(" ");
        }

        if (number >= 100) {
            long centaines = number / 100;
            if (centaines == 1) {
                result.append("cent");
            } else {
                result.append(UNITS[(int) centaines]).append(" cent");
            }
            number %= 100;
            if (number == 0 && centaines > 1) {
                result.append("s");
            } else if (number > 0) {
                result.append(" ");
            }
        }

        if (number > 0) {
            result.append(convertTens((int) number));
        }

        return result.toString().trim();
    }

    private static String convertTens(int number) {
        if (number < 20) {
            return UNITS[number];
        }

        int tensIndex = number / 10;
        int unit = number % 10;

        if (tensIndex == 7 || tensIndex == 9) {
            int remainder = number - (tensIndex - 1) * 10;
            if (remainder < 20) {
                String base = TENS[tensIndex];
                if (remainder == 11 && tensIndex == 7) {
                    return base + " et onze";
                }
                return base + "-" + UNITS[remainder];
            }
        }

        String result = TENS[tensIndex];
        if (unit == 0) {
            if (tensIndex == 8) return result + "s";
            return result;
        }

        if (unit == 1 && tensIndex != 8 && tensIndex != 9) {
            result += " et un";
        } else {
            result += "-" + UNITS[unit];
        }

        return result;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
