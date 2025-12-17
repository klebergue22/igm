/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.pdf.BaseFont;

import ec.gob.prueba.prueba_maven.modelo.Cie10;
import ec.gob.prueba.prueba_maven.modelo.ConsultaDiagnostico;
import ec.gob.prueba.prueba_maven.modelo.ConsultaMedica;
import ec.gob.prueba.prueba_maven.modelo.DatEmpleado;
import ec.gob.prueba.prueba_maven.modelo.FichaOcupacional;
import ec.gob.prueba.prueba_maven.modelo.FichaRiesgo;
import ec.gob.prueba.prueba_maven.modelo.PersonaAux;
import ec.gob.prueba.prueba_maven.modelo.SignosVitales;
import ec.gob.prueba.prueba_maven.modelo.AuditoriaConsultorio;
import ec.gob.prueba.prueba_maven.servicio.Cie10Service;
import ec.gob.prueba.prueba_maven.servicio.EmpleadoService;
import ec.gob.prueba.prueba_maven.servicio.FichaOcupacionalService;
import ec.gob.prueba.prueba_maven.servicio.FichaRiesgoService;
import ec.gob.prueba.prueba_maven.servicio.PersonaAuxService;
import ec.gob.prueba.prueba_maven.servicio.SignosVitalesService;
import ec.gob.prueba.prueba_maven.servicio.AuditoriaConsultorioService;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
@ManagedBean(name = "centroMedicoCtrl")
@ViewScoped
@Getter
@Setter
@ToString
public class CentroMedicoCtrl implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(CentroMedicoCtrl.class.getName());

    // ====== BÚSQUEDA POR CÉDULA / PERSONA AUXILIAR ======
    private String cedulaBusqueda;          // se enlaza al inputText del popup
    private DatEmpleado empleadoSeleccionado;
    private PersonaAux personaAux;          // registro auxiliar cuando no existe en RRHH
    private boolean mostrarDialogoAux;      // para controlar la visualización del diálogo
    private boolean permitirIngresoManual;  // <-- NUEVO: controla el botón "Ingresar manualmente"
    private boolean mostrarDlgCedula = true; //Dialogo de la cedula 
    // ===============================
// 🔑 PK DEL EMPLEADO (ANTI-JSF-LOSS)
// ===============================
    private Integer noPersonaSel;

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

    // Fechas / Edad
    private Date fechaNacimiento;
    private Date fechaAtencion;
    private String tipoEval;       // Selección en UI
    private Date fecIngreso;
    private Date fecReintegro;
    private Date fecRetiro;
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

    // ---- Constantes vitales
    private Double peso;   // kg (en el form está en kg)
    private Double talla;  // cm (en el form está en cm)
    private Double imc;    // kg/m2 (calculado en el bean)
    private Double temp;
    private String paStr;
    private Integer fc;
    private Integer fr;
    private Integer satO2;
    private Double tallaCm;
    private Double perimetroAbd;

    // ====== STEP 3: Datos para el certificado ====== 
    private Date fechaEmision;
    private String tipoEvaluacion; // INGRESO / PERIODICO / REINTEGRO / RETIRO

    // Aptitud
    private boolean apto;
    private boolean aptoObservacion;
    private boolean aptoLimitaciones;
    private boolean noApto;
    private String aptitudSel;  // "APTO", "APTO_EN_OBS", "APTO_LIMIT", "NO_APTO"

    private String detalleObservaciones;
    private String recomendaciones;

    private String medicoNombre;
    private String medicoCodigo;

    // ====== Vista previa / descarga ======
    private StreamedContent pdfPreview;  // (no se usa con el servlet, se deja por compatibilidad)
    private StreamedContent pdfDescarga; // (no se usa con el servlet, se deja por compatibilidad)
    private boolean certificadoListo;

    // === PREVIEW POR <object>/<iframe> via Servlet ===
    private String pdfObjectUrl; // opcional
    private String pdfToken;     // clave en sesión para el servlet

    public String getPdfObjectUrl() {
        return pdfObjectUrl;
    }

    public String getPdfToken() {
        return pdfToken;
    }

    // ========= OBJETOS DE DOMINIO PARA GUARDAR EN BD =========
    // Empleado seleccionado (llenado desde otra pantalla)
    private DatEmpleado empleadoSel;

    // Ficha ocupacional (tabla CONSULTORIO.FICHA_OCUPACIONAL)
    private FichaOcupacional ficha;

    // Signos vitales (tabla CONSULTORIO.SIGNOS_VITALES)
    private SignosVitales signos;

    // Consulta médica (tabla CONSULTORIO.CONSULTA_MEDICA) – opcional
    private ConsultaMedica consulta;

    // Lista de diagnósticos de la consulta (CONSULTORIO.CONSULTA_DIAGNOSTICO)
    private List<ConsultaDiagnostico> listaDiag;

    // Paso actual del wizard (step1, step2, step3, step4)
    private String currentStep = "step1";
    //Riesgos
    private FichaRiesgo fichaRiesgo;

    // STEP 2 - actividades 1..7 (para poder bindear en XHTML)
    private List<String> actividadesLab = new ArrayList<>();

// STEP 2 - checks de riesgos (se guardan como resumen en observaciones)
    private Map<String, Boolean> riesgos = new LinkedHashMap<>();

// STEP 2 - “otros” por columna/categoría
    private Map<String, String> otrosRiesgos = new LinkedHashMap<>();

    // Medidas preventivas seleccionadas en la matriz (STEP 2)
    private List<String> medidasPreventivas = new ArrayList<>();

    // ============================
    // CIE10 – Diagnóstico principal
    // ============================
    @EJB
    private Cie10Service cie10Service;

    @EJB
    private FichaOcupacionalService fichaService;

    @EJB
    private SignosVitalesService signosService;
    @EJB
    private FichaRiesgoService fichaRiesgoService;

    @EJB
    private EmpleadoService empleadoService;

    @EJB
    private PersonaAuxService personaAuxService;

    @EJB
    private AuditoriaConsultorioService auditoriaService;

    // campo que se guarda (código)
    private String codCie10Ppal;

    // campo que se muestra / busca (descripción)
    private String descCie10Ppal;

    @PostConstruct
    public void init() {
        mostrarDlgCedula = true;
        fechaAtencion = new Date();
        tipoEval = "INGRESO";
        sexo = "M";
        grupoSanguineo = "";
        lateralidad = "";

        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale("es"));

        institucion = "Instituto Geográfico Militar";
        institucion = institucion.toUpperCase();
        ruc = "1768007200001";

        // ====== INICIALIZAR OBJETOS DE DOMINIO ======
        ficha = new FichaOcupacional();
        signos = new SignosVitales();
        consulta = new ConsultaMedica();
        listaDiag = new ArrayList<>();
        fichaRiesgo = new FichaRiesgo();
        fichaRiesgo.setFicha(ficha);
        fichaRiesgo.setEstado("BORRADOR");
        personaAux = new PersonaAux();

        if (medidasPreventivas == null) {
            medidasPreventivas = new ArrayList<>();
        }

        // Si ya tienes empleadoSel seteado desde otra pantalla,
        // aquí lo amarras:
        if (empleadoSel != null) {
            ficha.setEmpleado(empleadoSel);
            consulta.setEmpleado(empleadoSel);
        }

        // Valores base
        ficha.setFechaEvaluacion(fechaAtencion);
        ficha.setTipoEvaluacion(tipoEval);

        // 👉 IMPORTANTE: inicializar filas de diagnósticos
        // Ajusta 6 si en tu UI tienes otro número de filas.
        for (int i = 0; i < 6; i++) {
            ConsultaDiagnostico cd = new ConsultaDiagnostico();
            // si manejas tipo P/S puedes inicializar en "P" o dejar null
            cd.setTipoDiag("P");
            listaDiag.add(cd);
        }
        if (ficha == null) {
            ficha = new FichaOcupacional();
            // set defaults si hace falta
        }

        if (fichaRiesgo == null) {
            fichaRiesgo = new FichaRiesgo();
            fichaRiesgo.setFicha(ficha);
            fichaRiesgo.setEstado("BORRADOR");
        }
        // ===== STEP 2: asegurar tamaños =====
        if (actividadesLab == null) {
            actividadesLab = new ArrayList<>();
        }
        while (actividadesLab.size() < 7) {
            actividadesLab.add(null);
        }

        if (medidasPreventivas == null) {
            medidasPreventivas = new ArrayList<>();
        }
        while (medidasPreventivas.size() < 7) {
            medidasPreventivas.add(null);
        }

        if (riesgos == null) {
            riesgos = new LinkedHashMap<>();
        }
        if (otrosRiesgos == null) {
            otrosRiesgos = new LinkedHashMap<>();
        }

    }

    private ConsultaDiagnostico ensureDiag(int index) {
        // si la lista es muy corta, agrándala
        while (listaDiag.size() <= index) {
            listaDiag.add(new ConsultaDiagnostico());
        }

        ConsultaDiagnostico d = listaDiag.get(index);
        if (d == null) {
            d = new ConsultaDiagnostico();
            listaDiag.set(index, d);
        }
        return d;
    }

    // ===== TUS LISTENERS =====
    public void onCie10BlurCodigo(int index) {
        ConsultaDiagnostico diag = ensureDiag(index);

        String codigo = diag.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            diag.setDescripcion(null);
            diag.setCie10(null);
            return;
        }

        Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
        if (cie != null) {
            diag.setDescripcion(cie.getDescripcion());
            diag.setCie10(cie);
        } else {
            diag.setDescripcion(null);
            diag.setCie10(null);
        }
    }

    // ===========================================================
    // MÉTODOS DE FECHAS Y EDAD
    // ===========================================================
    public void onFechaNacimientoSelect(SelectEvent e) {
        this.fechaNacimiento = (java.util.Date) e.getObject();
        this.edad = calcularEdad(this.fechaNacimiento);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cálculo de edad",
                        "Edad calculada: " + (edad == null ? "(sin fecha)" : edad + " años")));
    }

    public void onFechaNacimientoChange() {
        this.edad = calcularEdad(this.fechaNacimiento);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cálculo de edad",
                        "Edad calculada: " + (edad == null ? "(sin fecha)" : edad + " años")));
    }

    public void setFechaNacimiento(Date f) {
        this.fechaNacimiento = f;
        this.edad = calcularEdad(f);
    }
    // ===========================================================
    // CÁLCULO EDAD
    // ===========================================================

    public void calcularEdad() {
        this.edad = calcularEdad(this.fechaNacimiento);
    }

    private Integer calcularEdad(Date f) {
        if (f == null) {
            return null;
        }
        Calendar hoy = Calendar.getInstance();
        Calendar nac = Calendar.getInstance();
        nac.setTime(f);
        int years = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
        int mh = hoy.get(Calendar.MONTH), mn = nac.get(Calendar.MONTH);
        if (mh < mn || (mh == mn && hoy.get(Calendar.DAY_OF_MONTH) < nac.get(Calendar.DAY_OF_MONTH))) {
            years--;
        }
        return Math.max(years, 0);
    }

    public Date getFechaMaximaNacimiento() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public void validarEdadMinima() {
        if (edad != null && edad < 18) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La edad debe ser ≥ 18 años"));
            fechaNacimiento = null;
            edad = null;
        }
    }

    // ===========================================================
    // CÁLCULO IMC
    // ===========================================================
    public void recalcularIMC() {
        if (peso != null && talla != null && talla > 0) {
            double m = talla / 100.0;
            this.imc = Math.round((peso / (m * m)) * 100.0) / 100.0;
        } else {
            this.imc = null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Registra una auditoría genérica en CONSULTORIO.AUDITORIA_CONSULTORIO. Por
     * ahora el usuario se maneja como literal "USR_APP" hasta que exista login
     * real.
     */
    private void registrarAuditoria(String accion, String tabla, String campo, String observaciones) {
        try {
            AuditoriaConsultorio aud = new AuditoriaConsultorio();
            aud.setModulo("CENTRO_MEDICO");
            aud.setUsuario("USR_APP"); // TODO: reemplazar por usuario logueado cuando exista
            aud.setFecha(new Date());
            aud.setAccion(accion);
            aud.setTablaAfecta(tabla);
            aud.setCampoAfecta(campo);
            aud.setObservaciones(observaciones);
            auditoriaService.guardar(aud);
        } catch (Exception e) {
            // No romper el flujo funcional si la auditoría falla
            e.printStackTrace();
        }
    }

// ===========================================================
// WIZARD: GUARDAR POR STEP
// ===========================================================
    public void guardarStepActual() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            boolean ok = true;

            if ("step1".equals(currentStep)) {
                // (aquí más adelante puedes llamar a validarStep1 si ya lo tienes)
                guardarStep1();
                currentStep = "step2";

            } else if ("step2".equals(currentStep)) {
                // (igual para validarStep2 cuando lo tengas listo)
                 ok = validarStep2();
                guardarStep2();
                currentStep = "step3";

            } else if ("step3".equals(currentStep)) {
                ok = validarStep3();
                if (ok) {
                    guardarStep3();
                    currentStep = "step4";
                }
            }

            // Si falló alguna validación, se lo avisamos a PrimeFaces
            if (!ok) {
                ctx.validationFailed(); // esto hace que args.validationFailed sea true en el oncomplete
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "Ocurrió un error al guardar la información del paso actual."));
            ctx.validationFailed();
        }
    }

    public void retrocederStep() {
        if ("step2".equals(currentStep)) {
            currentStep = "step1";
        } else if ("step3".equals(currentStep)) {
            currentStep = "step2";
        } else if ("step4".equals(currentStep)) {
            currentStep = "step3";
        }
    }

    private boolean validarStep1() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean valido = true;

        // ===== NOMBRES Y APELLIDOS =====
        if (isBlank(apellido1) && isBlank(apellido2)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar al menos un apellido."));
            valido = false;
        }

        if (isBlank(nombre1) && isBlank(nombre2)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar al menos un nombre."));
            valido = false;
        }

        // ===== SEXO =====
        if (isBlank(sexo)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe seleccionar el sexo del paciente."));
            valido = false;
        }

        // ===== TIPO DE EVALUACIÓN =====
        if (isBlank(tipoEval)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe seleccionar el tipo de evaluación (Ingreso, Periódica, etc.)."));
            valido = false;
        }

        // ===== SIGNOS VITALES OBLIGATORIOS =====
        if (signos == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe registrar los signos vitales."));
            return false;
        }

        // PA (sistólica y diastólica)
        if (signos.getPaSistolica() == null || signos.getPaDiastolica() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar la presión arterial completa (PA sistólica y diastólica)."));
            valido = false;
        }

        // FC
        if (signos.getFrecuenciaCard() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar la frecuencia cardíaca (FC)."));
            valido = false;
        }

        // Peso
        if (signos.getPesoKg() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar el peso (kg)."));
            valido = false;
        }

        // Talla
        if (signos.getTallaM() == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar la talla (en metros o convertir desde cm)."));
            valido = false;
        }

        // ===== PUESTO DE TRABAJO =====
        if (fichaRiesgo == null || isBlank(fichaRiesgo.getPuestoTrabajo())) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 1",
                    "Debe ingresar el puesto de trabajo."));
            valido = false;
        }

        return valido;
    }

    // ==========================================================
// Helpers para mensajes Faces (WARN, INFO, ERROR)
// ==========================================================
    private void warn(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Step 1", msg));
    }

    private void info(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Step 1", msg));
    }

    private void error(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
    }

// Helpers
    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * STEP 1: - Atención prioritaria - Antecedentes personales -
     * Gineco-obstétricos (Solo llena la FichaOcupacional en memoria, aún no
     * persiste)
     */
    public void guardarStep1() {
        try {
            if (empleadoSel == null && noPersonaSel != null) {
                empleadoSel = empleadoService.buscarPorId(noPersonaSel);
            }
            // =========================================
            // 1) VALIDACIONES REQUERIDAS DEL STEP 1
            //    (lo que tú pediste que sea obligatorio)
            // =========================================

            // Apellido / Nombre
            if (esVacio(apellido1)) {
                warn("El primer apellido es obligatorio.");
                return;
            }
            if (esVacio(nombre1)) {
                warn("El primer nombre es obligatorio.");
                return;
            }

            // Sexo (muy usado en la plantilla, mejor exigirlo)
            if (esVacio(sexo)) {
                warn("Debe seleccionar el sexo del paciente.");
                return;
            }

            // RUC
            if (esVacio(ruc)) {
                warn("Debe ingresar el RUC.");
                return;
            }

            // Fecha de atención
            if (fechaAtencion == null) {
                warn("Debe ingresar la fecha de atención.");
                return;
            }

            // Tipo de evaluación
            if (esVacio(tipoEval)) {
                warn("Debe seleccionar el tipo de evaluación (Ingreso, Periódica, etc.).");
                return;
            }

            // PA, FC, Peso, Talla (obligatorios)
            if (esVacio(paStr)) {
                warn("Debe ingresar la presión arterial (PA) en formato 120/80.");
                return;
            }
            if (fc == null) {
                warn("Debe ingresar la frecuencia cardíaca (FC).");
                return;
            }
            if (peso == null || peso <= 0) {
                warn("Debe ingresar el peso (kg).");
                return;
            }

            // Si tallaCm viene nulo pero usas 'talla', intentamos aprovecharla
            if (tallaCm == null && talla != null) {
                tallaCm = talla;
            }
            if (tallaCm == null || tallaCm <= 0) {
                warn("Debe ingresar la talla (cm).");
                return;
            }

            // Paciente: empleado RRHH o persona auxiliar
            if (empleadoSel == null && (personaAux == null || personaAux.getIdPersonaAux() == null)) {
                warn("Debe seleccionar un empleado de RRHH o registrar una persona auxiliar.");
                return;
            }

            // =========================================
            // 2) DETERMINAR ORIGEN DEL PACIENTE
            //    Y NÚMERO DE HISTORIA CLÍNICA
            // =========================================
            String cedulaPaciente;

            if (empleadoSel != null) {
                // Paciente desde RRHH
                ficha.setEmpleado(empleadoSel);
                ficha.setPersonaAux(null);

                cedulaPaciente = empleadoSel.getNoCedula();
            } else {
                // Paciente desde tabla auxiliar
                ficha.setPersonaAux(personaAux);
                ficha.setEmpleado(null);

                cedulaPaciente = personaAux.getCedula();
            }

            // N° Historia clínica = cédula del paciente
            this.noHistoria = cedulaPaciente;
            // (si la FichaOcupacional tiene campo para N° historia, aquí lo podrías setear)

            // =========================================
            // 3) MAPEAR CAMPOS DEL STEP 1 A FICHA_OCUPACIONAL
            // =========================================
            // Fecha y tipo de evaluación
            ficha.setFechaEvaluacion(fechaAtencion);
            ficha.setTipoEvaluacion(tipoEval);

            // Atención prioritaria: S/N
            ficha.setApEmbarazada(apEmbarazada ? "S" : "N");
            ficha.setApDiscapacidad(apDiscapacidad ? "S" : "N");
            ficha.setApCatastrofica(apCatastrofica ? "S" : "N");
            ficha.setApLactancia(apLactancia ? "S" : "N");
            ficha.setApAdultoMayor(apAdultoMayor ? "S" : "N");

            // Antecedentes personales
            ficha.setAntClinicoQuir(antClinicoQuirurgico);
            ficha.setAntFamiliares(antFamiliares);
            ficha.setCondicionEspecial(condicionEspecial);

            // Transfusión / tratamientos hormonales
            ficha.setAutorizaTransfusion(autorizaTransfusion);
            ficha.setTratHormonal(tratamientoHormonal);
            ficha.setTratHormonalCual(tratamientoHormonalCual);

            // Reproductivos masculinos
            ficha.setExamReproMasc(examenReproMasculino);
            ficha.setTiempoReproMasc(tiempoReproMasculino);

            // Gineco-obstétricos (mujer)
            ficha.setFum(fum);
            ficha.setGestas(gestas);
            ficha.setPartos(partos);
            ficha.setCesareas(cesareas);
            ficha.setAbortos(abortos);
            ficha.setPlanificacion(planificacion);
            ficha.setPlanificacionCual(planificacionCual);

            // =========================================
            // 4) ARMAR / GUARDAR SIGNOS_VITALES
            //    Y ENLAZAR A LA FICHA
            // =========================================
            // Parsear PA "120/80"
            Integer paSis;
            Integer paDias;
            try {
                String[] parts = paStr.split("/");
                paSis = Integer.valueOf(parts[0].trim());
                paDias = Integer.valueOf(parts[1].trim());
            } catch (Exception ex) {
                warn("El formato de PA debe ser 120/80 (números enteros separados por '/').");
                return;
            }

            // Si la ficha ya tiene signos, se reutiliza, si no creamos uno nuevo
            SignosVitales sv = ficha.getSignos();
            if (sv == null) {
                sv = new SignosVitales();
            }

            // Mapear signos desde los atributos del controlador
            sv.setTemperaturaC(temp);
            sv.setPaSistolica(paSis);
            sv.setPaDiastolica(paDias);
            sv.setFrecuenciaCard(fc);
            sv.setFrecuenciaResp(fr);
            sv.setSatO2(satO2);
            sv.setPesoKg(peso);

            // talla viene en cm → se guarda en metros en la tabla
            Double tallaM = tallaCm != null ? (tallaCm / 100.0) : null;
            sv.setTallaM(tallaM);
            sv.setPerimetroAbdCm(perimetroAbd);

            Date ahora = new Date();
            if (sv.getIdSignos() == null) {
                sv.setFechaCreacion(ahora);
                sv.setUsrCreacion("USR_APP"); // TODO: usuario real / login
            }

            // Guarda/actualiza SIGNOS_VITALES (IMC lo calcula la BD)
            sv = signosService.guardar(sv);
            this.signos = sv;      // mantener el atributo del bean sincronizado
            ficha.setSignos(sv);   // enlazar a la ficha ocupacional

            // =========================================
            // 5) AUDITORÍA + GUARDADO FICHA_OCUPACIONAL
            // =========================================
            if (ficha.getIdFicha() == null) {
                ficha.setEstado("BORRADOR");
                ficha.setFechaCreacion(ahora);
                ficha.setUsrCreacion("USR_APP"); // TODO: usuario real
            } else {
                ficha.setFechaActualizacion(ahora);
                ficha.setUsrActualizacion("USR_APP");
            }

            // Guardar BORRADOR de la ficha
            ficha = fichaService.guardar(ficha);

            // Auditoría (una por ficha y una por signos)
            registrarAuditoria("GUARDAR_STEP1",
                    "FICHA_OCUPACIONAL",
                    "*",
                    "Step 1: datos generales guardados. ID_FICHA=" + ficha.getIdFicha());

            registrarAuditoria("GUARDAR_STEP1",
                    "SIGNOS_VITALES",
                    "*",
                    "Signos vitales guardados. ID_SIGNOS=" + sv.getIdSignos());

            info("Datos del Step 1 guardados correctamente (borrador).");

        } catch (Exception e) {
            log.log(Level.SEVERE, "Error en guardarStep1", e);
            error("Ocurrió un error al guardar el Step 1.");
        }
    }

    private boolean validarStep2() {
    FacesContext ctx = FacesContext.getCurrentInstance();
    boolean valido = true;

    // Puesto de trabajo
    if (fichaRiesgo == null || isBlank(fichaRiesgo.getPuestoTrabajo())) {
        ctx.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, "Step 2",
                "Debe ingresar el puesto de trabajo."));
        valido = false;
    }

    // Al menos 1 actividad (desde lista actividadesLab)
    boolean hayActividad = false;
    if (actividadesLab != null) {
        for (String a : actividadesLab) {
            if (!isBlank(a)) { hayActividad = true; break; }
        }
    }
    if (!hayActividad) {
        ctx.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, "Step 2",
                "Debe registrar al menos una actividad laboral."));
        valido = false;
    }

    // Al menos 1 medida preventiva (texto)
    boolean hayMedida = false;
    if (medidasPreventivas != null) {
        for (String m : medidasPreventivas) {
            if (!isBlank(m)) { hayMedida = true; break; }
        }
    }
    if (!hayMedida) {
        ctx.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_ERROR, "Step 2",
                "Debe registrar al menos una medida preventiva."));
        valido = false;
    }

    return valido;
}


    /**
     * STEP 2: Riesgos (matriz G). Por ahora solo actualiza la ficha (BORRADOR)
     * para dejar registro de que se pasó por este paso. Cuando tengas la tabla
     * de riesgos, aquí se mapea y persiste.
     */
   public void guardarStep2() {
    FacesContext ctx = FacesContext.getCurrentInstance();

    if (!validarStep2()) {
        ctx.validationFailed();
        return;
    }

    Date ahora = new Date();

    if (fichaRiesgo == null) {
        fichaRiesgo = new FichaRiesgo();
    }
    fichaRiesgo.setFicha(ficha);

    // 1) Copiar actividadesLab -> campos actividad1..7 (si existen en tu entidad)
    //    (si tu entidad NO tiene estos setters, me dices y lo adapto)
    fichaRiesgo.setActividad1(actividadesLab.get(0));
    fichaRiesgo.setActividad2(actividadesLab.get(1));
    fichaRiesgo.setActividad3(actividadesLab.get(2));
    fichaRiesgo.setActividad4(actividadesLab.get(3));
    fichaRiesgo.setActividad5(actividadesLab.get(4));
    fichaRiesgo.setActividad6(actividadesLab.get(5));
    fichaRiesgo.setActividad7(actividadesLab.get(6));

    // 2) Armar resumen de medidas
    StringBuilder med = new StringBuilder();
    for (int i = 0; i < medidasPreventivas.size(); i++) {
        String m = medidasPreventivas.get(i);
        if (!isBlank(m)) {
            if (med.length() > 0) med.append(" | ");
            med.append("M").append(i + 1).append(": ").append(m.trim());
        }
    }

    // 3) Armar resumen de checks marcados (riesgos)
    StringBuilder r = new StringBuilder();
    if (riesgos != null) {
        for (Map.Entry<String, Boolean> e : riesgos.entrySet()) {
            if (Boolean.TRUE.equals(e.getValue())) {
                if (r.length() > 0) r.append(", ");
                r.append(e.getKey());
            }
        }
    }

    // 4) Armar resumen de “otros”
    StringBuilder o = new StringBuilder();
    if (otrosRiesgos != null) {
        for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
            if (!isBlank(e.getValue())) {
                if (o.length() > 0) o.append(" | ");
                o.append(e.getKey()).append(": ").append(e.getValue().trim());
            }
        }
    }

    // 5) Guardar todo en observaciones (sin perder nada)
    StringBuilder obs = new StringBuilder();
    if (med.length() > 0) obs.append("MEDIDAS: ").append(med);
    if (r.length() > 0) {
        if (obs.length() > 0) obs.append(" || ");
        obs.append("RIESGOS: ").append(r);
    }
    if (o.length() > 0) {
        if (obs.length() > 0) obs.append(" || ");
        obs.append("OTROS: ").append(o);
    }
    fichaRiesgo.setObservaciones(obs.toString());

    // Auditoría / estado
    if (fichaRiesgo.getIdFichaRiesgo() == null) {
        fichaRiesgo.setEstado("BORRADOR");
        fichaRiesgo.setFechaCreacion(ahora);
        fichaRiesgo.setUsrCreacion("USR_APP");
    } else {
        fichaRiesgo.setFechaActualizacion(ahora);
        fichaRiesgo.setUsrActualizacion("USR_APP");
    }

    fichaRiesgo = fichaRiesgoService.guardar(fichaRiesgo);

    registrarAuditoria("GUARDAR_STEP2", "FICHA_RIESGO", "*",
            "Step 2 guardado. ID_FICHA=" + (ficha != null ? ficha.getIdFicha() : null));

    ctx.addMessage(null, new FacesMessage(
            FacesMessage.SEVERITY_INFO, "Step 2",
            "Riesgos laborales guardados correctamente."));
}


    /**
     * Validaciones del STEP 3: - Al menos 1 diagnóstico - Aptitud seleccionada
     * - Al menos 1 recomendación - Nombre del profesional - Código del médico
     */
    private boolean validarStep3() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean valido = true;

        // 1) Al menos un diagnóstico en listaDiag
        boolean hayDiagnostico = false;
        if (listaDiag != null) {
            for (ConsultaDiagnostico d : listaDiag) {
                if (d == null) {
                    continue;
                }
                boolean tieneCodigo = !isBlank(d.getCodigo());
                boolean tieneDescripcion = !isBlank(d.getDescripcion());
                boolean tieneCie = d.getCie10() != null;

                if (tieneCodigo || tieneDescripcion || tieneCie) {
                    hayDiagnostico = true;
                    break;
                }
            }
        }

        if (!hayDiagnostico) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 3",
                    "Debe registrar al menos un diagnóstico (CIE10) en la sección de diagnósticos."));
            valido = false;
        }

        // 2) Aptitud
        if (isBlank(aptitudSel)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 3",
                    "Debe seleccionar la aptitud médica (APTO, APTO EN OBS, APTO CON LIMITACIONES o NO APTO)."));
            valido = false;
        }

        // 3) Al menos una recomendación
        if (isBlank(recomendaciones)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 3",
                    "Debe ingresar al menos una recomendación para el trabajador."));
            valido = false;
        }

        // 4) Nombre del profesional
        if (isBlank(medicoNombre)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 3",
                    "Debe ingresar el nombre del profesional que emite el certificado."));
            valido = false;
        }

        // 5) Código del médico
        if (isBlank(medicoCodigo)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Step 3",
                    "Debe ingresar el código del médico (registro profesional)."));
            valido = false;
        }

        return valido;
    }

    /**
     * STEP 3: - Signos vitales básicos (peso, talla) - Aptitud, observaciones,
     * recomendaciones - CIE10 principal, médico, fechas Aquí SÍ se persiste la
     * FICHA_OCUPACIONAL (y SIGNOS_VITALES).
     */
    public void guardarStep3() {
        // === Signos vitales (solo peso/talla desde este formulario) ===
        if (peso != null || talla != null) {
            signos.setPesoKg(peso);
            // En BD TALLA_M está en metros; en el form manejas cm.
            if (talla != null) {
                signos.setTallaM(talla / 100.0);
            }
            signos.setFechaCreacion(new Date());
            signos.setUsrCreacion("USR_APP"); // reemplaza por el usuario logueado

            signos = signosService.guardar(signos);
            ficha.setSignos(signos);
        }

        // Sincronizar tipoEvaluacion si no lo hiciste antes
        if (tipoEval != null) {
            ficha.setTipoEvaluacion(tipoEval);
        }

        // Aptitud
        ficha.setAptitudSel(aptitudSel);
        ficha.setDetalleObs(detalleObservaciones);
        ficha.setRecomendaciones(recomendaciones);

        // Fechas
        Date fEmision = (fechaEmision != null) ? fechaEmision : new Date();
        ficha.setFechaEmision(fEmision);

        // Médico
        ficha.setMedicoNombre(medicoNombre);
        ficha.setMedicoCodigo(medicoCodigo);

        // CIE10 principal (desde tu autocomplete de codCie10Ppal)
        if (codCie10Ppal != null && !codCie10Ppal.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPorCodigo(codCie10Ppal.trim());
            if (cie != null) {
                ficha.setCie10Principal(cie);
            } else {
                ficha.setCie10Principal(null);
            }
        } else {
            ficha.setCie10Principal(null);
        }

        // Estado de la ficha: pasa de BORRADOR a EMITIDA en Step 3
        if (ficha.getEstado() == null || "BORRADOR".equals(ficha.getEstado())) {
            ficha.setEstado("EMITIDA");
        }

// Auditoría mínima
        Date ahora = new Date();
        if (ficha.getFechaCreacion() == null) {
            ficha.setFechaCreacion(ahora);
            ficha.setUsrCreacion("USR_APP"); // reemplaza por usuario real
        } else {
            ficha.setFechaActualizacion(ahora);
            ficha.setUsrActualizacion("USR_APP");
        }

// === Persistir en BD ===
        ficha = fichaService.guardar(ficha);
        registrarAuditoria("GUARDAR_STEP3", "FICHA_OCUPACIONAL", "*",
                "Step 3: certificado emitido / actualizado. ID_FICHA=" + ficha.getIdFicha());

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Step 3",
                        "Ficha ocupacional guardada correctamente en la base de datos (estado EMITIDA)."));

    }

    /**
     * Verifica que todo lo necesario esté guardado para poder generar el
     * certificado. No guarda nada, solo valida.
     */
    private boolean verificarFichaCompleta() {
        StringBuilder sb = new StringBuilder();

        if (ficha == null || ficha.getIdFicha() == null) {
            sb.append("- La ficha ocupacional aún no se ha guardado (Steps 1 y 3).\n");
        } else {
            // Puedes incluso recargar desde BD si quieres estar 100% seguro:
            // ficha = fichaService.buscarPorId(ficha.getIdFicha());
        }

        if (ficha != null) {
            if (ficha.getEmpleado() == null) {
                sb.append("- Falta seleccionar el empleado.\n");
            }
            if (ficha.getFechaEvaluacion() == null) {
                sb.append("- Falta la fecha de evaluación.\n");
            }
            if (ficha.getTipoEvaluacion() == null || ficha.getTipoEvaluacion().trim().isEmpty()) {
                sb.append("- Falta el tipo de evaluación (INGRESO/PERÍODICA/etc.).\n");
            }
            if (ficha.getAptitudSel() == null || ficha.getAptitudSel().trim().isEmpty()) {
                sb.append("- Debe seleccionar la aptitud médica.\n");
            }
            if (ficha.getCie10Principal() == null) {
                sb.append("- Debe registrar un diagnóstico CIE10 principal.\n");
            }
            if (ficha.getSignos() == null) {
                sb.append("- Debe registrar signos vitales (peso/talla) en Step 3.\n");
            }
            if (ficha.getFechaEmision() == null) {
                sb.append("- Falta la fecha de emisión del certificado.\n");
            }
        }

        if (sb.length() > 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Validación antes de generar el certificado",
                            sb.toString()));
            return false;
        }

        return true;
    }

    // ===========================================================
    // PDF PREVIEW Y DESCARGA (STEP 4)
    // ===========================================================
    public void prepararVistaPrevia() {
        try {
            // >>> VALIDACIÓN SOLO LECTURA, NO GUARDA NADA <<<
            if (!verificarFichaCompleta()) {
                // No generar PDF si falta algo
                certificadoListo = false;
                return;
            }

            String html = construirHtmlDesdePlantilla();
            byte[] bytes = renderizarPdf(html);

            // 1) Generar token único por ejecución
            this.pdfToken = "CERT_" + System.currentTimeMillis();

            // 2) Guardar bytes en sesión
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .put(pdfToken, bytes);

            // Si usas servlet para previsualizar, desactiva objectUrl
            this.pdfObjectUrl = null;

            certificadoListo = true;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "PDF listo",
                            "Se generó el certificado para vista previa y descarga."));
        } catch (Exception e) {
            certificadoListo = false;

            if (pdfToken != null) {
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getSessionMap()
                        .remove(pdfToken);
            }
            pdfToken = null;
            pdfObjectUrl = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo generar el PDF"));
            e.printStackTrace();
        }
    }

    public void limpiarVistaPrevia() {
        if (pdfToken != null) {
            FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getSessionMap()
                    .remove(pdfToken);
        }
        certificadoListo = false;
        pdfToken = null;
        pdfObjectUrl = null;
    }

    /**
     * Render PDF con baseURL apuntando a /resources/ (para CSS/IMG) En la
     * PLANTILLA.html referencia imágenes externas como:
     * <img src="images/LOGO_IGM_FULL_COLOR.png" />
     * (Los logos principales se inyectan por Data URI).
     */
    private byte[] renderizarPdf(String xhtml) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        // baseURL = raíz del WAR (no /resources/)
        String baseURL = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResource("/")
                .toExternalForm(); // p.ej. file:/.../webapp/

        // Fuente Unicode (opcional)
        try {
            String fontsBase = FacesContext.getCurrentInstance()
                    .getExternalContext().getRealPath("/resources/fonts/");
            if (fontsBase != null) {
                renderer.getFontResolver().addFont(
                        fontsBase + File.separator + "DejaVuSans.ttf",
                        BaseFont.IDENTITY_H, true
                );
            }
        } catch (Throwable ignore) {
        }

        renderer.setDocumentFromString(xhtml, baseURL);
        renderer.layout();
        renderer.createPDF(baos);
        renderer.finishPDF();
        return baos.toByteArray();
    }

    /**
     * Lee /resources/pdf/PLANTILLA.html y reemplaza {{placeholders}} Inyecta
     * además los logos MIDENA/IGM como Data URI.
     */
    private String construirHtmlDesdePlantilla() throws Exception {
        // 1) Cargar plantilla y normalizar a XHTML
        String template = cargarRecursoComoString("PLANTILLA.html");
        template = normalizarXhtml(template);

        // 2) Fechas
        Date f = (fechaEmision != null) ? fechaEmision : new Date();
        SimpleDateFormat yy = new SimpleDateFormat("yyyy");
        SimpleDateFormat MM = new SimpleDateFormat("MM");
        SimpleDateFormat dd = new SimpleDateFormat("dd");

        // 3) Checks de aptitud (según aptitudSel)
        String aApto = "&nbsp;", aObs = "&nbsp;", aLim = "&nbsp;", aNo = "&nbsp;";
        if (aptitudSel != null) {
            switch (aptitudSel) {
                case "APTO":
                    aApto = "X";
                    break;
                case "APTO_EN_OBS":
                    aObs = "X";
                    break;
                case "APTO_LIMIT":
                    aLim = "X";
                    break;
                case "NO_APTO":
                    aNo = "X";
                    break;
            }
        }

        // 4) Sincronizar tipoEval -> tipoEvaluacion si está vacío
        if (tipoEval != null && (tipoEvaluacion == null || tipoEvaluacion.isEmpty())) {
            tipoEvaluacion = tipoEval;
        }

        // 5) Checks del tipo de evaluación
        String chkIngreso = "&nbsp;", chkPeriodico = "&nbsp;", chkReintegro = "&nbsp;", chkRetiro = "&nbsp;";
        if (tipoEvaluacion != null) {
            switch (tipoEvaluacion.toUpperCase()) {
                case "INGRESO":
                    chkIngreso = "X";
                    break;
                case "PERIODICO":
                case "PERIÓDICO":
                    chkPeriodico = "X";
                    break;
                case "REINTEGRO":
                    chkReintegro = "X";
                    break;
                case "RETIRO":
                    chkRetiro = "X";
                    break;
            }
        }

        // 6) Resolver URLs absolutas de logos (más robusto que data:)
        String logoIgmUrl = "";
        String logoMidenaUrl = "";
        try {
            logoIgmUrl = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getResource("/resources/images/LOGO_IGM_FULL_COLOR.png")
                    .toExternalForm();
        } catch (Exception ex) {
            System.err.println("[PDF] No se pudo resolver LOGO_IGM_FULL_COLOR.png: " + ex.getMessage());
        }
        try {
            logoMidenaUrl = FacesContext.getCurrentInstance()
                    .getExternalContext()
                    .getResource("/resources/images/logomidena.PNG")
                    .toExternalForm();
        } catch (Exception ex) {
            System.err.println("[PDF] No se pudo resolver LOGO_MIDENA_FULL_COLO.png: " + ex.getMessage());
        }

        // 7) Mapa de reemplazos {{clave}} -> valor
        Map<String, String> rep = new LinkedHashMap<>();

        // Logos (tu plantilla usa estos placeholders)
        rep.put("LOGO_IGM_DATAURI", logoIgmUrl);
        rep.put("LOGO_MIDENA_DATAURI", logoMidenaUrl);

        // A. Identificación
        rep.put("institucion", safe(institucion));
        rep.put("ruc", safe(ruc));
        rep.put("num_formulario", safe(noHistoria));
        rep.put("num_archivo", safe(noArchivo));
        rep.put("centroTrabajo", safe(centroTrabajo));
        rep.put("ciiu", safe(ciiu));

        // Paciente
        rep.put("apellido1", safe(apellido1));
        rep.put("apellido2", safe(apellido2));
        rep.put("nombre1", safe(nombre1));
        rep.put("nombre2", safe(nombre2));
        rep.put("sexo", safe(sexo));

        // Fecha y evaluación
        rep.put("fecha_yyyy", yy.format(f));
        rep.put("fecha_MM", MM.format(f));
        rep.put("fecha_dd", dd.format(f));
        rep.put("chk_ingreso", chkIngreso);
        rep.put("chk_periodico", chkPeriodico);
        rep.put("chk_reintegro", chkReintegro);
        rep.put("chk_retiro", chkRetiro);

        // Aptitud
        rep.put("chk_apto", aApto);
        rep.put("chk_obs", aObs);
        rep.put("chk_lim", aLim);
        rep.put("chk_noapto", aNo);

        // Textos libres
        rep.put("detalleObservaciones", safe(detalleObservaciones));
        rep.put("recomendaciones", safe(recomendaciones));
        rep.put("medicoNombre", safe(medicoNombre));
        rep.put("medicoCodigo", safe(medicoCodigo));

        // 8) Aplicar reemplazos
        for (Map.Entry<String, String> e : rep.entrySet()) {
            String key = e.getKey();
            String val = (e.getValue() == null) ? "" : e.getValue();
            template = template.replace("{{" + key + "}}", val);
        }

        return template;
    }

    /**
     * Lee un recurso del WAR a String: /resources/pdf/{pathRelativo}
     */
    private String cargarRecursoComoString(String pathRelativo) throws IOException {
        InputStream in = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResourceAsStream("/resources/pdf/" + pathRelativo);
        if (in == null) {
            throw new IllegalArgumentException("No se encontró la plantilla: /resources/pdf/" + pathRelativo);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * Normaliza a XHTML por si el archivo trae meta/br sin cierre.
     */
    private static String normalizarXhtml(String s) {
        if (s == null) {
            return "";
        }
        s = s.replaceAll("(?i)<meta\\s+charset\\s*=\\s*\"?utf-8\"?\\s*>",
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        s = s.replaceAll("(?i)<br(\\s*)>", "<br />");
        s = s.replaceAll("(?i)<hr(\\s*)>", "<hr />");
        s = s.replaceAll("(?i)<img([^>]*?)(?<!/)>", "<img$1 />");
        return s;
    }

    /**
     * Escape básico para evitar romper XHTML al inyectar texto.
     */
    private String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public void syncTipoEvaluacion() {
        this.tipoEvaluacion = this.tipoEval;
    }

    // --- Utilitario: convierte /resources/... a Data URI (Base64) ---
    private String dataUriFromResource(String pathFromResources) throws IOException {
        InputStream in = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResourceAsStream("/resources/" + pathFromResources);
        if (in == null) {
            System.err.println("[PDF] No se encontró recurso: /resources/" + pathFromResources);
            return ""; // no romper el render
        }
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            bytes = bos.toByteArray();
        }
        String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
        String mime = "image/png";
        String lower = pathFromResources.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mime = "image/jpeg";
        } else if (lower.endsWith(".gif")) {
            mime = "image/gif";
        }
        return "data:" + mime + ";base64," + base64;
    }

    // ===========================================================
    // AUTOCOMPLETE CIE10 PRINCIPAL
    // ===========================================================
    public List<Cie10> completarCie10(String query) {
        return cie10Service.buscarPorCodigoODescripcion(query);
    }
    // Autocomplete para el campo de CÓDIGO

// ======================
// AUTOCOMPLETE CIE10
// ======================
    public List<String> completarCie10PorCodigo(String query) {
        List<String> codigos = new ArrayList<>();

        if (query == null) {
            return codigos;
        }

        String q = query.trim().toUpperCase();
        if (q.isEmpty()) {
            return codigos;
        }

        // Puedes seguir usando el mismo servicio general...
        List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);

        for (Cie10 c : lista) {
            if (c != null && c.getCodigo() != null) {
                String cod = c.getCodigo().toUpperCase();

                // FILTRO SOLO POR CÓDIGO
                // si quieres "contenga":
                // if (cod.contains(q)) {
                // si quieres "empieza por":
                if (cod.startsWith(q)) {
                    codigos.add(c.getCodigo());
                }
            }
        }

        return codigos;
    }

    public List<String> completarCie10PorDescripcion(String query) {
        List<String> descripciones = new ArrayList<String>();

        if (query == null || query.trim().isEmpty()) {
            return descripciones;
        }

        // Igual: busca tanto por código como por descripción
        List<Cie10> lista = cie10Service.buscarPorCodigoODescripcion(query);

        for (Cie10 c : lista) {
            if (c != null && c.getDescripcion() != null) {
                descripciones.add(c.getDescripcion());
            }
        }

        return descripciones;
    }

    // Cuando el usuario selecciona un CÓDIGO desde el autocomplete
    public void onCie10CodigoSelect(SelectEvent event) {
        String codigo = (String) event.getObject();
        this.codCie10Ppal = codigo;

        if (codigo != null && !codigo.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
            if (cie != null) {
                this.descCie10Ppal = cie.getDescripcion();
            } else {
                this.descCie10Ppal = null;
            }
        } else {
            this.descCie10Ppal = null;
        }
    }

    // Cuando escribe el código y solo da ENTER/TAB (sale del campo)
    public void onCie10CodigoBlur() {
        if (this.codCie10Ppal != null && !this.codCie10Ppal.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPorCodigo(this.codCie10Ppal.trim());
            if (cie != null) {
                this.descCie10Ppal = cie.getDescripcion();
            } else {
                this.descCie10Ppal = null;
            }
        } else {
            this.descCie10Ppal = null;
        }
    }

    // Cuando el usuario selecciona una DESCRIPCIÓN desde el autocomplete
    public void onCie10DescripcionSelect(SelectEvent event) {
        String descripcion = (String) event.getObject();
        this.descCie10Ppal = descripcion;

        if (descripcion != null && !descripcion.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
            if (cie != null) {
                this.codCie10Ppal = cie.getCodigo();
            } else {
                this.codCie10Ppal = null;
            }
        } else {
            this.codCie10Ppal = null;
        }
    }

    // Cuando escribe la descripción y solo da ENTER/TAB
    public void onCie10DescripcionBlur() {
        if (this.descCie10Ppal != null && !this.descCie10Ppal.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(this.descCie10Ppal.trim());
            if (cie != null) {
                this.codCie10Ppal = cie.getCodigo();
            } else {
                this.codCie10Ppal = null;
            }
        } else {
            this.codCie10Ppal = null;
        }
    }

    public void onCie10SelectCodigo(int index) {
        if (listaDiag == null || index < 0 || index >= listaDiag.size()) {
            return;
        }

        ConsultaDiagnostico diag = listaDiag.get(index);
        String codigo = diag.getCodigo();

        if (codigo == null || codigo.trim().isEmpty()) {
            diag.setDescripcion(null);
            return;
        }

        Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
        if (cie != null) {
            diag.setDescripcion(cie.getDescripcion());
        } else {
            diag.setDescripcion(null);
        }
    }

    public void onCie10SelectDescripcion(int index) {
        if (listaDiag == null || index < 0 || index >= listaDiag.size()) {
            return;
        }

        ConsultaDiagnostico diag = listaDiag.get(index);
        String desc = diag.getDescripcion();

        if (desc == null || desc.trim().isEmpty()) {
            diag.setCodigo(null);
            return;
        }

        Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(desc.trim());
        if (cie != null) {
            diag.setCodigo(cie.getCodigo());
        } else {
            diag.setCodigo(null);
        }
    }

// TAB / ENTER en campo DESCRIPCIÓN
    public void onCie10BlurDescripcion(int index) {
        onCie10SelectDescripcion(index);
    }

// -----------------------------------------------------------
// ABRIR DIÁLOGO DE INGRESO MANUAL (botón "INGRESAR MANUALMENTE")
// -----------------------------------------------------------
    public void abrirPersonaAuxManual() {
        // Siempre aseguramos que exista el objeto
        if (personaAux == null) {
            personaAux = new PersonaAux();
        }

        // Si ya escribió una cédula en el popup, la reutilizamos
        if (!esVacio(cedulaBusqueda)
                && (personaAux.getCedula() == null || personaAux.getCedula().isEmpty())) {
            personaAux.setCedula(cedulaBusqueda.trim());
        }

//    // Limpiamos los campos auxiliares del diálogo
//    auxApellido1 = null;
//    auxApellido2 = null;
//    auxNombre1  = null;
//    auxNombre2  = null;
        // sexo y fechaNacimiento los puedes dejar como estén o limpiar:
        // sexo = null;
        // fechaNacimiento = null;
        mostrarDialogoAux = true;
        PrimeFaces.current().executeScript("PF('dlgPersonaAux').show();");
    }

// -----------------------------------------------------------
// GUARDAR PERSONA AUXILIAR Y USARLA EN LA FICHA
// (botón "GUARDAR Y USAR EN FICHA")
// -----------------------------------------------------------
    public void guardarPersonaAuxYUsar() {

        System.out.println("INGRESA AL METODO DE GUARDAR ");
        FacesContext ctx = FacesContext.getCurrentInstance();

        // 0) Asegurar que exista el objeto
        if (personaAux == null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No existe información de la persona para guardar."
            ));
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return;
        }

        System.out.println("PERSONA AUXILIAR ANTES VALIDAR: " + personaAux);

        // 1) Validar campos obligatorios sobre EL MISMO personaAux
        if (esVacio(personaAux.getCedula())
                || esVacio(personaAux.getApellido1())
                || esVacio(personaAux.getNombre1())
                || esVacio(personaAux.getSexo())
                || personaAux.getFechaNac() == null) {

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Datos incompletos",
                    "Cédula, primer apellido, primer nombre, sexo y fecha de nacimiento son obligatorios."
            ));
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
            return;
        }

        try {
            // 2) Normalizar un poco
            personaAux.setCedula(personaAux.getCedula().trim());

            if (!esVacio(personaAux.getApellido1())) {
                personaAux.setApellido1(personaAux.getApellido1().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getApellido2())) {
                personaAux.setApellido2(personaAux.getApellido2().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getNombre1())) {
                personaAux.setNombre1(personaAux.getNombre1().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getNombre2())) {
                personaAux.setNombre2(personaAux.getNombre2().trim().toUpperCase());
            }
            if (!esVacio(personaAux.getSexo())) {
                personaAux.setSexo(personaAux.getSexo().trim().toUpperCase());
            }

            // 3) Auditoría básica
            Date ahora = new Date();
            if (personaAux.getIdPersonaAux() == null) {
                personaAux.setEstado("A");
                personaAux.setFechaCreacion(ahora);
                personaAux.setUsrCreacion("SISTEMA");
            } else {
                personaAux.setFechaActualizacion(ahora);
                personaAux.setUsrActualizacion("SISTEMA");
            }

            // 4) Persistir en CONSULTORIO.PERSONA_AUX
            personaAux = personaAuxService.guardar(personaAux);

            // 5) Pasar datos a la ficha principal (step 1)
            this.cedulaBusqueda = personaAux.getCedula();
            this.apellido1 = personaAux.getApellido1();
            this.apellido2 = personaAux.getApellido2();
            this.nombre1 = personaAux.getNombre1();
            this.nombre2 = personaAux.getNombre2();
            this.sexo = personaAux.getSexo();
            this.fechaNacimiento = personaAux.getFechaNac();
            this.noHistoria = personaAux.getCedula();
            // 6) Cerrar diálogos y deshabilitar ingreso manual
            mostrarDialogoAux = false;
            mostrarDlgCedula = false;
            permitirIngresoManual = false;
            PrimeFaces.current().ajax().update("layoutForm:noHistoria");
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", false);
            PrimeFaces.current().executeScript(
                    "PF('dlgPersonaAux').hide(); PF('dlgBuscarCedula').hide();"
            );

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Datos guardados",
                    "Se guardó la persona auxiliar y se cargaron los datos en la ficha."
            ));

            log.log(Level.INFO,
                    "PersonaAux guardada manualmente: {0} {1} / {2} {3} (cedula={4})",
                    new Object[]{
                        personaAux.getApellido1(),
                        personaAux.getApellido2(),
                        personaAux.getNombre1(),
                        personaAux.getNombre2(),
                        personaAux.getCedula()
                    });

        } catch (Exception e) {
            log.log(Level.SEVERE, "Error guardando datos manuales", e);
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Ocurrió un error al procesar y guardar los datos."
            ));
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
        }
    }

    /**
     * REALIZA LA BUSQUEDA POR CEDULA
     */
    /**
     * REALIZA LA BUSQUEDA POR CEDULA
     */
    public void buscarCedula() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // Siempre reseteamos el flag antes de empezar
        permitirIngresoManual = false;

        // Validación básica de entrada
        if (cedulaBusqueda == null || cedulaBusqueda.trim().isEmpty()) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Búsqueda",
                    "Ingrese una cédula para realizar la búsqueda."
            ));
            return;
        }

        String cedula = cedulaBusqueda.trim();

        try {
            DatEmpleado emp = empleadoService.buscarPorCedula(cedula);

            if (emp != null) {
                // ==============================
                // 1) EMPLEADO ENCONTRADO
                // ==============================
                this.empleadoSel = emp;
                this.noPersonaSel = emp.getNoPersona();
                this.apellido1 = emp.getPriApellido();
                this.apellido2 = emp.getSegApellido();
                this.nombre1 = emp.getNombres(); // si viene concatenado, luego separas

                this.sexo = emp.getSexo() != null ? emp.getSexo().getCodigo() : null;

                Date fn = emp.getFNacimiento();
                if (fn != null) {
                    this.fechaNacimiento = fn;
                }
                this.noHistoria = emp.getNoCedula();
                // Cerramos el diálogo y deshabilitamos ingreso manual
                mostrarDlgCedula = false;
                permitirIngresoManual = false;

                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Búsqueda",
                        "Se cargó la información del empleado desde RRHH."
                ));
            } else {
                this.empleadoSel = null;
                this.noPersonaSel = null;
                // ==============================
                // 2) NO SE ENCONTRÓ → INGRESO MANUAL
                // ==============================
                if (personaAux == null) {
                    personaAux = new PersonaAux();
                }

                // Prellenamos la cédula y limpiamos los demás campos
                personaAux.setCedula(cedula);
                personaAux.setApellido1(null);
                personaAux.setApellido2(null);
                personaAux.setNombre1(null);
                personaAux.setNombre2(null);
                personaAux.setSexo(null);
                personaAux.setFechaNac(null);
                personaAux.setNoPersona(null);

                // Auditoría/estado mínimo
                personaAux.setFechaActualizacion(new Date());
                personaAux.setUsrActualizacion("SISTEMA"); // o el usuario logueado

                this.noHistoria = cedula;
                // Dejamos abierto el diálogo de cédula y habilitamos el botón
                mostrarDlgCedula = true;
                permitirIngresoManual = true;

                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Búsqueda",
                        "No se encontró la cédula en RRHH. Puede ingresarla manualmente."
                ));
            }
            // 🔄 Actualizar visualmente el campo de historia clínica
            PrimeFaces.current().ajax().update("layoutForm:noHistoria");

        } catch (Exception e) {
            permitirIngresoManual = false;
            mostrarDlgCedula = true;

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Ocurrió un error al buscar la cédula. Intente nuevamente."
            ));
            e.printStackTrace();
        }
    }

    public boolean isMostrarDlgCedula() {
        return mostrarDlgCedula;
    }

    public void setMostrarDlgCedula(boolean mostrarDlgCedula) {
        this.mostrarDlgCedula = mostrarDlgCedula;
    }

// ayudita
    private String esNulo(String s) {
        return s == null ? "" : s;
    }

    /**
     * Separa un texto en dos partes: - [0] = primera palabra - [1] = resto (o
     * null si no hay resto) Ej: "Guerra Kleber" -> ["Guerra", "Kleber"] "De la
     * Cruz López" -> ["De", "la Cruz López"]
     */
    private String[] splitEnDos(String valor) {
        String res1 = null;
        String res2 = null;

        if (!isBlank(valor)) {
            String trimmed = valor.trim();
            String[] partes = trimmed.split("\\s+");
            if (partes.length == 1) {
                res1 = partes[0];
            } else if (partes.length > 1) {
                res1 = partes[0];
                // el resto tal cual, sin perder lo que el usuario escribió
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < partes.length; i++) {
                    if (i > 1) {
                        sb.append(' ');
                    }
                    sb.append(partes[i]);
                }
                res2 = sb.toString();
            }
        }

        return new String[]{res1, res2};
    }

    /**
     * Habilita el ingreso manual de datos desde el diálogo PersonaAux. Se
     * invoca al presionar el botón "Ingresar manualmente".
     */
    /**
     * Habilita el ingreso manual de datos desde el diálogo PersonaAux. Se
     * invoca al presionar el botón "Ingresar manualmente".
     */
    public void prepararIngresoManual() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (esVacio(cedulaBusqueda)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Cédula requerida",
                    "Ingrese la cédula antes de continuar."
            ));
            return;
        }

        // Aseguramos que exista
        if (personaAux == null) {
            personaAux = new PersonaAux();
        }

        personaAux.setCedula(cedulaBusqueda.trim());

        // limpiar resto de campos
        personaAux.setApellido1(null);
        personaAux.setApellido2(null);
        personaAux.setNombre1(null);
        personaAux.setNombre2(null);
        personaAux.setSexo(null);
        personaAux.setFechaNac(null);

        permitirIngresoManual = true;
    }

// ===============================
//   MÉTODOS UTILITARIOS
// ===============================
    private String primerToken(String texto) {
        if (esVacio(texto)) {
            return null;
        }
        String[] partes = texto.trim().split("\\s+");
        return partes[0];
    }

    private String restoTokens(String texto) {
        if (esVacio(texto)) {
            return null;
        }
        String[] partes = texto.trim().split("\\s+");
        if (partes.length <= 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < partes.length; i++) {
            if (i > 1) {
                sb.append(' ');
            }
            sb.append(partes[i]);
        }
        return sb.toString();
    }

    /**
     * GETTER DE PERSONA AUX
     */
    public PersonaAux getPersonaAux() {
        if (personaAux == null) {
            personaAux = new PersonaAux();
        }
        return personaAux;
    }

}
