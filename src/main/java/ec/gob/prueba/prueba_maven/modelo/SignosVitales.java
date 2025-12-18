/*
 * Modelo JPA para CONSULTORIO.SIGNOS_VITALES
 */
package ec.gob.prueba.prueba_maven.modelo;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SIGNOS_VITALES", schema = "CONSULTORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SignosVitales implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
        name = "SIGNOS_GEN",
        sequenceName = "CONSULTORIO.SQ_SIGNOS",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SIGNOS_GEN")
    @Column(name = "ID_SIGNOS", nullable = false)
    private Long idSignos;

    // ========================
    // SIGNOS CLÍNICOS
    // ========================

    // Temperatura (°C)
    @Column(name = "TEMPERATURA_C", precision = 4, scale = 1)
    private Double temperaturaC;

    // Presión arterial
    @Column(name = "PA_SISTOLICA")
    private Integer paSistolica;

    @Column(name = "PA_DIASTOLICA")
    private Integer paDiastolica;

    // Frecuencia cardíaca (FC)
    @Column(name = "FRECUENCIA_CARD")
    private Integer frecuenciaCard;

    // Frecuencia respiratoria (FR)
    @Column(name = "FRECUENCIA_RESP")
    private Integer frecuenciaResp;

    // Saturación de oxígeno (%)
    @Column(name = "SAT_O2")
    private Integer satO2;

    // Peso (kg)
    @Column(name = "PESO_KG", precision = 6, scale = 2)
    private Double pesoKg;

    // Talla (m)
    @Column(name = "TALLA_M", precision = 4, scale = 2)
    private Double tallaM;

    // IMC calculado por la BD (columna virtual)
    @Column(name = "IMC", precision = 6, scale = 2, insertable = false, updatable = false)
    private Double imc;

    // Perímetro abdominal (cm)
    @Column(name = "PERIMETRO_ABD_CM", precision = 5, scale = 1)
    private Double perimetroAbdCm;

    // ========================
    // AUDITORÍA
    // ========================

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
}
