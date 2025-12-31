/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo;

/**
 * Ficha de riesgos laborales (Step 2)
 *
 * Tabla: CONSULTORIO.FICHA_RIESGO
 */
import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "FICHA_RIESGO", schema = "CONSULTORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"ficha"})
public class FichaRiesgo implements Serializable {

    @Id
    @SequenceGenerator(
            name = "FICHA_RIESGO_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA_RIESGO",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FICHA_RIESGO_GEN")
    @Column(name = "ID_FICHA_RIESGO", nullable = false)
    private Long idFichaRiesgo;

    // ===== Relación con FICHA_OCUPACIONAL =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    // ===== Datos generales del puesto / actividades =====
    @Column(name = "PUESTO_TRABAJO", length = 200)
    private String puestoTrabajo;

    @Column(name = "ACTIVIDAD_1", length = 500)
    private String actividad1;

    @Column(name = "ACTIVIDAD_2", length = 500)
    private String actividad2;

    @Column(name = "ACTIVIDAD_3", length = 500)
    private String actividad3;

    @Column(name = "ACTIVIDAD_4", length = 500)
    private String actividad4;

    @Column(name = "ACTIVIDAD_5", length = 500)
    private String actividad5;

    @Column(name = "ACTIVIDAD_6", length = 500)
    private String actividad6;

    @Column(name = "ACTIVIDAD_7", length = 500)
    private String actividad7;

    // ===== Resumen de factores de riesgo por bloque =====
    @Column(name = "RIESGOS_FISICOS", length = 2000)
    private String riesgosFisicos;

    @Column(name = "RIESGOS_SEGURIDAD", length = 2000)
    private String riesgosSeguridad;

    @Column(name = "RIESGOS_QUIMICOS", length = 2000)
    private String riesgosQuimicos;

    @Column(name = "RIESGOS_BIOLOGICOS", length = 2000)
    private String riesgosBiologicos;

    @Column(name = "RIESGOS_ERGONOMICOS", length = 2000)
    private String riesgosErgonomicos;

    @Column(name = "RIESGOS_PSICOSOCIALES", length = 2000)
    private String riesgosPsicosociales;

    @Column(name = "OBSERVACIONES", length = 2000)
    private String observaciones;

    // ===== Auditoría =====
    @Column(name = "ESTADO", length = 20)
    private String estado; // p.ej. ACTIVO / INACTIVO / BORRADOR

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fechaActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    @Column(name = "MEDIDAS_PREVENTIVAS", length = 2000)
    private String medidasPreventivas;

    // ===== Hooks de auditoría =====
    @PrePersist
    public void prePersist() {
        if (estado == null || estado.trim().isEmpty()) {
            estado = "ACTIVO";   // o el valor que decidan manejar como default
        }
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
    }
}
