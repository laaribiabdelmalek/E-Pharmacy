package com.epharmacy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton de connexion JDBC à la base MySQL e_pharmacie.
 * <p>
 * Fournit une instance unique de {@link Connection} partagée
 * par toutes les couches DAO de l'application.
 * </p>
 *
 * <pre>
 *     Connection conn = DBConnection.getInstance().getConnection();
 * </pre>
 *
 * @author E-Pharmacie
 * @version 1.0
 */
public class DBConnection {

    /** URL JDBC vers la base de données e_pharmacie */
    private static final String URL =
            "jdbc:mysql://localhost:3306/e_pharmacie?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    /** Nom d'utilisateur MySQL */
    private static final String USER = "root";

    /** Mot de passe MySQL */
    private static final String PASSWORD = "epharmacy123";

    /** Instance unique du singleton */
    private static DBConnection instance;

    /** Connexion JDBC sous-jacente */
    private Connection connection;

    /**
     * Constructeur privé — établit la connexion JDBC.
     *
     * @throws SQLException si la connexion échoue
     */
    private DBConnection() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Retourne l'instance unique de {@code DBConnection}.
     * <p>
     * Recrée automatiquement la connexion si elle a été fermée.
     * </p>
     *
     * @return instance singleton
     * @throws SQLException si la connexion échoue
     */
    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Retourne la connexion JDBC active.
     *
     * @return objet {@link Connection}
     */
    public Connection getConnection() {
        return connection;
    }
}
