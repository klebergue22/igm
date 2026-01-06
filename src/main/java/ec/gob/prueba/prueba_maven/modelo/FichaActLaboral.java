package ec.gob.prueba.prueba_maven.modelo;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "FICHA_ACT_LABORAL", schema = "CONSULTORIO")
@Access(AccessType.FIELD) // ✅ JPA mapea SOLO por CAMPOS
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FichaActLaboral implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
            name = "SQ_FICHA_ACT_LAB_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA_ACT_LAB",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_FICHA_ACT_LAB_GEN")
    @Column(name = "ID_FICHA_ACT_LAB", nullable = false)
    private Long idFichaActLab;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    @Column(name = "NRO_FILA", nullable = false)
    private Integer nroFila; // 1..8

    @Column(name = "CENTRO_TRABAJO", length = 250)
    private String centroTrabajo;

    @Column(name = "ACTIVIDAD", length = 500)
    private String actividad;

    @Column(name = "ES_ANTERIOR", length = 1, nullable = false)
    private String esAnterior; // 'S'/'N'

    @Column(name = "ES_ACTUAL", length = 1, nullable = false)
    private String esActual; // 'S'/'N'

    @Column(name = "TIEMPO", length = 100)
    private String tiempo;

    @Column(name = "INCIDENTE", length = 1, nullable = false)
    private String incidente; // 'S'/'N'

    @Column(name = "ACCIDENTE", length = 1, nullable = false)
    private String accidente; // 'S'/'N'

    @Column(name = "ENF_OCUPACIONAL", length = 1, nullable = false)
    private String enfOcupacional; // 'S'/'N'

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EVENTO")
    private Date fechaEvento;

    @Column(name = "ESPECIFICAR", length = 300)
    private String especificar;

    @Column(name = "OBSERVACIONES", length = 2000)
    private String observaciones;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_CREACION")
    private Date fCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_ACTUALIZACION")
    private Date fActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    @PrePersist
    public void prePersist() {
        if (fCreacion == null) fCreacion = new Date();
        if (esAnterior == null) esAnterior = "N";
        if (esActual == null) esActual = "N";
        if (incidente == null) incidente = "N";
        if (accidente == null) accidente = "N";
        if (enfOcupacional == null) enfOcupacional = "N";
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }

    // ✅ Helpers para JSF (sin anotaciones JPA en métodos)
    public boolean isAnteriorBool() { return "S".equalsIgnoreCase(esAnterior); }
    public void setAnteriorBool(boolean v) { this.esAnterior = v ? "S" : "N"; }

    public boolean isActualBool() { return "S".equalsIgnoreCase(esActual); }
    public void setActualBool(boolean v) { this.esActual = v ? "S" : "N"; }

    public boolean isIncidenteBool() { return "S".equalsIgnoreCase(incidente); }
    public void setIncidenteBool(boolean v) { this.incidente = v ? "S" : "N"; }

    public boolean isAccidenteBool() { return "S".equalsIgnoreCase(accidente); }
    public void setAccidenteBool(boolean v) { this.accidente = v ? "S" : "N"; }

    public boolean isEnfOcupacionalBool() { return "S".equalsIgnoreCase(enfOcupacional); }
    public void setEnfOcupacionalBool(boolean v) { this.enfOcupacional = v ? "S" : "N"; }
}
