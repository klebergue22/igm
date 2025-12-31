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
import ec.gob.prueba.prueba_maven.modelo.FichaActLaboral;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Stateless
public class FichaActLaboralService {

    // IMPORTANTE: SOLO unitName (debe existir en persistence.xml)
    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    public FichaActLaboral find(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(FichaActLaboral.class, id);
    }

    public List<FichaActLaboral> listarPorFicha(Long idFicha) {
        TypedQuery<FichaActLaboral> q = em.createQuery(
                "SELECT a FROM FichaActLaboral a "
                + "WHERE a.ficha.idFicha = :idFicha "
                + "ORDER BY a.nroFila",
                FichaActLaboral.class
        );
        q.setParameter("idFicha", idFicha);
        return q.getResultList();
    }

    public FichaActLaboral guardar(FichaActLaboral a, String usuario) {
        if (a == null) {
            return null;
        }

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date ahora = new Date();

        if (a.getIdFichaActLab()== null) {
            a.setFCreacion(ahora);
            a.setUsrCreacion(usr);
            em.persist(a);
            return a;
        } else {
            a.setFActualizacion(ahora);
            a.setUsrActualizacion(usr);
            return em.merge(a);
        }
    }

    public int eliminarPorFicha(Long idFicha) {
        return em.createQuery(
                "DELETE FROM FichaActLaboral a WHERE a.ficha.idFicha = :idFicha"
        ).setParameter("idFicha", idFicha)
                .executeUpdate();
    }
}
