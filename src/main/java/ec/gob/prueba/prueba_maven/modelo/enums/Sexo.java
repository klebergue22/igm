/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Sexo {
    MASCULINO("M", "Masculino"),
    FEMENINO("F", "Femenino");

    private final String codigo;       // lo que guardas en BD
    private final String descripcion;  // lo que muestras en UI

    public static Sexo fromCodigo(String c) {
        if (c == null) return null;
        for (Sexo s : values()) if (s.codigo.equalsIgnoreCase(c)) return s;
        throw new IllegalArgumentException("Código Sexo inválido: " + c);
    }

    @Override public String toString() { return descripcion; }
}
