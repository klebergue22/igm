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
import ec.gob.prueba.prueba_maven.modelo.FichaExamenComp;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Stateless
public class FichaExamenCompService {

    // IMPORTANTE: debe coincidir EXACTO con <persistence-unit name="pruebaPU"> en persistence.xml
    @PersistenceContext (unitName = "rhPU")
    private EntityManager em;

    public FichaExamenComp find(Long id) {
        if (id == null) return null;
        return em.find(FichaExamenComp.class, id);
    }

    public List<FichaExamenComp> listarPorFicha(Long idFicha) {
        TypedQuery<FichaExamenComp> q = em.createQuery(
                "SELECT e " +
                "FROM FichaExamenComp e " +
                "WHERE e.ficha.idFicha = :idFicha " +
                "ORDER BY e.nroFila, e.fechaExamen",
                FichaExamenComp.class
        );
        q.setParameter("idFicha", idFicha);
        return q.getResultList();
    }

    public FichaExamenComp guardar(FichaExamenComp e, String usuario) {
        if (e == null) return null;

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date ahora = new Date();

        // NUEVO
        if (e.getIdFichaExamen() == null) {
            e.setFCreacion(ahora);
            e.setUsrCreacion(usr);

            // si existe en tu entidad:
            e.setFActualizacion(null);
            e.setUsrActualizacion(null);

            em.persist(e);
            // em.flush(); // (opcional) si necesitas el ID inmediatamente
            return e;
        }

        // EXISTENTE
        e.setFActualizacion(ahora);
        e.setUsrActualizacion(usr);

        return em.merge(e);
    }

    public int eliminarPorFicha(Long idFicha) {
        return em.createQuery(
                "DELETE FROM FichaExamenComp e WHERE e.ficha.idFicha = :idFicha"
        ).setParameter("idFicha", idFicha)
         .executeUpdate();
    }
}
