/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.converter.faces;

/**
 *
 * @author GUERRA_KLEBER
 */
 

 

import ec.gob.prueba.prueba_maven.modelo.enums.TipoContrato;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = TipoContrato.class)
public class TipoContratoFacesConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.isEmpty()) return null;
        // Si en la vista envías el código numérico como String:
        // return TipoContrato.fromCodigo(Integer.parseInt(value));
        // Si envías el name del enum:
        return TipoContrato.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) return "";
        // Si en la vista quieres enviar el código:
        // return Integer.toString(((TipoContrato) value).getCodigo());
        // Si envías el name:
        return ((TipoContrato) value).name();
    }
}
