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

    // SOLO unitName (debe coincidir con persistence.xml)
    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    public FichaRiesgoDet find(Long id) {
        if (id == null) return null;
        return em.find(FichaRiesgoDet.class, id);
    }

    public List<FichaRiesgoDet> listarPorFicha(Long idFicha) {
        TypedQuery<FichaRiesgoDet> q = em.createQuery(
                "SELECT d FROM FichaRiesgoDet d " +
                "WHERE d.ficha.idFicha = :idFicha " +
                "ORDER BY d.nroFila",
                FichaRiesgoDet.class
        );
        q.setParameter("idFicha", idFicha);
        return q.getResultList();
    }

    public FichaRiesgoDet guardar(FichaRiesgoDet d, String usuario) {
        if (d == null) return null;

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date ahora = new Date();

        if (d.getIdFichaRiesgoDet() == null) {
            d.setFCreacion(ahora);
            d.setUsrCreacion(usr);
            em.persist(d);
            return d;
        } else {
            d.setFActualizacion(ahora);
            d.setUsrActualizacion(usr);
            return em.merge(d);
        }
    }

    public int eliminarPorFicha(Long idFicha) {
        return em.createQuery(
                "DELETE FROM FichaRiesgoDet d WHERE d.ficha.idFicha = :idFicha"
        ).setParameter("idFicha", idFicha)
         .executeUpdate();
    }
}
