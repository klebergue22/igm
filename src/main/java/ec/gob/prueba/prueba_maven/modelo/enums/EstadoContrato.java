/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
/**
 *
 * @author GUERRA_KLEBER
 */
 

// EstadoContrato.java

import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EstadoContrato {
    VIGENTE("V", "Vigente"),
    FINALIZADO("F", "Finalizado"),
    CESADO("C","Cesado"),
    SUSPENDIDO("S", "Suspendido");

    private final String codigo;       // lo que hay en BD (ej: "V")
    private final String descripcion;  // lo que muestras en UI

    public static EstadoContrato fromCodigo(String c) {
        if (c == null || c.isEmpty()) return null;
        switch (c.charAt(0)) {
            case 'V': return VIGENTE;
            case 'F': return FINALIZADO;
            case 'S': return SUSPENDIDO;
            case 'C': return CESADO;
            default: throw new IllegalArgumentException("Código de estado desconocido: " + c);
        }
    }
}
