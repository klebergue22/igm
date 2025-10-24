/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.servicio;

import ec.gob.prueba.prueba_maven.modelo.Mensaje;
import static java.lang.Math.log;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

 

/**
 *
 * @author GUERRA_KLEBER
 */
@Stateless
@Slf4j

public class MensajeService {
   @PersistenceContext(unitName="rhPU")
    private EntityManager em;

    public Mensaje guardar(String texto) {
        Mensaje m = new Mensaje(null, texto, new Date());
        em.persist(m);
        log.info("Mensaje guardado: {}", m);
        return m;
    }

    public List<Mensaje> listar() {
        return em.createQuery("SELECT m FROM Mensaje m ORDER BY m.id DESC", Mensaje.class)
                 .getResultList();
    }
}
