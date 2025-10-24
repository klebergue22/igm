package ec.gob.prueba.prueba_maven.modelo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrupoSangre {
    O_POS("O+", "O positivo"),
    O_NEG("O-", "O negativo"),
    A_POS("A+", "A positivo"),
    A_NEG("A-", "A negativo"),
    B_POS("B+", "B positivo"),
    B_NEG("B-", "B negativo"),
    AB_POS("AB+", "AB positivo"),
    AB_NEG("AB-", "AB negativo");

    private final String codigo;
    private final String descripcion;

    public static GrupoSangre fromCodigo(String c) {
        if (c == null) return null;
        for (GrupoSangre g : values()) if (g.codigo.equalsIgnoreCase(c)) return g;
        throw new IllegalArgumentException("Código GrupoSangre inválido: " + c);
    }

    @Override public String toString() { return descripcion; }
}
