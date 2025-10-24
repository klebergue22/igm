/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.converter;
import ec.gob.prueba.prueba_maven.modelo.enums.Flag01;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.math.BigDecimal;
/**
 *
 * @author GUERRA_KLEBER
 */
@Converter(autoApply = false)
public class Flag01Converter implements AttributeConverter<Flag01, BigDecimal> {
    @Override public BigDecimal convertToDatabaseColumn(Flag01 a) {
        return a == null ? null : BigDecimal.valueOf(a.getCode());
    }
    @Override public Flag01 convertToEntityAttribute(BigDecimal db) {
        return db == null ? null : Flag01.fromCode(db);
    }
}
