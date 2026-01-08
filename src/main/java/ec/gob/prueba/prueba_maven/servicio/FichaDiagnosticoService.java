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

import ec.gob.prueba.prueba_maven.modelo.ConsultaDiagnostico;
import ec.gob.prueba.prueba_maven.modelo.FichaDiagnostico;
import ec.gob.prueba.prueba_maven.modelo.FichaOcupacional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Guarda diagnósticos de una ficha a partir de diagnósticos de consulta.
     * Estrategia: elimina existentes y vuelve a insertar (simple y evita duplicados por UK_FD_FICHA_CIE10).
     */
    public void guardarDiagnosticosDeFicha(Long idFicha,
                                          List<ConsultaDiagnostico> diagnosticos,
                                          Date fecha,
                                          String usuario) {

        if (idFicha == null) {
            throw new IllegalArgumentException("idFicha no puede ser null");
        }

        // 1) Eliminar actuales
        eliminarPorFicha(idFicha);

        if (diagnosticos == null || diagnosticos.isEmpty()) {
            return;
        }

        // 2) Referencia a la ficha
        FichaOcupacional fichaRef = em.getReference(FichaOcupacional.class, idFicha);

        // 3) Evitar violación de unique(ID_FICHA, COD_CIE10) si viene repetido el mismo código
        Set<String> codigosInsertados = new HashSet<>();

        int orden = 1;
        Date now = (fecha != null) ? fecha : new Date();

        for (ConsultaDiagnostico cd : diagnosticos) {
            if (cd == null) continue;

            // ✅ En tu modelo el código se obtiene con getCodigo()
            String cod = safe(cd.getCodigo());
            if (cod == null) continue;

            if (!codigosInsertados.add(cod)) {
                continue; // repetido, saltar
            }

            FichaDiagnostico fd = new FichaDiagnostico();
            fd.setFicha(fichaRef);
            fd.setCodCie10(cod);

            // ✅ En tu modelo la descripción está como puente getDescripcion()
            fd.setDescripcion(safe(cd.getDescripcion()));

            // ✅ En tu modelo sí existe getTipoDiag() (campo tipoDiag)
            fd.setTipoDiag(normalizarTipo(cd.getTipoDiag())); // P / S

            fd.setOrden(orden++);
            fd.setEstado("A");

            // Auditoría
            fd.setFechaCreacion(now);
            fd.setUsrCreacion(usuario);

            // En creación no necesitas setear actualización
            fd.setFechaActualizacion(null);
            fd.setUsrActualizacion(null);

            em.persist(fd);
        }

        em.flush();
    }

    private String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String normalizarTipo(String tipo) {
        String t = safe(tipo);
        if (t == null) return "S";
        t = t.toUpperCase();
        return ("P".equals(t) || "S".equals(t)) ? t : "S";
    }
}
