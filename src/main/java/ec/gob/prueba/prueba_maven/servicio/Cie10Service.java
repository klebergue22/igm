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
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Stateless

public class Cie10Service {
     @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    public List<Cie10> buscarJerarquiaPorTerm(String term) {

        if (term == null) {
            return new ArrayList<Cie10>();
        }

        String limpio = term.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (limpio.length() == 0) {
            return new ArrayList<Cie10>();
        }

        // Menos de 3 caracteres → búsqueda simple
        if (limpio.length() < 3) {
            return em.createQuery(
                    "SELECT c FROM Cie10 c WHERE UPPER(c.codigo) LIKE :q ORDER BY c.codigo",
                    Cie10.class)
                    .setParameter("q", limpio + "%")
                    .setMaxResults(30)
                    .getResultList();
        }

        // raíz del padre (3 primeros)
        String root = limpio.substring(0, 3);

        List<Cie10> lista = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.codigo) = :root " +
                "   OR UPPER(c.codigo) LIKE :rootPref " +
                "ORDER BY c.codigo", Cie10.class)
                .setParameter("root", root)
                .setParameter("rootPref", root + "%")
                .getResultList();

        // Como no hay Java 8, filtramos manualmente
        List<Cie10> filtrados = new ArrayList<Cie10>();
        for (Cie10 c : lista) {
            if (c.getCodigo() == null) continue;

            String cod = c.getCodigo().toUpperCase();

            if (cod.equals(root)) {
                filtrados.add(c); // siempre padre
            } else if (cod.startsWith(limpio)) {
                filtrados.add(c);
            }
        }

        return filtrados;
    }

    public Cie10 buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        return em.find(Cie10.class, codigo.trim().toUpperCase());
    }
    
     /** Sugerencias por código o descripción (case-insensitive), con tope de 20 filas */
    public List<Cie10> buscarPorCodigoODescripcion(String term) {
        if (term == null) return new ArrayList<Cie10>();
        String q = "%" + term.trim().toUpperCase() + "%";
        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.codigo) LIKE :q OR UPPER(c.descripcion) LIKE :q " +
                "ORDER BY c.codigo", Cie10.class)
            .setParameter("q", q)
            .setMaxResults(20)
            .getResultList();
    }
    
    // ==========================
    // AUTOCOMPLETE POR DESCRIPCION
    // ==========================
    public java.util.List<Cie10> buscarPorDescripcionLike(String term) {
        if (term == null) {
            return new java.util.ArrayList<Cie10>();
        }
        String q = "%" + term.trim().toUpperCase() + "%";
        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) LIKE :q " +
                "ORDER BY c.descripcion",
                Cie10.class)
                .setParameter("q", q)
                .setMaxResults(20)
                .getResultList();
    }

    public Cie10 buscarPrimeroPorDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }
        java.util.List<Cie10> lista = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) = :d " +
                "ORDER BY c.codigo",
                Cie10.class)
                .setParameter("d", descripcion.trim().toUpperCase())
                .setMaxResults(1)
                .getResultList();
        return lista.isEmpty() ? null : lista.get(0);
    }
}

   


    
