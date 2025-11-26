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
 

import ec.gob.prueba.prueba_maven.modelo.AuditoriaConsultorio;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

@Stateless
public class AuditoriaConsultorioService {

    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    public AuditoriaConsultorio registrar(
            String modulo,
            String usuario,
            String accion,
            String tablaAfecta,
            String campoAfecta,
            String observaciones
    ) {
        AuditoriaConsultorio a = new AuditoriaConsultorio();
        a.setModulo(modulo);
        a.setUsuario(usuario != null ? usuario : "USR_APP");
        a.setFecha(new Date());
        a.setAccion(accion);
        a.setTablaAfecta(tablaAfecta);
        a.setCampoAfecta(campoAfecta);
        a.setObservaciones(observaciones);
        em.persist(a);
        return a;
    }
      public AuditoriaConsultorio guardar(AuditoriaConsultorio auditoria) {
        if (auditoria.getId() == null) {
            em.persist(auditoria);
            return auditoria;
        } else {
            return em.merge(auditoria);
        }
    }
}
