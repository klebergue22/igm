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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import ec.gob.prueba.prueba_maven.web.CentroMedicoCtrl;


@FacesConverter("cie10Conv")
public class Cie10Converter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent c, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // 🔑 Obtener el ManagedBean JSF (JSF 2.2 compatible)
        CentroMedicoCtrl bean = (CentroMedicoCtrl) fc.getApplication()
                .evaluateExpressionGet(fc, "#{centroMedicoCtrl}", CentroMedicoCtrl.class);

        return bean.getCie10Service().buscarPorCodigo(value.trim());
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent c, Object obj) {
        if (obj == null) {
            return "";
        }
        Cie10 cie = (Cie10) obj;
        return cie.getCodigo();
    }
}
