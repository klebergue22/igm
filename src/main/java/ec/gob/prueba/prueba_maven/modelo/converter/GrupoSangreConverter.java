/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.converter;

import ec.gob.prueba.prueba_maven.modelo.enums.GrupoSangre;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author GUERRA_KLEBER
 */
@Converter(autoApply = false) // si pones true, puedes omitir @Convert en la entidad
public class GrupoSangreConverter implements AttributeConverter<GrupoSangre, String> {

    @Override
    public String convertToDatabaseColumn(GrupoSangre attr) {
        return attr == null ? null : attr.getCodigo();   // "O+", "A-", etc.
    }

    @Override
    public GrupoSangre convertToEntityAttribute(String db) {
        return db == null ? null : GrupoSangre.fromCodigo(db);
    }
}
