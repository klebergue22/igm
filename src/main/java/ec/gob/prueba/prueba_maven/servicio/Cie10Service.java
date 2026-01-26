/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author GUERRA_KLEBER
 */
package ec.gob.prueba.prueba_maven.servicio;

import ec.gob.prueba.prueba_maven.modelo.Cie10;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class Cie10Service {

    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    private static final int DEFAULT_LIMIT = 30;

    public List<Cie10> buscarJerarquiaPorTerm(String term) {
        String limpio = limpiarCodigo(term);
        if (limpio.isEmpty()) return new ArrayList<Cie10>();

        if (limpio.length() < 3) {
            return queryPorPrefijoCodigo(limpio, DEFAULT_LIMIT);
        }

        String root = limpio.substring(0, 3);
        List<Cie10> lista = queryRootYRama(root);

        return filtrarJerarquia(lista, root, limpio);
    }

    public Cie10 buscarPorCodigo(String codigo) {
        String c = limpiarCodigoExacto(codigo);
        if (c.isEmpty()) return null;
        return em.find(Cie10.class, c);
    }

    // ====== SOBRECARGAS (arreglan tu error de firma) ======
    public List<Cie10> buscarPorCodigoODescripcion(String term) {
        return buscarPorCodigoODescripcion(term, DEFAULT_LIMIT);
    }

    public List<Cie10> buscarPorCodigoODescripcion(String term, int limit) {
        if (term == null) return new ArrayList<Cie10>();
        String t = term.trim();
        if (t.isEmpty()) return new ArrayList<Cie10>();

        int lim = normalizarLimit(limit);
        String q = "%" + t.toUpperCase() + "%";

        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.codigo) LIKE :q OR UPPER(c.descripcion) LIKE :q " +
                "ORDER BY c.codigo", Cie10.class)
            .setParameter("q", q)
            .setMaxResults(lim)
            .getResultList();
    }

    public List<Cie10> buscarPorDescripcionLike(String term) {
        return buscarPorDescripcionLike(term, DEFAULT_LIMIT);
    }

    public List<Cie10> buscarPorDescripcionLike(String term, int limit) {
        if (term == null) return new ArrayList<Cie10>();
        String t = term.trim();
        if (t.isEmpty()) return new ArrayList<Cie10>();

        int lim = normalizarLimit(limit);
        String q = "%" + t.toUpperCase() + "%";

        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) LIKE :q " +
                "ORDER BY c.descripcion", Cie10.class)
            .setParameter("q", q)
            .setMaxResults(lim)
            .getResultList();
    }

    public Cie10 buscarPrimeroPorDescripcion(String descripcion) {
        if (descripcion == null) return null;
        String d = descripcion.trim().toUpperCase();
        if (d.isEmpty()) return null;

        List<Cie10> lista = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) = :d " +
                "ORDER BY c.codigo", Cie10.class)
            .setParameter("d", d)
            .setMaxResults(1)
            .getResultList();

        return lista.isEmpty() ? null : lista.get(0);
    }

    // =========================
    // Helpers (cortos)
    // =========================
    private int normalizarLimit(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, 50);
    }

    private String limpiarCodigo(String term) {
        if (term == null) return "";
        String limpio = term.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        return limpio == null ? "" : limpio;
    }

    private String limpiarCodigoExacto(String codigo) {
        if (codigo == null) return "";
        return codigo.trim().toUpperCase();
    }

    private List<Cie10> queryPorPrefijoCodigo(String prefijo, int limit) {
        return em.createQuery(
                "SELECT c FROM Cie10 c WHERE UPPER(c.codigo) LIKE :q ORDER BY c.codigo",
                Cie10.class)
            .setParameter("q", prefijo + "%")
            .setMaxResults(normalizarLimit(limit))
            .getResultList();
    }

    private List<Cie10> queryRootYRama(String root) {
        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.codigo) = :root OR UPPER(c.codigo) LIKE :rootPref " +
                "ORDER BY c.codigo", Cie10.class)
            .setParameter("root", root)
            .setParameter("rootPref", root + "%")
            .getResultList();
    }

    private List<Cie10> filtrarJerarquia(List<Cie10> lista, String root, String limpio) {
        List<Cie10> filtrados = new ArrayList<Cie10>();
        for (Cie10 c : lista) {
            if (c == null || c.getCodigo() == null) continue;
            String cod = c.getCodigo().toUpperCase();

            if (cod.equals(root)) {
                filtrados.add(c);
            } else if (cod.startsWith(limpio)) {
                filtrados.add(c);
            }
        }
        return filtrados;
    }
}
