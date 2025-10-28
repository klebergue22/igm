package ec.gob.prueba.prueba_maven.web;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.primefaces.event.SelectEvent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author GUERRA_KLEBER
 */
@Named("centroMedicoCtrl")
@ViewScoped
@Getter
@Setter
@ToString
public class CentroMedicoCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    // ========= A. DATOS DEL ESTABLECIMIENTO / USUARIO =========
    private String institucion;
    private String ruc;
    private String ciiu;
    private String centroTrabajo;

    private String noHistoria;
    private String noArchivo;

    private String apellido1;
    private String apellido2;
    private String nombre1;
    private String nombre2;

    // Atención prioritaria
    private boolean apEmbarazada;
    private boolean apDiscapacidad;
    private boolean apCatastrofica;
    private boolean apLactancia;
    private boolean apAdultoMayor;

    // Sexo (M o F)
    private String sexo;

    // Fecha nacimiento / Edad
    private Date fechaNacimiento;
    private Integer edad;

    // Grupo sanguíneo / Lateralidad
    private String grupoSanguineo;
    private String lateralidad;

    // ========= C. ANTECEDENTES PERSONALES =========
    private String antClinicoQuirurgico;
    private String antFamiliares;
    private String condicionEspecial;

    private String autorizaTransfusion;
    private String tratamientoHormonal;
    private String tratamientoHormonalCual;

    // ---- Solo hombres ----
    private String examenReproMasculino;
    private Integer tiempoReproMasculino;

    // ---- Solo mujeres ----
    private Date fum; // Fecha última menstruación
    private Integer gestas;
    private Integer partos;
    private Integer cesareas;
    private Integer abortos;
    private String planificacion;
    private String planificacionCual;

    @PostConstruct
    public void init() {
        sexo = "M"; // valor inicial por defecto
        edad = null;
        grupoSanguineo = "";
        lateralidad = "";
        planificacion = "";
    }

    // ================== MÉTODOS DE LÓGICA ==================

    /** Se llama desde el <p:ajax> del calendario */
    public void calcularEdad(SelectEvent<Date> event) {
        if (event != null) {
            this.fechaNacimiento = event.getObject();
        }
        calcularEdad();
    }

    /** Cálculo de edad independiente del evento */
    public void calcularEdad() {
        if (fechaNacimiento == null) {
            edad = null;
            return;
        }

        LocalDate nacimiento = fechaNacimiento.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate hoy = LocalDate.now();

        edad = Period.between(nacimiento, hoy).getYears();
        if (edad < 0) edad = null;
    }
}