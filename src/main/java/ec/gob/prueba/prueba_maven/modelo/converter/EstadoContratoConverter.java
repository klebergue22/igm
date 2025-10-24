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

import ec.gob.prueba.prueba_maven.modelo.enums.EstadoContrato;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
public class EstadoContratoConverter implements AttributeConverter<EstadoContrato, String> {

    @Override
    public String convertToDatabaseColumn(EstadoContrato atributo) {
        if (atributo == null) {
            return null;
        }
        // Mapea las constantes del enum a los códigos de BD
        switch (atributo) {
            case VIGENTE:
                return "V";
            case CESADO:
                return "C";
            case SUSPENDIDO:
                return "S";
            default:
                return null; // o lanza IllegalArgumentException
        }
    }

    @Override
    public EstadoContrato convertToEntityAttribute(String valorBD) {
        if (valorBD == null) {
            return null;
        }
        String v = valorBD.trim().toUpperCase();

        // Mapea el código de BD a la constante del enum
        switch (v) {
            case "V":
                return EstadoContrato.VIGENTE;
            case "C":
                return EstadoContrato.CESADO;
            case "S":
                return EstadoContrato.SUSPENDIDO;
            default:
                throw new IllegalArgumentException("Código de EstadoContrato inválido: " + valorBD);
        }
    }
}
