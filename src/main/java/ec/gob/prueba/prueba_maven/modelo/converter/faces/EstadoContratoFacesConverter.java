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
 

 

import ec.gob.prueba.prueba_maven.modelo.enums.EstadoContrato;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter(forClass = EstadoContrato.class)
public class EstadoContratoFacesConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.isEmpty()) return null;
        // Si usas código distinto al name, cambia a fromCodigo(value)
        return EstadoContrato.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) return "";
        // Si tu enum tiene getCodigo(), retorna eso
        return ((EstadoContrato) value).name();
    }
}
