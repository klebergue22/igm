/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.servicio;
import ec.gob.prueba.prueba_maven.modelo.DatEmpleado;

import javax.ejb.Stateless;
import javax.persistence.*;
import java.util.List;
/**
 *
 * @author GUERRA_KLEBER
 */

@Stateless
public class EmpleadoService {
  @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    public List<DatEmpleado> listarTodos() {
        return em.createQuery(
            "select e from DatEmpleado e order by e.priApellido, e.nombres",
            DatEmpleado.class
        ).getResultList();
    }
 
    public List<DatEmpleado> buscarPorApellidos(String filtro) {
        String f = filtro == null ? "" : filtro.trim().toLowerCase();
        return em.createQuery(
            "select e from DatEmpleado e " +
            "where lower(e.priApellido) like :f or lower(e.segApellido) like :f or lower(e.nombres) like :f " +
            "order by e.priApellido, e.nombres",
            DatEmpleado.class
        ).setParameter("f", "%"+f+"%").getResultList();
    }

    public DatEmpleado buscarPorId(Integer noPersona) {
        return em.find(DatEmpleado.class, noPersona);
    }

    public DatEmpleado guardar(DatEmpleado e) {
        if (e.getNoPersona() == null) {
            em.persist(e);
            return e;
        }
        return em.merge(e);
    }

    public void eliminar(Integer noPersona) {
        DatEmpleado e = em.find(DatEmpleado.class, noPersona);
        if (e != null) em.remove(e);
    }
    
}
