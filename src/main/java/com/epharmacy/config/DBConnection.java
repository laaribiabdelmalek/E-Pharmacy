package com.epharmacy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestionnaire de connexions JDBC via un pool HikariCP.
 * <p>
 * La configuration est lue dans l'ordre suivant :
 * <ol>
 *     <li>Variables d'environnement ({@code EPHARMACY_DB_URL}, {@code EPHARMACY_DB_USER}, {@code EPHARMACY_DB_PASS})</li>
 *     <li>Fichier {@code config.properties} à la racine du projet</li>
 *     <li>Valeurs par défaut codées en dur</li>
 * </ol>
 * Chaque appel à {@link #getConnection()} retourne une <strong>nouvelle</strong> connexion
 * empruntée au pool. L'appelant <strong>doit</strong> la fermer via {@code try-with-resources}.
 * </p>
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public final class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    /** URL JDBC par défaut */
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/e_pharmacie?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "epharmacy123";

    /** Pool de connexions (initialisé une seule fois) */
    private static volatile HikariDataSource dataSource;

    private static final DBConnection INSTANCE = new DBConnection();

    private DBConnection() {
    }

    /**
     * Accesseur singleton pour rétrocompatibilité.
     * @return instance unique de DBConnection
     */
    public static DBConnection getInstance() {
        return INSTANCE;
    }

    /**
     * Retourne une connexion JDBC empruntée au pool HikariCP.
     * <p>
     * L'appelant <strong>doit</strong> fermer la connexion retournée,
     * idéalement via {@code try-with-resources}.
     * </p>
     *
     * @return une connexion JDBC active
     * @throws SQLException si le pool ne peut pas fournir de connexion
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            synchronized (DBConnection.class) {
                if (dataSource == null) {
                    initDataSource();
                }
            }
        }
        return dataSource.getConnection();
    }

    /**
     * Ferme le pool de connexions. Appelé lors de l'arrêt de l'application.
     */
    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Fermeture du pool de connexions HikariCP.");
            dataSource.close();
            dataSource = null;
        }
    }

    /**
     * Initialise le pool HikariCP avec la configuration résolue.
     */
    private static void initDataSource() {
        String url = resolveProperty("EPHARMACY_DB_URL", "db.url", DEFAULT_URL);
        String user = resolveProperty("EPHARMACY_DB_USER", "db.user", DEFAULT_USER);
        String pass = resolveProperty("EPHARMACY_DB_PASS", "db.password", DEFAULT_PASS);

        // S'assurer que les paramètres UTF-8 sont présents dans l'URL
        if (!url.contains("useUnicode=true")) {
            url += (url.contains("?") ? "&" : "?") + "useUnicode=true&characterEncoding=UTF-8";
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300_000);
        config.setConnectionTimeout(30_000);
        config.setMaxLifetime(1_800_000);
        config.setPoolName("EPharmaciePool");

        // Propriétés recommandées pour MySQL
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        logger.info("Pool HikariCP initialisé — URL : {}", url.replaceAll("password=[^&]*", "password=***"));
    }

    /**
     * Résout une propriété en cherchant d'abord dans les variables d'environnement,
     * puis dans le fichier {@code config.properties}, puis utilise la valeur par défaut.
     */
    private static String resolveProperty(String envKey, String propKey, String defaultValue) {
        // 1. Variable d'environnement
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            logger.debug("Configuration {} lue depuis la variable d'environnement.", envKey);
            return value;
        }

        // 2. Fichier config.properties
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                props.load(fis);
            }
            value = props.getProperty(propKey);
            if (value != null && !value.isBlank()) {
                logger.debug("Configuration {} lue depuis config.properties.", propKey);
                return value;
            }
        } catch (IOException e) {
            // Fichier absent — ce n'est pas une erreur
            logger.debug("Fichier config.properties introuvable, utilisation des valeurs par défaut.");
        }

        // 3. Valeur par défaut
        return defaultValue;
    }
}
