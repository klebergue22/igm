/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.converter;

/**
 *
 * @author GUERRA_KLEBER
 */
 

import ec.gob.prueba.prueba_maven.modelo.Cie10;
import ec.gob.prueba.prueba_maven.servicio.Cie10Service;
import ec.gob.prueba.prueba_maven.web.CentroMedicoCtrl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("cie10Conv")
public class Cie10Converter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent c, String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            CentroMedicoCtrl bean = (CentroMedicoCtrl) fc.getApplication()
                    .evaluateExpressionGet(fc, "#{centroMedicoCtrl}", CentroMedicoCtrl.class);

            if (bean == null) {
                System.err.println("[cie10Conv] ERROR: No se pudo resolver #{centroMedicoCtrl}");
                return null;
            }

            Cie10Service srv = bean.getCie10Service();
            if (srv == null) {
                System.err.println("[cie10Conv] ERROR: cie10Service es null en CentroMedicoCtrl");
                return null;
            }

            Cie10 cie = srv.buscarPorCodigo(value.trim());
            if (cie == null) {
                System.err.println("[cie10Conv] WARN: No existe CIE10 para codigo=" + value.trim());
            }
            return cie;

        } catch (Exception e) {
            System.err.println("[cie10Conv] ERROR getAsObject value=" + value);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent c, Object obj) {
        try {
            if (obj == null) {
                return "";
            }
            if (!(obj instanceof Cie10)) {
                System.err.println("[cie10Conv] ERROR getAsString: objeto no es Cie10 -> " + obj.getClass());
                return "";
            }
            Cie10 cie = (Cie10) obj;
            return (cie.getCodigo() == null) ? "" : cie.getCodigo();

        } catch (Exception e) {
            System.err.println("[cie10Conv] ERROR getAsString obj=" + obj);
            e.printStackTrace();
            return "";
        }
    }
}
