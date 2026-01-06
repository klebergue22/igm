package ec.gob.prueba.prueba_maven.servicio;

import ec.gob.prueba.prueba_maven.modelo.FichaActLaboral;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class FichaActLaboralService {

    // ✅ Debe ser el mismo PU donde mapeas CONSULTORIO (igual que FichaOcupacionalService)
    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    public FichaActLaboral find(Long id) {
        return (id == null) ? null : em.find(FichaActLaboral.class, id);
    }

    public List<FichaActLaboral> listarPorFicha(Long idFicha) {
        if (idFicha == null) {
            return java.util.Collections.emptyList();
        }

        return em.createQuery(
                "SELECT a FROM FichaActLaboral a " +
                "WHERE a.ficha.idFicha = :idFicha " +
                "ORDER BY a.nroFila",
                FichaActLaboral.class
        ).setParameter("idFicha", idFicha)
         .getResultList();
    }

    /**
     * Busca una fila específica por UNIQUE (ID_FICHA, NRO_FILA)
     */
    public FichaActLaboral buscarPorFichaYFila(Long idFicha, Integer nroFila) {
        if (idFicha == null || nroFila == null) {
            return null;
        }

        List<FichaActLaboral> res = em.createQuery(
                "SELECT a FROM FichaActLaboral a " +
                "WHERE a.ficha.idFicha = :idFicha " +
                "AND a.nroFila = :nroFila",
                FichaActLaboral.class
        ).setParameter("idFicha", idFicha)
         .setParameter("nroFila", nroFila)
         .setMaxResults(1)
         .getResultList();

        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * Guardado simple por PK (ID_FICHA_ACT_LAB).
     * - Si idFichaActLab == null => INSERT
     * - Si idFichaActLab != null => UPDATE
     */
    public FichaActLaboral guardar(FichaActLaboral e) {
        if (e == null) return null;

        final Date ahora = new Date();

        if (e.getIdFichaActLab() == null) {
            // defaults S/N para evitar ORA-02290 por checks
            if (e.getEsAnterior() == null) e.setEsAnterior("N");
            if (e.getEsActual() == null) e.setEsActual("N");
            if (e.getIncidente() == null) e.setIncidente("N");
            if (e.getAccidente() == null) e.setAccidente("N");
            if (e.getEnfOcupacional() == null) e.setEnfOcupacional("N");

            if (e.getFCreacion() == null) e.setFCreacion(ahora);
            if (e.getUsrCreacion() == null) e.setUsrCreacion("SYSTEM");

            em.persist(e);
            return e;
        } else {
            e.setFActualizacion(ahora);
            if (e.getUsrActualizacion() == null) e.setUsrActualizacion("SYSTEM");
            return em.merge(e);
        }
    }

    /**
     * ✅ UPSERT por UNIQUE (ID_FICHA, NRO_FILA) para respetar UX_FAL_FICHA_FILA.
     * Si ya existe una fila (misma ficha y nroFila) => actualiza
     * Si no existe => inserta
     */
    public FichaActLaboral guardarUpsert(FichaActLaboral a, String usuario) {
        if (a == null) return null;

        if (a.getFicha() == null || a.getFicha().getIdFicha() == null) {
            throw new IllegalArgumentException("La actividad laboral debe tener ficha (ID_FICHA) obligatoria.");
        }
        if (a.getNroFila() == null) {
            throw new IllegalArgumentException("NRO_FILA es obligatorio.");
        }

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date ahora = new Date();

        // defaults S/N para evitar ORA-02290 por checks
        if (a.getEsAnterior() == null) a.setEsAnterior("N");
        if (a.getEsActual() == null) a.setEsActual("N");
        if (a.getIncidente() == null) a.setIncidente("N");
        if (a.getAccidente() == null) a.setAccidente("N");
        if (a.getEnfOcupacional() == null) a.setEnfOcupacional("N");

        // buscar existente por UNIQUE (ficha, fila)
        FichaActLaboral existente = buscarPorFichaYFila(a.getFicha().getIdFicha(), a.getNroFila());
        if (existente != null) {
            // ✅ clave: setear el PK real para que JPA haga merge (UPDATE)
            a.setIdFichaActLab(existente.getIdFichaActLab());

            // mantener auditoría de creación original (opcional pero recomendado)
            a.setFCreacion(existente.getFCreacion());
            a.setUsrCreacion(existente.getUsrCreacion());
        }

        if (a.getIdFichaActLab() == null) {
            // INSERT
            a.setFCreacion(ahora);
            a.setUsrCreacion(usr);
            em.persist(a);
            return a;
        } else {
            // UPDATE
            a.setFActualizacion(ahora);
            a.setUsrActualizacion(usr);
            return em.merge(a);
        }
    }

    public int eliminarPorFicha(Long idFicha) {
        if (idFicha == null) return 0;

        return em.createQuery(
                "DELETE FROM FichaActLaboral a WHERE a.ficha.idFicha = :idFicha"
        ).setParameter("idFicha", idFicha)
         .executeUpdate();
    }

    public int eliminarPorFichaYFila(Long idFicha, Integer nroFila) {
        if (idFicha == null || nroFila == null) return 0;

        return em.createQuery(
                "DELETE FROM FichaActLaboral a " +
                "WHERE a.ficha.idFicha = :idFicha " +
                "AND a.nroFila = :nroFila"
        ).setParameter("idFicha", idFicha)
         .setParameter("nroFila", nroFila)
         .executeUpdate();
    }
}
