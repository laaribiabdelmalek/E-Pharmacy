package com.epharmacy.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de conversion de montants en toutes lettres (FrenchNumberToWords)")
public class FrenchNumberToWordsTest {

    @Test
    @DisplayName("Conversion de zéro dirham")
    void testZero() {
        assertEquals("Zéro Dirham", FrenchNumberToWords.convert(0.0));
        assertEquals("Zéro Dirham", FrenchNumberToWords.convert(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Conversion d'un dirham au singulier")
    void testUnDirham() {
        assertEquals("Un Dirham", FrenchNumberToWords.convert(1.0));
    }

    @Test
    @DisplayName("Conversion avec décimales (centimes)")
    void testAvecCentimes() {
        assertEquals("Cent cinquante Dirhams et soixante-quinze centimes",
                FrenchNumberToWords.convert(150.75));
        assertEquals("Cent cinquante Dirhams et soixante-quinze centimes",
                FrenchNumberToWords.convert(new BigDecimal("150.75")));
    }

    @Test
    @DisplayName("Conversion de montants ronds au pluriel")
    void testPluriel() {
        assertEquals("Mille deux cents Dirhams",
                FrenchNumberToWords.convert(1200.00));
    }
}
