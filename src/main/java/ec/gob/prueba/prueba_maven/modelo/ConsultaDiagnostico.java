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
}
