package com.epharmacy.exception;

/**
 * Exception levée lorsque le stock est insuffisant pour honorer une vente.
 *
 * @author E-PHARMACIE
 * @version 3.0
 */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(int idMedicament, int demande, int disponible) {
        super(String.format("Stock insuffisant pour le médicament #%d. Demandé : %d, Disponible : %d", idMedicament, demande, disponible));
    }
}
