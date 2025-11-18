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
 

import ec.gob.prueba.prueba_maven.modelo.FichaOcupacional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

@Stateless
public class FichaOcupacionalService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "rhPU") // ajusta si tu PU se llama distinto
    private EntityManager em;

    /**
     * Guarda o actualiza una FichaOcupacional.
     * Si el id es null -> persist
     * Si el id no es null -> merge
     */
    public FichaOcupacional guardar(FichaOcupacional ficha) {
        if (ficha == null) {
            return null;
        }

        if (ficha.getIdFicha() == null) {
            em.persist(ficha);
            // después de persist, el id se asigna
            return ficha;
        } else {
            return em.merge(ficha);
        }
    }

    /**
     * Buscar una ficha por ID.
     */
    public FichaOcupacional buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(FichaOcupacional.class, id);
    }

    // Si quieres luego puedes añadir consultas por empleado, fecha, etc.
}
