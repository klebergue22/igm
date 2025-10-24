// src/main/java/ec/gob/prueba/prueba_maven/modelo/converter/EstadoCivilConverterJPA.java
package ec.gob.prueba.prueba_maven.modelo.converter;

import ec.gob.prueba.prueba_maven.modelo.enums.EstadoCivil;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
public class EstadoCivilConverterJPA implements AttributeConverter<EstadoCivil, String> {

    @Override
    public String convertToDatabaseColumn(EstadoCivil attribute) {
        return attribute == null ? null : attribute.getCodigo();
    }

    @Override
    public EstadoCivil convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String code = dbData.trim();
        if (code.isEmpty()) return null;       // por si en BD hay vacío
        return EstadoCivil.fromCodigo(code);   // ⬅️ nombre correcto
    }
}
