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
 

import ec.gob.prueba.prueba_maven.modelo.enums.TipoContrato;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigDecimal;

@Converter(autoApply = false)
public class TipoContratoConverter implements AttributeConverter<TipoContrato, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(TipoContrato atributo) {
        if (atributo == null) {
            return null;
        }
        // Guarda el código del enum como BigDecimal (porque la BD usa NUMBER)
        return BigDecimal.valueOf(atributo.getCodigo());
    }

    @Override
    public TipoContrato convertToEntityAttribute(BigDecimal valorBD) {
        if (valorBD == null) {
            return null;
        }
        // Convierte el valor de la BD al enum correspondiente
        return TipoContrato.fromCodigo(valorBD.intValue());
    }
}