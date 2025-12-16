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
@Table(name = "FICHA_OCUPACIONAL", schema = "CONSULTORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"empleado", "signos", "cie10Principal", "personaAux"})
public class FichaOcupacional implements Serializable {

    @Id
    @SequenceGenerator(
        name = "FICHA_OCUP_GEN",
        sequenceName = "CONSULTORIO.SQ_FICHA",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FICHA_OCUP_GEN")
    @Column(name = "ID_FICHA", nullable = false)
    private Long idFicha;

    // === Relación con empleado (RH.T_DAT_EMPLEADO)
    // OJO: puede ser NULL si se usa PERSONA_AUX (regla CHECK en BD)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_PERSONA")   // sin nullable=false
    private DatEmpleado empleado;

    // === Persona auxiliar (CONSULTORIO.PERSONA_AUX.ID_PERSONA_AUX)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA_AUX")
    private PersonaAux personaAux;

    // === Signos vitales asociados (SIGNOS_VITALES.ID_SIGNOS)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SIGNOS")
    private SignosVitales signos;

    // === CIE10 principal (COD_CIE10_PPAL -> CIE10.CODIGO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_CIE10_PPAL", referencedColumnName = "CODIGO")
    private Cie10 cie10Principal;

    // ==== Campos de la tabla ====

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EVALUACION", nullable = false)
    private Date fechaEvaluacion;

    @Column(name = "TIPO_EVALUACION", length = 20, nullable = false)
    private String tipoEvaluacion;  // INGRESO, PERIODICA, etc.

    // Atención prioritaria
    @Column(name = "AP_EMBARAZADA", length = 1)
    private String apEmbarazada;    // 'S'/'N'

    @Column(name = "AP_DISCAPACIDAD", length = 1)
    private String apDiscapacidad;

    @Column(name = "AP_CATASTROFICA", length = 1)
    private String apCatastrofica;

    @Column(name = "AP_LACTANCIA", length = 1)
    private String apLactancia;

    @Column(name = "AP_ADULTO_MAYOR", length = 1)
    private String apAdultoMayor;

    // Antecedentes
    @Column(name = "ANT_CLINICO_QUIR", length = 2000)
    private String antClinicoQuir;

    @Column(name = "ANT_FAMILIARES", length = 2000)
    private String antFamiliares;

    @Column(name = "CONDICION_ESPECIAL", length = 500)
    private String condicionEspecial;

    @Column(name = "AUTORIZA_TRANSFUSION", length = 2)
    private String autorizaTransfusion; // SI/NO

    @Column(name = "TRAT_HORMONAL", length = 2)
    private String tratHormonal; // SI/NO

    @Column(name = "TRAT_HORMONAL_CUAL", length = 500)
    private String tratHormonalCual;

    @Column(name = "EXAM_REPRO_MASC", length = 500)
    private String examReproMasc;

    @Column(name = "TIEMPO_REPRO_MASC")
    private Integer tiempoReproMasc;

    // Gineco-obstétricos
    @Temporal(TemporalType.DATE)
    @Column(name = "FUM")
    private Date fum;

    @Column(name = "GESTAS")
    private Integer gestas;

    @Column(name = "PARTOS")
    private Integer partos;

    @Column(name = "CESAREAS")
    private Integer cesareas;

    @Column(name = "ABORTOS")
    private Integer abortos;

    @Column(name = "PLANIFICACION", length = 50)
    private String planificacion;

    @Column(name = "PLANIFICACION_CUAL", length = 200)
    private String planificacionCual;

    // Aptitud
    @Column(name = "APTITUD_SEL", length = 20, nullable = false)
    private String aptitudSel; // APTO / APTO_EN_OBS / APTO_LIMIT / NO_APTO

    @Column(name = "DETALLE_OBS", length = 2000)
    private String detalleObs;

    @Column(name = "RECOMENDACIONES", length = 2000)
    private String recomendaciones;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EMISION", nullable = false)
    private Date fechaEmision;

    @Column(name = "MEDICO_NOMBRE", length = 150)
    private String medicoNombre;

    @Column(name = "MEDICO_CODIGO", length = 50)
    private String medicoCodigo;

    @Column(name = "ESTADO", length = 20)
    private String estado; // EMITIDA, ANULADA, etc.

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

 // ===== Auditoría básica / defaults =====
@PrePersist
public void prePersist() {
    Date ahora = new Date();

    // Estado por defecto para guardado progresivo
    if (estado == null || estado.trim().isEmpty()) {
        estado = "BORRADOR"; // <-- mejor para wizard (no EMITIDA)
    }

    // Auditoría
    if (fechaCreacion == null) {
        fechaCreacion = ahora;
    }

    // *** CLAVE: evitar ORA-01400 en FECHA_EMISION ***
    if (fechaEmision == null) {
        fechaEmision = ahora;
    }

    // *** RECOMENDADO: evitar ORA-01400 en APTITUD_SEL mientras sea BORRADOR ***
    if ((aptitudSel == null || aptitudSel.trim().isEmpty())
            && "BORRADOR".equalsIgnoreCase(estado)) {
        aptitudSel = "PENDIENTE"; // o "N/A" según tu catálogo/valores permitidos
    }
}


    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
    }
}
