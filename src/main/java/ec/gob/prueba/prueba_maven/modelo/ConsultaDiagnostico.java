/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo;

/**
 *
 * @author GUERRA_KLEBER
 */
 

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CONSULTA_DIAGNOSTICO", schema = "CONSULTORIO")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"consulta", "cie10"})
public class ConsultaDiagnostico implements Serializable {

    @Id
    @SequenceGenerator(
        name = "CONS_DIAG_GEN",
        sequenceName = "CONSULTORIO.SQ_CONS_DIAG",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONS_DIAG_GEN")
    @Column(name = "ID_CONS_DIAG", nullable = false)
    private Long idConsDiag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONSULTA", nullable = false)
    private ConsultaMedica consulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_CIE10", referencedColumnName = "CODIGO", nullable = false)
    private Cie10 cie10;

    @Column(name = "TIPO_DIAG", length = 1)
    private String tipoDiag;  // 'P' principal, 'S' secundario

    @Column(name = "OBSERVACION", length = 1000)
    private String observacion;

    @Column(name = "ES_PPAL", insertable = false, updatable = false)
    private Integer esPpal; // virtual (1 si es principal, NULL si no)
    
        // ========= Puente hacia Cie10 para la vista/controlador =========

    @Transient
    public String getCodigo() {
        return (cie10 != null ? cie10.getCodigo() : null);
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            this.cie10 = null;   // si borras el código, limpias el CIE10
        } else {
            if (this.cie10 == null) {
                this.cie10 = new Cie10();
            }
            this.cie10.setCodigo(codigo);
        }
    }

    @Transient
    public String getDescripcion() {
        return (cie10 != null ? cie10.getDescripcion() : null);
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            if (this.cie10 != null) {
                this.cie10.setDescripcion(null);
            }
        } else {
            if (this.cie10 == null) {
                this.cie10 = new Cie10();
            }
            this.cie10.setDescripcion(descripcion);
        }
    }

}
