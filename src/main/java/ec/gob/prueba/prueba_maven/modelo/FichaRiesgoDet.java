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
import java.util.Date;

@Entity
@Table(name = "FICHA_RIESGO_DET", schema = "CONSULTORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FichaRiesgoDet implements Serializable {

    @Id
    @SequenceGenerator(
        name = "SQ_FICHA_RIESGO_DET_GEN",
        sequenceName = "CONSULTORIO.SQ_FICHA_RIESGO_DET",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_FICHA_RIESGO_DET_GEN")
    @Column(name = "ID_FICHA_RIESGO_DET", nullable = false)
    private Long idFichaRiesgoDet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    @Column(name = "GRUPO", length = 30, nullable = false)
    private String grupo; // FISICO/QUIMICO/BIOLOGICO/ERGONOMICO/SEGURIDAD/PSICOSOCIAL

    @Column(name = "ITEM", length = 300, nullable = false)
    private String item; // Texto del riesgo tal cual Excel

    @Column(name = "ACTIVIDAD_NRO", nullable = false)
    private Integer actividadNro; // 1..7

    @Column(name = "MARCADO", length = 1, nullable = false)
    private String marcado; // 'S'/'N'

    @Column(name = "ORDEN")
    private Integer orden;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    @PrePersist
    public void prePersist() {
        if (fCreacion == null) fCreacion = new Date();
        if (marcado == null) marcado = "N";
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }
}

