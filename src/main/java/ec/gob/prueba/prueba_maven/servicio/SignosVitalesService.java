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
 

import ec.gob.prueba.prueba_maven.modelo.SignosVitales;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

@Stateless
public class SignosVitalesService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "rhPU") // usa el mismo PU donde están mapeadas las entidades CONSULTORIO
    private EntityManager em;

    /**
     * Guarda o actualiza un registro de signos vitales.
     * Si el id es null -> persist
     * Si el id no es null -> merge
     */
    public SignosVitales guardar(SignosVitales signos) {
        if (signos == null) {
            return null;
        }

        if (signos.getIdSignos() == null) {
            em.persist(signos);
            return signos;
        } else {
            return em.merge(signos);
        }
    }

    /**
     * Buscar signos por ID.
     */
    public SignosVitales buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return em.find(SignosVitales.class, id);
    }
}
