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
// SexoFacesConverter.java
 

import ec.gob.prueba.prueba_maven.modelo.enums.Sexo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = Sexo.class, value = "sexoFacesConverter")
public class SexoFacesConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return Sexo.fromCodigo(value); // "M" / "F" -> enum
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) return "";
        return ((Sexo) value).getCodigo();
    }
}
