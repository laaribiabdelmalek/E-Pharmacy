package com.epharmacy.test;

import com.epharmacy.security.AccessControlTest;
import com.epharmacy.service.AuthServiceLockoutTest;
import com.epharmacy.service.UtilisateurServiceTest;
import com.epharmacy.util.FrenchNumberToWordsTest;

import java.lang.reflect.Method;

/**
 * Runner programmatique pour les tests unitaires JUnit 5 (sans dépendre de plugins Maven distants).
 *
 * @author E-PHARMACIE
 */
public class JUnitTestRunner {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("  E-PHARMACIE — Exécution Programmatique des Tests Unitaires JUnit 5");
        System.out.println("================================================================================");

        int total = 0;
        int passed = 0;
        int failed = 0;

        Class<?>[] testClasses = new Class<?>[]{
                AccessControlTest.class,
                UtilisateurServiceTest.class,
                FrenchNumberToWordsTest.class,
                AuthServiceLockoutTest.class
        };

        for (Class<?> clazz : testClasses) {
            System.out.println("\nClasse de test : " + clazz.getSimpleName());
            Object instance;
            try {
                instance = clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("Impossible d'instancier " + clazz.getName() + " : " + e.getMessage());
                continue;
            }

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(org.junit.jupiter.api.Test.class)) {
                    total++;
                    try {
                        // Exécuter BeforeEach si présent
                        for (Method setup : clazz.getDeclaredMethods()) {
                            if (setup.isAnnotationPresent(org.junit.jupiter.api.BeforeEach.class)) {
                                setup.setAccessible(true);
                                setup.invoke(instance);
                            }
                        }

                        m.setAccessible(true);
                        m.invoke(instance);

                        // Exécuter AfterEach si présent
                        for (Method tearDown : clazz.getDeclaredMethods()) {
                            if (tearDown.isAnnotationPresent(org.junit.jupiter.api.AfterEach.class)) {
                                tearDown.setAccessible(true);
                                tearDown.invoke(instance);
                            }
                        }

                        System.out.println("  [PASS] " + m.getName());
                        passed++;
                    } catch (Throwable t) {
                        System.out.println("  [FAIL] " + m.getName() + " -> " + (t.getCause() != null ? t.getCause().getMessage() : t.getMessage()));
                        failed++;
                    }
                }
            }
        }

        System.out.println("\n================================================================================");
        System.out.println("  RÉSULTATS DES TESTS UNITAIRES");
        System.out.println("  Total : " + total + " | Succès : " + passed + " | Échecs : " + failed);
        System.out.println("================================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
