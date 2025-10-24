/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo.enums;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
 *
 * @author GUERRA_KLEBER
 */
@Getter
@RequiredArgsConstructor
public enum Flag01 {
    NO(0), SI(1);
    private final int code;

    public static Flag01 fromCode(Number n) {
        if (n == null) return null;
        int v = n.intValue();
        if (v == 0) return NO;
        if (v == 1) return SI;
        throw new IllegalArgumentException("Flag01 inválido: " + n);
    }
}