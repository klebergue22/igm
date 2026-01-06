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
import ec.gob.prueba.prueba_maven.modelo.FichaActLaboral;
import ec.gob.prueba.prueba_maven.servicio.Cie10Service;
import ec.gob.prueba.prueba_maven.servicio.EmpleadoService;
import ec.gob.prueba.prueba_maven.servicio.FichaOcupacionalService;
import ec.gob.prueba.prueba_maven.servicio.FichaRiesgoService;
import ec.gob.prueba.prueba_maven.servicio.PersonaAuxService;
import ec.gob.prueba.prueba_maven.servicio.SignosVitalesService;
import ec.gob.prueba.prueba_maven.servicio.AuditoriaConsultorioService;
import ec.gob.prueba.prueba_maven.servicio.FichaActLaboralService;
import ec.gob.prueba.prueba_maven.servicio.FichaDiagnosticoService;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import javax.faces.component.UIComponent;
import org.primefaces.event.SelectEvent;

@Slf4j
@ManagedBean(name = "centroMedicoCtrl")
@ViewScoped
@Getter
@Setter
@ToString
public class CentroMedicoCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    // -----------------------------
// CONSTANTES (filas fijas)
// -----------------------------
    private static final int H_ROWS = 8;         // Step H (8 filas)
    private static final int CONSUMO_ROWS = 3;   // Tabaco/Alcohol/Otras
    private static final int DIAG_ROWS = 6;      // Diagnósticos

// -----------------------------
// WIZARD / NAVEGACIÓN
// -----------------------------
    private String currentStep = "step1";
    private boolean mostrarDlgCedula = true;
    private boolean mostrarDialogoAux;
    private boolean permitirIngresoManual;

// -----------------------------
// BÚSQUEDA / PACIENTE
// -----------------------------
    private String cedulaBusqueda;
    private Integer noPersonaSel;
    private DatEmpleado empleadoSel;
    private PersonaAux personaAux;

// -----------------------------
// A. DATOS ESTABLECIMIENTO / IDENTIFICACIÓN
// -----------------------------
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

    private String sexo;
    private Date fechaNacimiento;
    private Integer edad;

    private Date fechaAtencion;
    private String tipoEval;          // el que usas en UI y BD
    private String tipoEvaluacion;    // el que usas en PDF (y lo sincronizas)

    private Date fecIngreso;
    private Date fecReintegro;
    private Date fecRetiro;

    private String grupoSanguineo;
    private String lateralidad;

// -----------------------------
// ATENCIÓN PRIORITARIA
// -----------------------------
    private boolean apEmbarazada;
    private boolean apDiscapacidad;
    private boolean apCatastrofica;
    private boolean apLactancia;
    private boolean apAdultoMayor;

// -----------------------------
// C. ANTECEDENTES / CONDICIÓN
// -----------------------------
    private String antClinicoQuirurgico;
    private String antFamiliares;
    private String condicionEspecial;

    private String autorizaTransfusion;
    private String tratamientoHormonal;
    private String tratamientoHormonalCual;

// --- Solo hombres ---
    private String examenReproMasculino;
    private Integer tiempoReproMasculino;

// --- Solo mujeres ---
    private Date fum;
    private Integer gestas;
    private Integer partos;
    private Integer cesareas;
    private Integer abortos;
    private String planificacion;
    private String planificacionCual;

// -----------------------------
// SIGNOS VITALES (inputs Step1)
// -----------------------------
    private Double peso;        // kg

// ✅ IMPORTANTE: para que compile recalcularIMC() y el mapeo que ya tienes
    private Double talla;       // cm (alias que usas en recalcularIMC si no cambias el método)

// tu campo actual
    private Double tallaCm;     // cm

    private Double imc;         // calculado
    private Double temp;        // °C
    private String paStr;       // "120/80"
    private Integer fc;
    private Integer fr;
    private Integer satO2;
    private Double perimetroAbd;

// -----------------------------
// STEP 1 - CONSUMO / AF / MED (arrays)
// -----------------------------
    private Integer[] consTiempoConsumoMeses;
    private Boolean[] consExConsumidor;
    private Integer[] consTiempoAbstinenciaMeses;
    private Boolean[] consNoConsume;
    private String consOtrasCual;

    private String[] afCual;
    private String[] afTiempo;

    private String[] medCual;
    private Integer[] medCant;

    private String consumoVidaCondObs;
    private String obsJ;

// -----------------------------
// STEP 2 - RIESGOS
// -----------------------------
    private FichaRiesgo fichaRiesgo;
    private List<String> actividadesLab = new ArrayList<>();
    private Map<String, Boolean> riesgos = new LinkedHashMap<>();
    private Map<String, String> otrosRiesgos = new LinkedHashMap<>();
    private List<String> medidasPreventivas = new ArrayList<>();

// -----------------------------
// STEP 3 - CERTIFICADO / APTITUD / MÉDICO
// -----------------------------
    private Date fechaEmision;
    private String aptitudSel;
    private String detalleObservaciones;
    private String recomendaciones;
    private String medicoNombre;
    private String medicoCodigo;
    // N. RETIRO
    private String nRealizaEvaluacion; // 'S' / 'N'
    private String nRelacionTrabajo;   // 'S' / 'N'
    private String nObsRetiro;         // texto

// CIE10 principal (para PDF/UI)
    private String codCie10Ppal;
    private String descCie10Ppal;

// Diagnósticos (filas)
    private List<ConsultaDiagnostico> listaDiag = new ArrayList<>();

// -----------------------------
// STEP H (8 filas)
// -----------------------------
    private String[] hCentroTrabajo;
    private String[] hActividad;
    private Boolean[] hIncidente;
    private Boolean[] hAccidente;
    private Integer[] hTiempo;
    private Boolean[] hEnfOcupacional;
    private Boolean[] hEnfComun;
    private Boolean[] hEnfProfesional;
    private Boolean[] hOtros;
    private String[] hOtrosCual;
    private Date[] hFecha;
    private String[] hEspecificacion;
    private String[] hObservacion;

// -----------------------------
// FECHAS ADICIONALES
// -----------------------------
    private List<Date> iessFecha;
    private List<Date> fechaAct;

    private List<Date> examFecha = new ArrayList<>();

    // I. Actividades extralaborales
    private List<String> tipoAct;

    private List<String> descAct;

// -----------------------------
// OBJETOS DOMINIO PARA BD
// -----------------------------
    private FichaOcupacional ficha;
    private SignosVitales signos;
    private ConsultaMedica consulta;

// -----------------------------
// PDF (Step 4)
// -----------------------------
    private boolean certificadoListo;
    private String pdfObjectUrl;
    private String pdfToken;
    private List<String> actLabRows;               // solo para iterar 1..N
    private List<String> actLabCentroTrabajo;      // centro de trabajo
    private List<String> actLabActividad;          // actividad laboral
    private List<String> actLabIncidente;          // descripción incidente/accidente/enfermedad
    private List<Date> actLabFecha;              // fecha

    private List<String> actLabTiempo;

    // CHECKS (deben ser booleanos)
    private List<Boolean> actLabTrabajoAnterior;
    private List<Boolean> actLabTrabajoActual;
    private List<Boolean> actLabIncidenteChk;
    private List<Boolean> actLabAccidenteChk;
    private List<Boolean> actLabEnfermedadChk;

// === STEP H (IESS / Observaciones) ===
    private List<Boolean> iessSi;
    private List<Boolean> iessNo;
    private List<String> iessEspecificar;
    private List<String> actLabObservaciones;

    // J. Exámenes
    private List<String> examNombre;
    private List<String> examResultado;

    private int stepIndex = 1;

// -----------------------------
// SERVICES (EJB)
// -----------------------------
    @EJB
    private Cie10Service cie10Service;
    @EJB
    private FichaOcupacionalService fichaService;
    @EJB
    private SignosVitalesService signosService;
    @EJB
    private FichaRiesgoService fichaRiesgoService;
    @EJB
    private FichaDiagnosticoService fichaDiagnosticoService;
    @EJB
    private EmpleadoService empleadoService;
    @EJB
    private PersonaAuxService personaAuxService;
    @EJB
    private AuditoriaConsultorioService auditoriaService;
    @EJB
    private FichaActLaboralService fichaActLaboralService;

    private void initExamenes(int n) {
        examNombre = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        examFecha = new ArrayList<>(java.util.Collections.nCopies(n, null));
        examResultado = new ArrayList<>(java.util.Collections.nCopies(n, ""));
    }

    private void initActLab(int n) {
        actLabRows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            actLabRows.add(String.valueOf(i + 1));
        }

        actLabCentroTrabajo = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        actLabActividad = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        actLabIncidente = new ArrayList<>(java.util.Collections.nCopies(n, "")); // si lo sigues usando como texto
        actLabFecha = new ArrayList<>(java.util.Collections.nCopies(n, null));
        actLabTiempo = new ArrayList<>(java.util.Collections.nCopies(n, ""));

        // ✅ checks Trabajo / Incidente / Accidente / Enfermedad
        actLabTrabajoAnterior = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabTrabajoActual = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabIncidenteChk = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabAccidenteChk = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        actLabEnfermedadChk = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));

        // ✅ Observaciones (columna final)
        actLabObservaciones = new ArrayList<>(java.util.Collections.nCopies(n, ""));

        // ✅ IESS: SI / NO / FECHA / ESPECIFICAR
        iessSi = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        iessNo = new ArrayList<>(java.util.Collections.nCopies(n, Boolean.FALSE));
        iessFecha = new ArrayList<>(java.util.Collections.nCopies(n, null));
        iessEspecificar = new ArrayList<>(java.util.Collections.nCopies(n, ""));
    }

    private void initActividadesExtra(int n) {
        fechaAct = new ArrayList<>(java.util.Collections.nCopies(n, null));
        tipoAct = new ArrayList<>(java.util.Collections.nCopies(n, ""));
        descAct = new ArrayList<>(java.util.Collections.nCopies(n, ""));
    }

    @PostConstruct
    public void init() {
        mostrarDlgCedula = true;
        fechaAtencion = new Date();
        initActLab(3);
        initActividadesExtra(3);
        initExamenes(5);
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
        //SETEO VALORES COMUNES 
        ficha.setRucEstablecimiento(ruc);
        ficha.setNoHistoriaClinica(null);
        ficha.setInstSistema(institucion);
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
        // Step H – IESS (3 filas)

        // Si tienes otros calendarios similares:
        examFecha = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            examFecha.add(null);
        }
        hCentroTrabajo = new String[H_ROWS];
        hActividad = new String[H_ROWS];
        hIncidente = new Boolean[H_ROWS];
        hAccidente = new Boolean[H_ROWS];
        hTiempo = new Integer[H_ROWS];
        hEnfOcupacional = new Boolean[H_ROWS];
        hEnfComun = new Boolean[H_ROWS];
        hEnfProfesional = new Boolean[H_ROWS];
        hOtros = new Boolean[H_ROWS];
        hOtrosCual = new String[H_ROWS];
        hFecha = new Date[H_ROWS];
        hEspecificacion = new String[H_ROWS];
        hObservacion = new String[H_ROWS];

        consTiempoConsumoMeses = new Integer[3];
        consExConsumidor = new Boolean[3];
        consTiempoAbstinenciaMeses = new Integer[3];
        consNoConsume = new Boolean[3];

        afCual = new String[3];
        afTiempo = new String[3];

        medCual = new String[3];
        medCant = new Integer[3];

        consOtrasCual = null;
        consumoVidaCondObs = null;
        actLabRows = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
        initActLab(H_ROWS); // o initActLab(8)
        ensureActLabSize();
        initConsumoVidaCond();

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
//
//    private Integer calcularEdad(Date f) {
//        if (f == null) {
//            return null;
//        }
//        Calendar hoy = Calendar.getInstance();
//        Calendar nac = Calendar.getInstance();
//        nac.setTime(f);
//        int years = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);
//        int mh = hoy.get(Calendar.MONTH), mn = nac.get(Calendar.MONTH);
//        if (mh < mn || (mh == mn && hoy.get(Calendar.DAY_OF_MONTH) < nac.get(Calendar.DAY_OF_MONTH))) {
//            years--;
//        }
//        return Math.max(years, 0);
//    }

    /**
     * Calcula edad exacta (años cumplidos) en Java 1.7 usando Calendar. -
     * Valida fecha futura (retorna null) - Normaliza horas para evitar errores
     * por HH:mm:ss
     */
    private Integer calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return null;
        }

        Calendar hoy = Calendar.getInstance();
        Calendar nac = Calendar.getInstance();
        nac.setTime(fechaNacimiento);

        // Normalizar horas a medianoche (evita errores por hora/min/seg)
        limpiarHora(hoy);
        limpiarHora(nac);

        // Fecha futura => inválida
        if (nac.after(hoy)) {
            return null;
        }

        int years = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);

        // Si aún no ha pasado el cumpleaños este año, restar 1
        int mesHoy = hoy.get(Calendar.MONTH);
        int mesNac = nac.get(Calendar.MONTH);

        if (mesHoy < mesNac || (mesHoy == mesNac && hoy.get(Calendar.DAY_OF_MONTH) < nac.get(Calendar.DAY_OF_MONTH))) {
            years--;
        }

        return years;
    }

    private void limpiarHora(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
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
        System.out.println("===============INGRESA A GUARDAR STEP ACTUAL =========");
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            if ("step1".equals(currentStep)) {
                guardarStep1();
                currentStep = "step2";

            } else if ("step2".equals(currentStep)) {
                if (!validarStep2()) {
                    ctx.validationFailed();
                    return;
                }
                guardarStep2();
                currentStep = "step3";

            } else if ("step3".equals(currentStep)) {
                System.out.println("ENTRA A GUARDAR STEP 3");
                // guardarStep3() ya hace: validar + ctx.validationFailed() + return
                guardarStep3();
                if (!ctx.isValidationFailed()) {
                    currentStep = "step4";
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
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

    private List<Date> ensureSize(List<Date> list, int size) {
        if (list == null) {
            list = new ArrayList<>();
        }
        while (list.size() < size) {
            list.add(null);
        }
        return list;
    }

    private void mapConsumoVidaCondToFicha(FichaOcupacional ficha) {
        if (ficha == null) {
            return;
        }

        // Si NO has declarado estos atributos en el controlador,
        // debes declararlos (te pongo la lista al final).
        // Seguridad por si algo viene null (evita NullPointer)
        if (consTiempoConsumoMeses == null || consTiempoConsumoMeses.length < 3) {
            consTiempoConsumoMeses = new Integer[3];
        }

        if (consExConsumidor == null || consExConsumidor.length < 3) {
            consExConsumidor = new Boolean[3];
        }

        if (consTiempoAbstinenciaMeses == null || consTiempoAbstinenciaMeses.length < 3) {
            consTiempoAbstinenciaMeses = new Integer[3];
        }

        if (consNoConsume == null || consNoConsume.length < 3) {
            consNoConsume = new Boolean[3];
        }

        if (afCual == null || afCual.length < 3) {
            afCual = new String[3];
        }
        if (afTiempo == null || afTiempo.length < 3) {
            afTiempo = new String[3];
        }

        if (medCual == null || medCual.length < 3) {
            medCual = new String[3];
        }
        if (medCant == null || medCant.length < 3) {
            medCant = new Integer[3];
        }

        // TABACO = 0
        ficha.setTabConsMeses(consTiempoConsumoMeses[0]);
        ficha.setTabExCons(sn(consExConsumidor[0]));
        ficha.setTabAbsMeses(consTiempoAbstinenciaMeses[0]);
        ficha.setTabNoCons(sn(consNoConsume[0]));

        // ALCOHOL = 1
        ficha.setAlcConsMeses(consTiempoConsumoMeses[1]);
        ficha.setAlcExCons(sn(consExConsumidor[1]));
        ficha.setAlcAbsMeses(consTiempoAbstinenciaMeses[1]);
        ficha.setAlcNoCons(sn(consNoConsume[1]));

        // OTRAS = 2
        ficha.setOtrCual(consOtrasCual);
        ficha.setOtrConsMeses(consTiempoConsumoMeses[2]);
        ficha.setOtrExCons(sn(consExConsumidor[2]));
        ficha.setOtrAbsMeses(consTiempoAbstinenciaMeses[2]);
        ficha.setOtrNoCons(sn(consNoConsume[2]));

        // AF (3 filas)
        ficha.setAfCual1(afCual[0]);
        ficha.setAfTiempo1(afTiempo[0]);
        ficha.setAfCual2(afCual[1]);
        ficha.setAfTiempo2(afTiempo[1]);
        ficha.setAfCual3(afCual[2]);
        ficha.setAfTiempo3(afTiempo[2]);

        // MED (3 filas)
        ficha.setMedCual1(medCual[0]);
        ficha.setMedCant1(medCant[0]);
        ficha.setMedCual2(medCual[1]);
        ficha.setMedCant2(medCant[1]);
        ficha.setMedCual3(medCual[2]);
        ficha.setMedCant3(medCant[2]);

        // OBS
        ficha.setObsConsumoVidaCond(consumoVidaCondObs);
    }

    private String usuarioReal() {
        try {
            // Si luego tienes login en sesión, aquí lo conectas.
            // Por ahora: valor fijo que no rompe.
            return "USR_APP";
        } catch (Exception e) {
            return "USR_APP";
        }
    }

    /**
     * STEP 1: - Atención prioritaria - Antecedentes personales -
     * Gineco-obstétricos (Solo llena la FichaOcupacional en memoria, aún no
     * persiste)
     */
    public void guardarStep1() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            Date ahora = new Date();
            String usuario = usuarioReal(); // ✅ NO hardcode

            // ====== (A) asegurar ficha ======
            if (ficha == null) {
                ficha = new FichaOcupacional();
            }

            // Si vienes solo con noPersonaSel, recupera empleadoSel
            if (empleadoSel == null && noPersonaSel != null) {
                empleadoSel = empleadoService.buscarPorId(noPersonaSel);
            }

            // ==============================
            // 1) VALIDACIONES (solo Step1 / BD)
            // ==============================
            if (fechaAtencion == null) {
                warn("Debe ingresar la fecha de atención.");
                return;
            }
            if (esVacio(tipoEval)) {
                warn("Debe seleccionar el tipo de evaluación.");
                return;
            }

            // paciente: empleado o personaAux
            if (empleadoSel == null) {
                if (personaAux == null || esVacio(personaAux.getCedula())) {
                    warn("Debe seleccionar un empleado de RRHH o registrar una persona auxiliar (cédula obligatoria).");
                    return;
                }
                if (esVacio(personaAux.getApellido1()) || esVacio(personaAux.getNombre1()) || esVacio(personaAux.getSexo())) {
                    warn("En Persona Auxiliar: primer apellido, primer nombre y sexo son obligatorios.");
                    return;
                }
            }

            // signos
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

            if (tallaCm == null && talla != null) {
                tallaCm = talla;
            }
            if (tallaCm == null || tallaCm <= 0) {
                warn("Debe ingresar la talla (cm).");
                return;
            }

            // ==============================
            // 2) ORIGEN DEL PACIENTE (SIN guardar ficha aquí)
            // ==============================
            String cedulaPaciente;

            if (empleadoSel != null) {
                ficha.setEmpleado(empleadoSel);
                ficha.setPersonaAux(null);
                cedulaPaciente = empleadoSel.getNoCedula();
            } else {
                // ✅ guardar personaAux si aún no tiene ID
                if (personaAux.getIdPersonaAux() == null) {
                    personaAux.setFechaCreacion(ahora);
                    personaAux.setUsrCreacion(usuario);
                    personaAux = personaAuxService.guardar(personaAux);
                }
                ficha.setPersonaAux(personaAux);
                ficha.setEmpleado(null);
                cedulaPaciente = personaAux.getCedula();
            }

            // ✅ Guardar en BD, no en variable suelta
            ficha.setNoHistoriaClinica(cedulaPaciente);

            // ==============================
            // 3) MAPEO A FICHA_OCUPACIONAL (Step1)
            // ==============================
            ficha.setFechaEvaluacion(fechaAtencion);
            ficha.setTipoEvaluacion(tipoEval);

            ficha.setApEmbarazada(sn(apEmbarazada));
            ficha.setApDiscapacidad(sn(apDiscapacidad));
            ficha.setApCatastrofica(sn(apCatastrofica));
            ficha.setApLactancia(sn(apLactancia));
            ficha.setApAdultoMayor(sn(apAdultoMayor));

            ficha.setAntClinicoQuir(antClinicoQuirurgico);
            ficha.setAntFamiliares(antFamiliares);
            ficha.setCondicionEspecial(condicionEspecial);

            ficha.setAutorizaTransfusion(autorizaTransfusion);
            ficha.setTratHormonal(tratamientoHormonal);
            ficha.setTratHormonalCual(tratamientoHormonalCual);

            ficha.setExamReproMasc(examenReproMasculino);
            ficha.setTiempoReproMasc(tiempoReproMasculino);

            ficha.setFum(fum);
            ficha.setGestas(gestas);
            ficha.setPartos(partos);
            ficha.setCesareas(cesareas);
            ficha.setAbortos(abortos);
            ficha.setPlanificacion(planificacion);
            ficha.setPlanificacionCual(planificacionCual);

            // consumo vida cond
            mapConsumoVidaCondToFicha(ficha);

            // ==============================
            // 4) ARMAR / GUARDAR SIGNOS_VITALES
            // ==============================
            Integer paSis, paDias;
            try {
                String[] parts = paStr.split("/");
                if (parts.length != 2) {
                    throw new IllegalArgumentException();
                }
                paSis = Integer.valueOf(parts[0].trim());
                paDias = Integer.valueOf(parts[1].trim());
            } catch (Exception ex) {
                warn("El formato de PA debe ser 120/80 (números enteros separados por '/').");
                return;
            }

            SignosVitales sv = (ficha.getSignos() != null) ? ficha.getSignos() : this.signos;
            if (sv == null) {
                sv = new SignosVitales();
            }

            sv.setTemperaturaC(temp);
            sv.setPaSistolica(paSis);
            sv.setPaDiastolica(paDias);
            sv.setFrecuenciaCard(fc);
            sv.setFrecuenciaResp(fr);
            sv.setSatO2(satO2);
            sv.setPesoKg(peso);

            Double tallaM = tallaCm / 100.0;
            sv.setTallaM(tallaM);
            sv.setPerimetroAbdCm(perimetroAbd);

            if (sv.getIdSignos() == null) {
                sv.setFechaCreacion(ahora);
                sv.setUsrCreacion(usuario);
            } else {
                sv.setFechaActualizacion(ahora);
                sv.setUsrActualizacion(usuario);
            }

            sv = signosService.guardar(sv);
            this.signos = sv;
            ficha.setSignos(sv);

            // ==============================
            // 5) GUARDAR FICHA (BORRADOR) - ÚNICO GUARDADO
            // ==============================
            ficha.setEstado("BORRADOR"); // ✅ Step1 siempre BORRADOR

            // ✅ FECHA_EMISION es NOT NULL en BD (SIEMPRE)
            if (ficha.getFechaEmision() == null) {
                ficha.setFechaEmision(ahora);
            }

            // auditoría de creación/actualización
            if (ficha.getIdFicha() == null) {
                ficha.setFechaCreacion(ahora);
                ficha.setUsrCreacion(usuario);
            } else {
                ficha.setFechaActualizacion(ahora);
                ficha.setUsrActualizacion(usuario);
            }

            ficha = fichaService.guardar(ficha);

            registrarAuditoria("GUARDAR_STEP1", "FICHA_OCUPACIONAL", "*",
                    "Step 1 guardado. ID_FICHA=" + ficha.getIdFicha());
            registrarAuditoria("GUARDAR_STEP1", "SIGNOS_VITALES", "*",
                    "Signos guardados. ID_SIGNOS=" + sv.getIdSignos());

            info("Step 1 guardado correctamente (BORRADOR).");

        } catch (Exception e) {
            log.error("Error en guardarStep1", e);
            error("Ocurrió un error al guardar el Step 1: " + e.getMessage());
            ctx.validationFailed();
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
                if (!isBlank(a)) {
                    hayActividad = true;
                    break;
                }
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
                if (!isBlank(m)) {
                    hayMedida = true;
                    break;
                }
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
                if (med.length() > 0) {
                    med.append(" | ");
                }
                med.append("M").append(i + 1).append(": ").append(m.trim());
            }
        }

        // 3) Armar resumen de checks marcados (riesgos)
        StringBuilder r = new StringBuilder();
        if (riesgos != null) {
            for (Map.Entry<String, Boolean> e : riesgos.entrySet()) {
                if (Boolean.TRUE.equals(e.getValue())) {
                    if (r.length() > 0) {
                        r.append(", ");
                    }
                    r.append(e.getKey());
                }
            }
        }

        // 4) Armar resumen de “otros”
        StringBuilder o = new StringBuilder();
        if (otrosRiesgos != null) {
            for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
                if (!isBlank(e.getValue())) {
                    if (o.length() > 0) {
                        o.append(" | ");
                    }
                    o.append(e.getKey()).append(": ").append(e.getValue().trim());
                }
            }
        }

        // 5) Guardar todo en observaciones (sin perder nada)
        StringBuilder obs = new StringBuilder();
        if (med.length() > 0) {
            obs.append("MEDIDAS: ").append(med);
        }
        if (r.length() > 0) {
            if (obs.length() > 0) {
                obs.append(" || ");
            }
            obs.append("RIESGOS: ").append(r);
        }
        if (o.length() > 0) {
            if (obs.length() > 0) {
                obs.append(" || ");
            }
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
        System.out.println("INGRESA A VALIDAR STEP3");
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
        System.out.println("INGRESA A GUARDAR METODO 3");
        FacesContext ctx = FacesContext.getCurrentInstance();

        try {
            // 0) Debe existir ficha guardada en Step1
            if (ficha == null || ficha.getIdFicha() == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Atención",
                        "Primero debe guardar el Step 1 para generar la ficha."
                ));
                ctx.validationFailed();
                return;
            }

            // 1) Validaciones del Step3
            if (!validarStep3()) {
                ctx.validationFailed();
                return;
            }

            final Date ahora = new Date();
            final String usuario = "USR_APP"; // TODO: usuario real

            // 2) CIE10 principal (si aplica)
            if (!isBlank(codCie10Ppal)) {
                Cie10 cie = cie10Service.buscarPorCodigo(codCie10Ppal.trim());
                if (cie != null) {
                    ficha.setCie10Principal(cie);
                } else {
                    ctx.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_WARN,
                            "Validación",
                            "El código CIE10 principal no existe: " + codCie10Ppal
                    ));
                    ctx.validationFailed();
                    return;
                }
            }

            // 3) Campos Step3 -> ficha
            ficha.setAptitudSel(aptitudSel);
            ficha.setDetalleObs(detalleObservaciones);
            ficha.setRecomendaciones(recomendaciones);
            ficha.setMedicoNombre(medicoNombre);
            ficha.setMedicoCodigo(medicoCodigo);
            ficha.setFechaEmision(fechaEmision != null ? fechaEmision : ahora);

            // Auditoría
            ficha.setFechaActualizacion(ahora);
            ficha.setUsrActualizacion(usuario);

            // 4) Guardar ficha (update)
            ficha = fichaService.guardar(ficha);

            // ============================================================
            // 5) H: ACTIVIDAD LABORAL (FICHA_ACT_LABORAL) - GUARDA/ACTUALIZA
            // ============================================================
            // Asegurar tamaños (evita IndexOutOfBounds)
            ensureActLabSize();

            for (int i = 0; i < H_ROWS; i++) {

                boolean filaTieneDatos
                        = !isBlank(getSafe(actLabCentroTrabajo, i))
                        || !isBlank(getSafe(actLabActividad, i))
                        || !isBlank(getSafe(actLabTiempo, i))
                        || isTrue(getSafe(actLabTrabajoAnterior, i))
                        || isTrue(getSafe(actLabTrabajoActual, i))
                        || isTrue(getSafe(actLabIncidenteChk, i))
                        || isTrue(getSafe(actLabAccidenteChk, i))
                        || isTrue(getSafe(actLabEnfermedadChk, i))
                        || getSafe(iessFecha, i) != null
                        || !isBlank(getSafe(iessEspecificar, i))
                        || !isBlank(getSafe(actLabObservaciones, i));

                int nroFila = i + 1; // BD: NRO_FILA (1..8)

                if (!filaTieneDatos) {
                    // Si existía en BD y ahora está vacía -> eliminar (opcional pero recomendado)
                    fichaActLaboralService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
                    continue;
                }

                // UPSERT por (ID_FICHA, NRO_FILA)
                FichaActLaboral fal = fichaActLaboralService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);

                if (fal == null) {
                    fal = new FichaActLaboral();
                    fal.setFicha(ficha);      // FK: ID_FICHA
                    fal.setNroFila(nroFila);  // NOT NULL
                    fal.setFCreacion(ahora);
                    fal.setUsrCreacion(usuario);
                } else {
                    fal.setFActualizacion(ahora);
                    fal.setUsrActualizacion(usuario);
                }

                fal.setCentroTrabajo(getSafe(actLabCentroTrabajo, i));
                fal.setActividad(getSafe(actLabActividad, i));
                fal.setTiempo(getSafe(actLabTiempo, i));

                // CHAR(1) 'S'/'N'
                fal.setEsAnterior(sn(getSafe(actLabTrabajoAnterior, i)));
                fal.setEsActual(sn(getSafe(actLabTrabajoActual, i)));
                fal.setIncidente(sn(getSafe(actLabIncidenteChk, i)));
                fal.setAccidente(sn(getSafe(actLabAccidenteChk, i)));
                fal.setEnfOcupacional(sn(getSafe(actLabEnfermedadChk, i)));

                // En tu UI lo estás usando como iessFecha / iessEspecificar:
                // En BD se llama FECHA_EVENTO y ESPECIFICAR.
                fal.setFechaEvento(getSafe(iessFecha, i));
                fal.setEspecificar(getSafe(iessEspecificar, i));
                fal.setObservaciones(getSafe(actLabObservaciones, i));

                fichaActLaboralService.guardar(fal);
            }

            // ============================================================
            // 6) I: ACTIVIDADES EXTRALABORALES (NO INSERTAR VACÍAS)
            // ============================================================
            if (tipoAct != null) {
                for (int i = 0; i < tipoAct.size(); i++) {
                    boolean filaTieneDatos
                            = !isBlank(getSafe(tipoAct, i))
                            || getSafe(fechaAct, i) != null
                            || !isBlank(getSafe(descAct, i));

                    if (!filaTieneDatos) {
                        continue;
                    }
                    // Aquí guardas si ya tienes entidad/tabla/servicio
                }
            }

            // ============================================================
            // 7) J: EXÁMENES (NO INSERTAR VACÍOS)
            // ============================================================
            if (examNombre != null) {
                for (int i = 0; i < examNombre.size(); i++) {
                    boolean filaTieneDatos
                            = !isBlank(getSafe(examNombre, i))
                            || getSafe(examFecha, i) != null
                            || !isBlank(getSafe(examResultado, i));

                    if (!filaTieneDatos) {
                        continue;
                    }
                    // Aquí guardas si ya tienes entidad/tabla/servicio
                }
            }

            // ============================================================
            // 8) K: DIAGNÓSTICOS (guardar SOLO filas con datos)
            // ============================================================
            if (listaDiag != null && !listaDiag.isEmpty()) {
                for (ConsultaDiagnostico cd : listaDiag) {
                    if (cd == null) {
                        continue;
                    }

                    String cod = cd.getCodigo();
                    String desc = cd.getDescripcion();
                    String tipo = cd.getTipoDiag();

                    boolean tieneDatos = !isBlank(cod) || !isBlank(desc) || !isBlank(tipo);
                    if (!tieneDatos) {
                        continue;
                    }

                    // Aquí guardas si ya tienes entidad/tabla/servicio real (FichaDiagnostico)
                }
            }

            registrarAuditoria("GUARDAR_STEP3", "FICHA_OCUPACIONAL", "*",
                    "Step 3 guardado. ID_FICHA=" + ficha.getIdFicha());

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "OK",
                    "Step 3 guardado correctamente (incluye Actividades Laborales)."
            ));

        } catch (Exception e) {
            log.error("Error en guardarStep3", e);
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Ocurrió un error al guardar el Step 3."
            ));
            ctx.validationFailed();
        }
    }

    /**
     * Helpers seguros (para no reventar por índices)
     */
    private <T> T getSafe(List<T> list, int idx) {
        if (list == null || idx < 0 || idx >= list.size()) {
            return null;
        }
        return list.get(idx);
    }

    private boolean isTrue(Boolean b) {
        return b != null && b;
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

    public void onKCieCodigoSelect(SelectEvent event) {
        int idx = getIdx(event);
        onCie10SelectCodigo(idx); // usa tu método existente (int index)
    }

    public void onKCieCodigoBlur() {
        // blur NO trae SelectEvent, así que toma idx desde el componente actual:
        int idx = getIdxFromCurrentComponent();
        onCie10SelectCodigo(idx);
    }

    public void onKDescSelect(SelectEvent event) {
        int idx = getIdx(event);
        onCie10SelectDescripcion(idx);
    }

    public void onKDescBlur() {
        int idx = getIdxFromCurrentComponent();
        onCie10SelectDescripcion(idx);
    }

// =====================
// Helpers
// =====================
    private String sn(Boolean b) {
        return Boolean.TRUE.equals(b) ? "S" : "N";
    }

    private String sn(boolean b) {
        return b ? "S" : "N";
    }

    private int getIdx(SelectEvent event) {
        UIComponent comp = event.getComponent();
        Object o = comp.getAttributes().get("idx");
        return (o == null) ? -1 : Integer.parseInt(o.toString());
    }

    private int getIdxFromCurrentComponent() {
        UIComponent comp = javax.faces.context.FacesContext.getCurrentInstance()
                .getViewRoot().findComponent(getClientIdFromRequest());
        if (comp == null) {
            return -1;
        }
        Object o = comp.getAttributes().get("idx");
        return (o == null) ? -1 : Integer.parseInt(o.toString());
    }

    /**
     * Si no quieres esto, te doy alternativa más simple.
     */
    private String getClientIdFromRequest() {
        return javax.faces.context.FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap().get("javax.faces.source");
    }

    public List<Cie10> completarCie10FilaPorCodigo(String query) {
        if (query == null) {
            return new ArrayList<>();
        }
        String q = query.trim().toUpperCase();
        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);

        List<Cie10> out = new ArrayList<>();
        for (Cie10 c : lista) {
            if (c != null && c.getCodigo() != null
                    && c.getCodigo().toUpperCase().startsWith(q)) {
                out.add(c);
            }
        }
        return out;
    }

    public List<Cie10> completarCie10FilaPorDescripcion(String query) {
        if (query == null) {
            return new ArrayList<>();
        }
        String q = query.trim();
        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        return cie10Service.buscarPorCodigoODescripcion(q);
    }

// TAB / ENTER en campo DESCRIPCIÓN
    public void onCie10BlurDescripcion(int index) {
        onCie10SelectDescripcion(index);
    }

    public void onCie10FilaSelect(int index, SelectEvent event) {
        ConsultaDiagnostico diag = ensureDiag(index);

        Cie10 cie = (Cie10) event.getObject();
        if (cie == null) {
            diag.setCodigo(null);
            diag.setDescripcion(null);
            diag.setCie10(null);
            return;
        }

        diag.setCodigo(cie.getCodigo());
        diag.setDescripcion(cie.getDescripcion());
        diag.setCie10(cie);
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
            PrimeFaces.current().ajax().update("layoutForm:noHistoriaClinica");
            PrimeFaces.current().ajax().addCallbackParam("validationFailed", false);
            PrimeFaces.current().executeScript(
                    "PF('dlgPersonaAux').hide(); PF('dlgCedula').hide();"
            );

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Datos guardados",
                    "Se guardó la persona auxiliar y se cargaron los datos en la ficha."
            ));

            log.info("PersonaAux guardada manualmente: {} {} / {} {} (cedula={})",
                    personaAux.getApellido1(),
                    personaAux.getApellido2(),
                    personaAux.getNombre1(),
                    personaAux.getNombre2(),
                    personaAux.getCedula());

        } catch (Exception e) {
            log.error("Error guardando datos manuales", e);
            // log.log(Level.SEVERE, "Error guardando datos manuales", e);
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
        permitirIngresoManual = false;

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
            // Asegurar objetos
            if (ficha == null) {
                ficha = new FichaOcupacional();
            }
            if (personaAux == null) {
                personaAux = new PersonaAux();
            }

            DatEmpleado emp = empleadoService.buscarPorCedula(cedula);

            if (emp != null) {
                // =========================
                // EMPLEADO ENCONTRADO
                // =========================
                this.empleadoSel = emp;
                this.noPersonaSel = emp.getNoPersona();

                this.apellido1 = emp.getPriApellido();
                this.apellido2 = emp.getSegApellido();

                // Si en RRHH viene todo en "NOMBRES", lo dejas así (o separas luego)
                this.nombre1 = emp.getNombres();
                this.nombre2 = null;

                this.sexo = (emp.getSexo() != null) ? emp.getSexo().getCodigo() : null;
                this.fechaNacimiento = emp.getFNacimiento();
                this.edad = calcularEdad(this.fechaNacimiento);

                // ✅ HISTORIA CLÍNICA (fuente de verdad)
                String cedEmp = emp.getNoCedula();
                ficha.setNoHistoriaClinica(cedEmp);

                // (Opcional) si sigues usando noHistoria en PDF viejo:
                this.noHistoria = cedEmp;

                // Amarrar paciente a la ficha
                ficha.setEmpleado(emp);
                ficha.setPersonaAux(null);

                mostrarDlgCedula = false;
                permitirIngresoManual = false;

                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        "Búsqueda",
                        "Se cargó la información del empleado desde RRHH."
                ));

            } else {
                // =========================
                // NO ENCONTRADO -> MANUAL
                // =========================
                this.empleadoSel = null;
                this.noPersonaSel = null;

                // Prellenar para auxiliar
                personaAux.setCedula(cedula);
                personaAux.setApellido1(null);
                personaAux.setApellido2(null);
                personaAux.setNombre1(null);
                personaAux.setNombre2(null);
                personaAux.setSexo(null);
                personaAux.setFechaNac(null);
                personaAux.setNoPersona(null);

                // ✅ Historia clínica igual se llena con la cédula ingresada
                ficha.setNoHistoriaClinica(cedula);
                this.noHistoria = cedula;

                mostrarDlgCedula = true;
                permitirIngresoManual = true;

                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Búsqueda",
                        "No se encontró la cédula en RRHH. Puede ingresarla manualmente."
                ));
            }

            // ✅ Actualiza lo correcto: el campo que muestra historia clínica y los datos visibles
            PrimeFaces.current().ajax().update(
                    "layoutForm:wiz",
                    "layoutForm:noHistoriaClinica"
            );

        } catch (Exception e) {
            permitirIngresoManual = false;
            mostrarDlgCedula = true;

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Ocurrió un error al buscar la cédula. Intente nuevamente."
            ));
            log.error("Error buscarCedula()", e);
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

    public String getStepActual() {
        return currentStep;
    }

    public void setStepActual(String stepActual) {
        this.currentStep = stepActual;
    }

    public Date getFechaAtencion() {
        if (fechaAtencion == null) {
            fechaAtencion = new Date(); // hoy
        }
        return fechaAtencion;
    }

    public void setFechaAtencion(Date fechaAtencion) {
        this.fechaAtencion = fechaAtencion;
    }
    // ====== Getters/Setters explícitos para JSF (evita PropertyNotFound) ======

    public List<String> getTipoAct() {
        if (tipoAct == null) {
            tipoAct = new ArrayList<>();
        }
        return tipoAct;
    }

    public void setTipoAct(List<String> tipoAct) {
        this.tipoAct = tipoAct;
    }

    public List<Date> getFechaAct() {
        if (fechaAct == null) {
            fechaAct = new ArrayList<>();
        }
        return fechaAct;
    }

    public void setFechaAct(List<Date> fechaAct) {
        this.fechaAct = fechaAct;
    }

    public List<String> getDescAct() {
        if (descAct == null) {
            descAct = new ArrayList<>();
        }
        return descAct;
    }

    public void setDescAct(List<String> descAct) {
        this.descAct = descAct;
    }

    private void ensureActLabSize() {
        int n = H_ROWS; // 8

        if (actLabRows == null) {
            actLabRows = new ArrayList<>();
        }
        if (actLabCentroTrabajo == null) {
            actLabCentroTrabajo = new ArrayList<>();
        }
        if (actLabActividad == null) {
            actLabActividad = new ArrayList<>();
        }
        if (actLabTiempo == null) {
            actLabTiempo = new ArrayList<>();
        }

        if (actLabTrabajoAnterior == null) {
            actLabTrabajoAnterior = new ArrayList<>();
        }
        if (actLabTrabajoActual == null) {
            actLabTrabajoActual = new ArrayList<>();
        }
        if (actLabIncidenteChk == null) {
            actLabIncidenteChk = new ArrayList<>();
        }
        if (actLabAccidenteChk == null) {
            actLabAccidenteChk = new ArrayList<>();
        }
        if (actLabEnfermedadChk == null) {
            actLabEnfermedadChk = new ArrayList<>();
        }

        if (actLabObservaciones == null) {
            actLabObservaciones = new ArrayList<>();
        }
        if (iessSi == null) {
            iessSi = new ArrayList<>();
        }
        if (iessNo == null) {
            iessNo = new ArrayList<>();
        }
        if (iessFecha == null) {
            iessFecha = new ArrayList<>();
        }
        if (iessEspecificar == null) {
            iessEspecificar = new ArrayList<>();
        }

        // rows 1..8
        while (actLabRows.size() < n) {
            actLabRows.add(String.valueOf(actLabRows.size() + 1));
        }

        while (actLabCentroTrabajo.size() < n) {
            actLabCentroTrabajo.add("");
        }
        while (actLabActividad.size() < n) {
            actLabActividad.add("");
        }
        while (actLabTiempo.size() < n) {
            actLabTiempo.add("");
        }

        while (actLabTrabajoAnterior.size() < n) {
            actLabTrabajoAnterior.add(Boolean.FALSE);
        }
        while (actLabTrabajoActual.size() < n) {
            actLabTrabajoActual.add(Boolean.FALSE);
        }
        while (actLabIncidenteChk.size() < n) {
            actLabIncidenteChk.add(Boolean.FALSE);
        }
        while (actLabAccidenteChk.size() < n) {
            actLabAccidenteChk.add(Boolean.FALSE);
        }
        while (actLabEnfermedadChk.size() < n) {
            actLabEnfermedadChk.add(Boolean.FALSE);
        }

        while (actLabObservaciones.size() < n) {
            actLabObservaciones.add("");
        }

        while (iessSi.size() < n) {
            iessSi.add(Boolean.FALSE);
        }
        while (iessNo.size() < n) {
            iessNo.add(Boolean.FALSE);
        }
        while (iessFecha.size() < n) {
            iessFecha.add(null);
        }
        while (iessEspecificar.size() < n) {
            iessEspecificar.add("");
        }
    }

    private boolean filaActLabTieneAlgo(int i) {
        // texto clave
        if (!isBlank(actLabCentroTrabajo.get(i))) {
            return true;
        }
        if (!isBlank(actLabActividad.get(i))) {
            return true;
        }
        if (!isBlank(actLabTiempo.get(i))) {
            return true;
        }
        if (!isBlank(actLabObservaciones.get(i))) {
            return true;
        }
        if (!isBlank(iessEspecificar.get(i))) {
            return true;
        }

        // checks
        if (Boolean.TRUE.equals(actLabTrabajoAnterior.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabTrabajoActual.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabIncidenteChk.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabAccidenteChk.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(actLabEnfermedadChk.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(iessSi.get(i))) {
            return true;
        }
        if (Boolean.TRUE.equals(iessNo.get(i))) {
            return true;
        }

        // fecha iess
        if (iessFecha.get(i) != null) {
            return true;
        }

        return false;
    }

    // 1..4
    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getProcessStepId() {
        // Ajusta el id del form si NO es layoutForm
        return ":layoutForm:wiz:" + currentStep;
    }

    

    private static final int CONS_ROWS = 3; // 0=Tabaco,1=Alcohol,2=Otras

private void initConsumoVidaCond() {

    final int N = CONS_ROWS;

    if (consTiempoConsumoMeses == null)      consTiempoConsumoMeses = new Integer[N];
    if (consExConsumidor == null)            consExConsumidor       = new Boolean[N];
    if (consTiempoAbstinenciaMeses == null)  consTiempoAbstinenciaMeses = new Integer[N];
    if (consNoConsume == null)               consNoConsume          = new Boolean[N];

    if (afCual == null)   afCual = new String[N];
    if (afTiempo == null) afTiempo = new String[N];

    if (medCual == null)  medCual = new String[N];
    if (medCant == null)  medCant = new Integer[N];

    // Defaults para evitar null en checkboxes
    for (int i = 0; i < N; i++) {
        if (consExConsumidor[i] == null) consExConsumidor[i] = Boolean.FALSE;
        if (consNoConsume[i] == null)    consNoConsume[i] = Boolean.FALSE;
    }

    // Observación (evitar null)
    if (consumoVidaCondObs == null) consumoVidaCondObs = "";
}


    // ===============================
// ALIASES para que el XHTML funcione
// ===============================
public Integer[] getConsTiempoConsumo() {
    return consTiempoConsumoMeses;
}
public void setConsTiempoConsumo(Integer[] v) {
    this.consTiempoConsumoMeses = v;
}

public Integer[] getConsTiempoAbstinencia() {
    return consTiempoAbstinenciaMeses;
}
public void setConsTiempoAbstinencia(Integer[] v) {
    this.consTiempoAbstinenciaMeses = v;
}

public String getConsObservacion() {
    return consumoVidaCondObs;
}
public void setConsObservacion(String v) {
    this.consumoVidaCondObs = v;
}


}
