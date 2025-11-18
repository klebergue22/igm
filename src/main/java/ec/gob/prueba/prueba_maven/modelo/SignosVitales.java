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
@Table(name = "SIGNOS_VITALES", schema = "CONSULTORIO")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class SignosVitales implements Serializable {

    @Id
    @SequenceGenerator(
        name = "SIGNOS_GEN",
        sequenceName = "CONSULTORIO.SQ_SIGNOS",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SIGNOS_GEN")
    @Column(name = "ID_SIGNOS", nullable = false)
    private Long idSignos;

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
    private Double imc; // calculado por la DB

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;
}

