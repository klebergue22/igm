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
@Table(name = "FICHA_EXAMEN_COMP", schema = "CONSULTORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FichaExamenComp implements Serializable {

    @Id
    @SequenceGenerator(
        name = "SQ_FICHA_EXAMEN_COMP_GEN",
        sequenceName = "CONSULTORIO.SQ_FICHA_EXAMEN_COMP",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_FICHA_EXAMEN_COMP_GEN")
    @Column(name = "ID_FICHA_EXAMEN", nullable = false)
    private Long idFichaExamen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    @Column(name = "NRO_FILA")
    private Integer nroFila;

    @Column(name = "NOMBRE_EXAMEN", length = 200)
    private String nombreExamen;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EXAMEN")
    private Date fechaExamen;

    @Column(name = "RESULTADO", length = 2000)
    private String resultado;

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
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }
}

