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
import java.util.List;

@Entity
@Table(name = "CONSULTA_MEDICA", schema = "CONSULTORIO")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"empleado", "diagnosticos"})
public class ConsultaMedica implements Serializable {

    @Id
    @SequenceGenerator(
        name = "CONSULTA_GEN",
        sequenceName = "CONSULTORIO.SQ_CONSULTA",  // ⚠ crea esta secuencia si aún no existe
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONSULTA_GEN")
    @Column(name = "ID_CONSULTA", nullable = false)
    private Long idConsulta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_PERSONA", nullable = false)
    private DatEmpleado empleado;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_CONSULTA", nullable = false)
    private Date fechaConsulta;

    @Column(name = "MOTIVO_CONSULTA", length = 1000)
    private String motivoConsulta;

    @Column(name = "ENFERMEDAD_ACTUAL", length = 2000)
    private String enfermedadActual;

    @Column(name = "EXAMEN_FISICO", length = 2000)
    private String examenFisico;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SIGNOS")
    private SignosVitales signos;


    @Column(name = "TEMPERATURA_C", precision = 4, scale = 1)
    private Double temperaturaC;

    @Column(name = "PA_SISTOLICA")
    private Integer paSistolica;

    @Column(name = "PA_DIASTOLICA")
    private Integer paDiastolica;

    @Column(name = "FRECUENCIA_CARD")
    private Integer frecuenciaCard;

    @Column(name = "PESO_KG", precision = 6, scale = 2)
    private Double pesoKg;

    @Column(name = "TALLA_M", precision = 4, scale = 2)
    private Double tallaM;

    @Column(name = "IMC", precision = 6, scale = 2, insertable = false, updatable = false)
    private Double imc; // virtual

    @Column(name = "MEDICO_NOMBRE", length = 150)
    private String medicoNombre;

    @Column(name = "MEDICO_CODIGO", length = 50)
    private String medicoCodigo;
    
    @Column(name = "ESTADO", length = 20)
private String estado;


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

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsultaDiagnostico> diagnosticos;
    
}
