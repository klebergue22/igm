/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.web;

import ec.gob.prueba.prueba_maven.servicio.MensajeService;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author GUERRA_KLEBER
 */
@Named @RequestScoped @Getter @Setter
public class HolaController {
    @EJB
  private MensajeService servicio;

  private String texto = "Bienvenido a Java";

  public void saludar() {
    servicio.guardar(texto);
    FacesContext.getCurrentInstance().addMessage(null,
      new FacesMessage(FacesMessage.SEVERITY_INFO,
        "Bienvenido a Java", "Se guardó el mensaje en Oracle."));
  }
}
