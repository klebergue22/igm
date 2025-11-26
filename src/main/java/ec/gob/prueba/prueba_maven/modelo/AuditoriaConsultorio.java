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
 

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "AUDITORIA_CONSULTORIO", schema = "CONSULTORIO")
public class AuditoriaConsultorio implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqAuditoriaConsultorio")
    @SequenceGenerator(
            name = "seqAuditoriaConsultorio",
            sequenceName = "CONSULTORIO.SQ_AUDITORIA_CONSULTORIO",
            allocationSize = 1
    )
    @Column(name = "ID")
    private Long id;

    @Column(name = "MODULO", nullable = false, length = 100)
    private String modulo;

    @Column(name = "USUARIO", nullable = false, length = 50)
    private String usuario;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA", nullable = false)
    private Date fecha;

    @Column(name = "ACCION", nullable = false, length = 50)
    private String accion;

    @Column(name = "TABLA_AFECTA", nullable = false, length = 100)
    private String tablaAfecta;

    @Column(name = "CAMPO_AFECTA", length = 50)
    private String campoAfecta;

    @Column(name = "OBSERVACIONES", length = 1000)
    private String observaciones;
}
