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
@Table(name = "PERSONA_AUX", schema = "CONSULTORIO")
@NamedQueries({
    @NamedQuery(name = "PersonaAux.findAll",
            query = "SELECT p FROM PersonaAux p ORDER BY p.idPersonaAux"),

    @NamedQuery(name = "PersonaAux.findByCedula",
            query = "SELECT p FROM PersonaAux p WHERE p.cedula = :cedula"),

    @NamedQuery(name = "PersonaAux.findPendientes",
            query = "SELECT p FROM PersonaAux p WHERE p.estado = 'PENDIENTE'"),

    @NamedQuery(name = "PersonaAux.findVinculados",
            query = "SELECT p FROM PersonaAux p WHERE p.estado = 'VINCULADO'")
})
public class PersonaAux implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "seqPersonaAux")
    @SequenceGenerator(
            name = "seqPersonaAux",
            sequenceName = "CONSULTORIO.SQ_PERSONA_AUX",
            allocationSize = 1)
    @Column(name = "ID_PERSONA_AUX")
    private Long idPersonaAux;

    @Column(name = "CEDULA", length = 10, nullable = false)
    private String cedula;

    // ====================================
    // NUEVOS CAMPOS REALES PARA TU PANTALLA
    // ====================================
    @Column(name = "APELLIDO1", length = 50)
    private String apellido1;

    @Column(name = "APELLIDO2", length = 50)
    private String apellido2;

    @Column(name = "NOMBRE1", length = 50)
    private String nombre1;

    @Column(name = "NOMBRE2", length = 50)
    private String nombre2;

    // CAMPOS COMPUESTOS (compatibilidad)
    @Column(name = "NOMBRES", length = 100)
    private String nombres;

    @Column(name = "APELLIDOS", length = 100)
    private String apellidos;

    @Column(name = "SEXO", length = 1)
    private String sexo;

    @Column(name = "FECHA_NAC")
    @Temporal(TemporalType.DATE)
    private Date fechaNac;

    // NO_PERSONA cuando se vincule con RRHH
    @Column(name = "NO_PERSONA")
    private Long noPersona;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    @Column(name = "F_CREACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Column(name = "F_ACTUALIZACION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    // ===========================
    // CONSTRUCTOR
    // ===========================
    public PersonaAux() {
        this.estado = "PENDIENTE";
        this.fechaCreacion = new Date();
    }

    // ==========================================
    // SETTERS AUTOMÁTICOS PARA CAMPOS COMPUESTOS
    // ==========================================
    @PrePersist
    @PreUpdate
    public void sincronizarCamposCompuestos() {

        // NOMBRES = nombre1 + nombre2
        StringBuilder nom = new StringBuilder();
        if (nombre1 != null) nom.append(nombre1);
        if (nombre2 != null && !nombre2.isEmpty())
            nom.append(" ").append(nombre2);
        this.nombres = nom.toString().trim();

        // APELLIDOS = apellido1 + apellido2
        StringBuilder ape = new StringBuilder();
        if (apellido1 != null) ape.append(apellido1);
        if (apellido2 != null && !apellido2.isEmpty())
            ape.append(" ").append(apellido2);
        this.apellidos = ape.toString().trim();
    }
}
