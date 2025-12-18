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
@Table(name = "FICHA_DIAGNOSTICO", schema = "CONSULTORIO",
       uniqueConstraints = @UniqueConstraint(name="UK_FD_FICHA_CIE10", columnNames={"ID_FICHA","COD_CIE10"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FichaDiagnostico implements Serializable {

    @Id
    @SequenceGenerator(name="FD_GEN", sequenceName="CONSULTORIO.SQ_FICHA_DIAG", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="FD_GEN")
    @Column(name="ID_FICHA_DIAG", nullable=false)
    private Long idFichaDiag;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="ID_FICHA", nullable=false)
    private FichaOcupacional ficha;

    @Column(name="COD_CIE10", length=10, nullable=false)
    private String codCie10;

    @Column(name="DESCRIPCION", length=500)
    private String descripcion;

    @Column(name="TIPO_DIAG", length=1, nullable=false)
    private String tipoDiag; // P / S

    @Column(name="ORDEN", nullable=false)
    private Integer orden;

    @Column(name="ESTADO", length=20)
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="F_CREACION")
    private Date fechaCreacion;

    @Column(name="USR_CREACION", length=30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="F_ACTUALIZACION")
    private Date fechaActualizacion;

    @Column(name="USR_ACTUALIZACION", length=30)
    private String usrActualizacion;
}

