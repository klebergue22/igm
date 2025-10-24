/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.converter;
import ec.gob.prueba.prueba_maven.modelo.enums.Sexo;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import lombok.NoArgsConstructor;
/**
 *
 * @author GUERRA_KLEBER
 */
@NoArgsConstructor
@Converter(autoApply = false)
public class SexoConverterJPA implements AttributeConverter<Sexo, String> {

    @Override
    public String convertToDatabaseColumn(Sexo attribute) {
        return attribute == null ? null : attribute.getCodigo();
    }

    @Override
    public Sexo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) return null;
        return Sexo.fromCodigo(dbData);
    }
}