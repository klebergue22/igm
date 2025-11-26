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
 * Servicio para gestionar la tabla CONSULTORIO.PERSONA_AUX
 */
 

import ec.gob.prueba.prueba_maven.modelo.PersonaAux;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class PersonaAuxService {

    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    // =====================
    // BÁSICOS
    // =====================

    public PersonaAux guardar(PersonaAux p) {
        Date ahora = new Date();

        if (p.getIdPersonaAux() == null) {
            // INSERT
            if (p.getEstado() == null) {
                p.setEstado("PENDIENTE");
            }
            if (p.getFechaCreacion() == null) {
                p.setFechaCreacion(ahora);
            }
            if (p.getUsrCreacion() == null) {
                p.setUsrCreacion("USR_APP"); // aquí luego pones el usuario real
            }
            em.persist(p);
            return p;
        } else {
            // UPDATE
            p.setFechaActualizacion(ahora);
            if (p.getUsrActualizacion() == null) {
                p.setUsrActualizacion("USR_APP"); // usuario real
            }
            return em.merge(p);
        }
    }

    public PersonaAux find(Long id) {
        return em.find(PersonaAux.class, id);
    }

    // =====================
    // CONSULTAS
    // =====================

    /** Lista completa (ordenada por ID). */
    public List<PersonaAux> listarTodos() {
        return em.createNamedQuery("PersonaAux.findAll", PersonaAux.class)
                 .getResultList();
    }

    /** Busca por cédula (devuelve null si no hay). */
    public PersonaAux findByCedula(String cedula) {
        List<PersonaAux> lst = em.createNamedQuery("PersonaAux.findByCedula", PersonaAux.class)
                                 .setParameter("cedula", cedula)
                                 .getResultList();
        return lst.isEmpty() ? null : lst.get(0);
    }

    /** Solo registros en estado PENDIENTE. */
    public List<PersonaAux> listarPendientes() {
        return em.createNamedQuery("PersonaAux.findPendientes", PersonaAux.class)
                 .getResultList();
    }

    /** Solo registros ya vinculados (estado VINCULADO). */
    public List<PersonaAux> listarVinculados() {
        return em.createNamedQuery("PersonaAux.findVinculados", PersonaAux.class)
                 .getResultList();
    }

    // =====================
    // UTILITARIO OPCIONAL
    // =====================

    /**
     * Crea rápidamente un registro auxiliar con datos básicos desde la pantalla.
     * (Úsalo si quieres encapsular la lógica de creación en el servicio).
     */
    public PersonaAux crearDesdePantalla(String cedula,
                                         String nombres,
                                         String apellidos,
                                         String sexo,
                                         Date fechaNac) {
        PersonaAux p = new PersonaAux();
        p.setCedula(cedula);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setSexo(sexo);
        p.setFechaNac(fechaNac);
        // estado / fechas / usr se setean en guardar(...)
        return guardar(p);
    }
}
