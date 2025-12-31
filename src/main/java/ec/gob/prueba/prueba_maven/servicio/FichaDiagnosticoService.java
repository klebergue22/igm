/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.servicio;

/**
 *
 * @author GUERRA_KLEBER
 */
 

import ec.gob.prueba.prueba_maven.modelo.FichaDiagnostico;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.util.List;

@Stateless
public class FichaDiagnosticoService {

    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    public void eliminarPorFicha(Long idFicha) {
        em.createQuery("DELETE FROM FichaDiagnostico d WHERE d.ficha.idFicha = :id")
          .setParameter("id", idFicha)
          .executeUpdate();
    }

    public FichaDiagnostico guardar(FichaDiagnostico d) {
        if (d.getIdFichaDiag() == null) {
            em.persist(d);
            return d;
        }
        return em.merge(d);
    }

    public List<FichaDiagnostico> listarPorFicha(Long idFicha) {
        return em.createQuery(
                "SELECT d FROM FichaDiagnostico d WHERE d.ficha.idFicha = :id ORDER BY d.orden",
                FichaDiagnostico.class)
                .setParameter("id", idFicha)
                .getResultList();
    }
}
