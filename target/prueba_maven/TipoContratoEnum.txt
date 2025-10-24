/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author GUERRA_KLEBER
 */

/**
 * Códigos de RH.T_TIPOS_CONTRATO (NUMBER(3))
 */
@Getter
@RequiredArgsConstructor
public enum TipoContrato {
    NOMBRAMIENTO(2, "Nombramiento"),
    NOMB_PROVISIONAL(12, "NOMB. PROVISIONAL"),
    OCASIONAL(20, "OCASIONAL"),
    CONTRATO(34, "PLAZO FIJO CT."),
    HONORARIOS(1, "CONTRATO"),
    CONTRATO_TAREA(40, "CONTRATO TAREA"),
    DESCONOCIDO(-1, "(desconocido)");

    private final int codigo;
    private final String descripcion;

    private static final Map<Integer, TipoContrato> LUT = new ConcurrentHashMap<>();

    static {
        Arrays.stream(values()).forEach(e -> LUT.put(e.codigo, e));
    }

    public static TipoContrato fromCodigo(int codigo) {
        return LUT.getOrDefault(codigo, DESCONOCIDO);
        // Si quieres estricto:
        // TipoContrato t = LUT.get(codigo);
        // if (t == null) throw new IllegalArgumentException("TipoContrato inválido: " + codigo);
        // return t;
    }
}
