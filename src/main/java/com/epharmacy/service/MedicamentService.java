package com.epharmacy.service;

import com.epharmacy.dao.MedicamentDAO;
import com.epharmacy.model.Medicament;
import java.util.List;

/**
 * Service métier pour la gestion du catalogue de médicaments.
 * @author E-PHARMACIE
 */
public class MedicamentService {
    private final MedicamentDAO dao = new MedicamentDAO();

    public List<Medicament> getTous() { return dao.findAll(); }
    public List<Medicament> rechercher(String keyword) { return dao.search(keyword); }
    public List<Medicament> getParStatut(String statut) { return dao.findByStatut(statut); }
    public List<Medicament> getAlertesStock() { return dao.findAlertesStock(); }
    public Medicament getParId(int id) { return dao.findById(id); }
    public int getNombreTotal() { return dao.count(); }
    public double getValeurStock() { return dao.getTotalValeurStock(); }

    public boolean ajouter(Medicament m) {
        if (m.getNomCommercial() == null || m.getNomCommercial().isBlank()) return false;
        if (m.getPrixVentePublic() < 0) return false;
        if (m.getSeuilMinimal() < 0) return false;
        return dao.insert(m);
    }

    public boolean modifier(Medicament m) {
        if (m.getNomCommercial() == null || m.getNomCommercial().isBlank()) return false;
        if (m.getPrixVentePublic() < 0) return false;
        return dao.update(m);
    }

    public boolean supprimer(int id) { return dao.delete(id); }
}
