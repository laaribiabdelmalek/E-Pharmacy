package com.epharmacy.ui.components;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Boîte de dialogue modale de base pour les formulaires.
 * <p>
 * Fournit une structure prête à l'emploi avec :
 * <ul>
 *   <li>Un panneau de formulaire central avec MigLayout</li>
 *   <li>Des boutons OK/Annuler en bas</li>
 *   <li>Des méthodes utilitaires pour messages d'erreur/succès et confirmation</li>
 *   <li>Un ajout simplifié de paires label/champ via {@link #addFormField}</li>
 *   <li>Fermeture par la touche Escape</li>
 * </ul>
 * </p>
 *
 * @author E-PHARMACIE
 * @version 1.0
 */
public class FormDialog extends JDialog {

    /** Panneau de contenu pour les champs de formulaire */
    private final JPanel formPanel;

    /** Bouton de validation */
    private final JButton okButton;

    /** Bouton d'annulation */
    private final JButton cancelButton;

    /** Action exécutée lors du clic sur OK */
    private Runnable okAction;

    /**
     * Construit une boîte de dialogue modale.
     *
     * @param parent la fenêtre parente
     * @param title  le titre de la boîte de dialogue
     * @param width  la largeur en pixels
     * @param height la hauteur en pixels
     */
    public FormDialog(JFrame parent, String title, int width, int height) {
        super(parent, title, true);
        setSize(width, height);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Layout principal
        JPanel mainPanel = new JPanel(new MigLayout("insets 20, fill", "[grow]", "[grow][]"));

        // Panneau de formulaire
        formPanel = new JPanel(new MigLayout("insets 0, wrap 2", "[120!]15[grow]", "[]10[]"));
        formPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 10 0 0 0", "push[]10[]", "[]"));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, getBorderColor()));

        cancelButton = new JButton("Annuler");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setPreferredSize(new Dimension(110, 35));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        okButton = new JButton("Valider");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okButton.setPreferredSize(new Dimension(110, 35));
        okButton.setBackground(new Color(33, 150, 243));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> {
            if (okAction != null) {
                okAction.run();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        mainPanel.add(scrollPane, "grow, wrap");
        mainPanel.add(buttonPanel, "growx, dock south");

        setContentPane(mainPanel);

        // Fermeture par Escape
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Enter déclenche OK
        getRootPane().setDefaultButton(okButton);
    }

    /**
     * Retourne le panneau de formulaire pour y ajouter des composants.
     *
     * @return le panneau de contenu
     */
    public JPanel getFormPanel() {
        return formPanel;
    }

    /**
     * Définit l'action exécutée lors du clic sur le bouton Valider.
     *
     * @param action le runnable à exécuter
     */
    public void setOkAction(Runnable action) {
        this.okAction = action;
    }

    /**
     * Ajoute une paire label/champ au formulaire.
     *
     * @param label le texte du label
     * @param field le composant de saisie
     */
    public void addFormField(String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        formPanel.add(lbl, "right");
        formPanel.add(field, "growx");
    }

    /**
     * Affiche un message d'erreur dans une boîte de dialogue.
     *
     * @param message le message d'erreur
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Affiche un message de succès dans une boîte de dialogue.
     *
     * @param message le message de succès
     */
    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Affiche une boîte de dialogue de confirmation.
     *
     * @param message le message de confirmation
     * @return {@code true} si l'utilisateur a confirmé, {@code false} sinon
     */
    public boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(
                this, message, "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    /**
     * Retourne le bouton OK pour personnalisation.
     *
     * @return le bouton de validation
     */
    public JButton getOkButton() {
        return okButton;
    }

    /**
     * Retourne le bouton Annuler pour personnalisation.
     *
     * @return le bouton d'annulation
     */
    public JButton getCancelButton() {
        return cancelButton;
    }

    /**
     * Retourne la couleur de bordure.
     *
     * @return la couleur de bordure
     */
    private Color getBorderColor() {
        Color c = UIManager.getColor("Component.borderColor");
        return c != null ? c : new Color(224, 224, 224);
    }
}
