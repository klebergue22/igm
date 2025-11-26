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
/*
 * Service para FichaRiesgo (Step 2)
 */
 

import ec.gob.prueba.prueba_maven.modelo.FichaRiesgo;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateless
public class FichaRiesgoService {

    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    /**
     * Crear o actualizar una ficha de riesgos.
     */
    public FichaRiesgo guardar(FichaRiesgo fr) {
        if (fr == null) {
            return null;
        }

        if (fr.getIdFichaRiesgo() == null) {
            em.persist(fr);
            return fr;
        } else {
            return em.merge(fr);
        }
    }

    /**
     * Buscar por ID.
     */
    public FichaRiesgo buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(FichaRiesgo.class, id);
    }

    /**
     * Buscar riesgos por ID_FICHA.
     * (Por si luego quieres cargar/editar el Step 2 de una ficha existente).
     */
    public List<FichaRiesgo> buscarPorFicha(Long idFicha) {
        if (idFicha == null) {
            return null;
        }
        TypedQuery<FichaRiesgo> q = em.createQuery(
                "SELECT r FROM FichaRiesgo r WHERE r.ficha.idFicha = :idFicha ORDER BY r.idFichaRiesgo",
                FichaRiesgo.class);
        q.setParameter("idFicha", idFicha);
        return q.getResultList();
    }
}

