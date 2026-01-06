package ec.gob.prueba.prueba_maven.servicio;
/**
 *
 * @author GUERRA_KLEBER
 */


import ec.gob.prueba.prueba_maven.modelo.FichaRiesgoDet;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

@Stateless
public class FichaRiesgoDetService {

    @PersistenceContext(unitName = "rhPU") // Debe coincidir con persistence.xml
    private EntityManager em;

    // =========================
    // Buscar por ID
    // =========================
    public FichaRiesgoDet find(Long id) {
        if (id == null) return null;
        return em.find(FichaRiesgoDet.class, id);
    }

    // =========================
    // Listar detalle por ficha
    // =========================
    public List<FichaRiesgoDet> listarPorFicha(Long idFicha) {

        TypedQuery<FichaRiesgoDet> q = em.createQuery(
            "SELECT d FROM FichaRiesgoDet d " +
            "WHERE d.ficha.idFicha = :idFicha " +
            "ORDER BY d.actividadNro, d.grupo, d.orden",
            FichaRiesgoDet.class
        );

        q.setParameter("idFicha", idFicha);
        return q.getResultList();
    }

    // =========================
    // Guardar / actualizar
    // =========================
    public FichaRiesgoDet guardar(FichaRiesgoDet d, String usuario) {

        if (d == null) return null;

        final String usr = (usuario == null || usuario.trim().isEmpty())
                ? "SYSTEM"
                : usuario.trim();

        final Date ahora = new Date();

        if (d.getIdFichaRiesgoDet() == null) {
            // INSERT
            d.setFCreacion(ahora);
            d.setUsrCreacion(usr);
            em.persist(d);
            return d;
        } else {
            // UPDATE
            d.setFActualizacion(ahora);
            d.setUsrActualizacion(usr);
            return em.merge(d);
        }
    }

    // =========================
    // Eliminar todo el detalle de una ficha
    // (usar antes de volver a guardar Step 2)
    // =========================
    public int eliminarPorFicha(Long idFicha) {

        if (idFicha == null) return 0;

        return em.createQuery(
            "DELETE FROM FichaRiesgoDet d WHERE d.ficha.idFicha = :idFicha"
        )
        .setParameter("idFicha", idFicha)
        .executeUpdate();
    }
}
