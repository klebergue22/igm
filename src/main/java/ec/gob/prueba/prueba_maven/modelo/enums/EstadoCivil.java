package ec.gob.prueba.prueba_maven.modelo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoCivil {
    SOLTERO("S", "Soltero/a"),
    CASADO("C", "Casado/a"),
    DIVORCIADO("D", "Divorciado/a"),
    VIUDO("V", "Viudo/a"),
    UNION_LIBRE("U", "Unión de hecho");

    private final String codigo;
    private final String descripcion;

    public static EstadoCivil fromCodigo(String c) {
        if (c == null) return null;
        for (EstadoCivil e : values()) if (e.codigo.equalsIgnoreCase(c)) return e;
        throw new IllegalArgumentException("Código EstadoCivil inválido: " + c);
    }

    @Override public String toString() { return descripcion; }
}
