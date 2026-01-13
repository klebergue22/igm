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
import ec.gob.prueba.prueba_maven.modelo.FichaExamenComp;
import ec.gob.prueba.prueba_maven.modelo.FichaRiesgoDet;
import ec.gob.prueba.prueba_maven.servicio.Cie10Service;
import ec.gob.prueba.prueba_maven.servicio.EmpleadoService;
import ec.gob.prueba.prueba_maven.servicio.FichaOcupacionalService;
import ec.gob.prueba.prueba_maven.servicio.FichaRiesgoService;
import ec.gob.prueba.prueba_maven.servicio.PersonaAuxService;
import ec.gob.prueba.prueba_maven.servicio.SignosVitalesService;
import ec.gob.prueba.prueba_maven.servicio.AuditoriaConsultorioService;
import ec.gob.prueba.prueba_maven.servicio.FichaActLaboralService;
import ec.gob.prueba.prueba_maven.servicio.FichaDiagnosticoService;
import ec.gob.prueba.prueba_maven.servicio.FichaExamenCompService;
import ec.gob.prueba.prueba_maven.servicio.FichaRiesgoDetService;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import javax.faces.component.UIComponent;
import org.primefaces.event.FlowEvent;
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
  
    private String activeStep = "step1";

    private boolean mostrarDlgCedula = true;
    private boolean preRenderDone = false;
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
    private String motivoObs;

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
    private List<String> examNombre = new ArrayList<>();
    private List<String> examResultado = new ArrayList<>();
    private List<Date> examFecha = new ArrayList<>();

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
    @EJB
    private FichaRiesgoDetService fichaRiesgoDetService;

    @EJB
    private FichaExamenCompService fichaExamenCompService;

 public void preRenderInit() {
    try {
        FacesContext fc = FacesContext.getCurrentInstance();

        // Evita NPE raro si no hay FacesContext (casos no comunes)
        if (fc == null) {
            return;
        }

        final boolean postback = fc.isPostback();

        // =========================================================
        // 1) Decisión del diálogo SOLO en la primera carga (GET)
        //    y SOLO si estamos en step1
        // =========================================================
        if (!postback && !preRenderDone) {
            // Solo Step1 puede mostrar el diálogo
            mostrarDlgCedula = ("step1".equals(activeStep) && empleadoSel == null);
        }

        // Si ya NO estás en Step1, apaga el diálogo SIEMPRE
        // (por seguridad para que nunca se reabra en step2/3/4)
        if (!"step1".equals(activeStep)) {
            mostrarDlgCedula = false;
        }

        // =========================================================
        // 2) Inicialización pesada SOLO una vez
        // =========================================================
        if (preRenderDone) {
            // Aun así, asegurar tamaño de listas para evitar NPE si el XHTML las usa
            ensureActLabSize();
            return;
        }
        preRenderDone = true;

        // =========================================================
        // 3) Solo en GET inicial
        // =========================================================
        if (!postback) {
            initExamenes(5);
        }

        // =========================================================
        // 4) Asegurar listas SIEMPRE
        // =========================================================
        ensureActLabSize();

    } catch (Exception e) {
        // log si quieres
        // log.error("preRenderInit error", e);
    }
}


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
        actLabIncidente = new ArrayList<>(java.util.Collections.nCopies(n, ""));
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
        if (fichaRiesgo == null) {
            fichaRiesgo = new FichaRiesgo();
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

        consTiempoConsumoMeses = new Integer[]{0, 0, 0};
        consTiempoAbstinenciaMeses = new Integer[]{0, 0, 0};

        consExConsumidor = new Boolean[]{false, false, false};
        consNoConsume = new Boolean[]{false, false, false};

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
        if (personaAux == null) {
            personaAux = new PersonaAux();
        }
        permitirIngresoManual = false;

    }

    public void onNoConsumeChange(int idx) {
        if (Boolean.TRUE.equals(consNoConsume[idx])) {
            consExConsumidor[idx] = false;
            consTiempoConsumoMeses[idx] = 0;
            consTiempoAbstinenciaMeses[idx] = 0;
        }
    }

    /**
     * Se llama cuando el cliente va a mostrar el diálogo (via remoteCommand).
     * Sirve para que NO se vuelva a abrir al cambiar de step (AJAX updates).
     */
    public void onDlgCedulaShown() {
        mostrarDlgCedula = false;
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
public String onFlow(FlowEvent event) {
    this.activeStep = event.getNewStep();

    // Si sales de step1, apaga diálogo sí o sí
    if (!"step1".equals(this.activeStep)) {
        this.mostrarDlgCedula = false;
    }

    return event.getNewStep();
}

    // ===========================================================
    // CÁLCULO EDAD
    // ===========================================================

    public void calcularEdad() {
        this.edad = calcularEdad(this.fechaNacimiento);
    }

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
        if (peso != null && tallaCm != null && tallaCm > 0) {
            double m = tallaCm / 100.0;
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
        s3("registrarAuditoria() accion=" + accion + " tabla=" + tabla + " campo=" + campo);

        try {
            AuditoriaConsultorio aud = new AuditoriaConsultorio();
            aud.setModulo("CENTRO_MEDICO");
            aud.setUsuario("USR_APP");
            aud.setFecha(new Date());
            aud.setAccion(accion);
            aud.setTablaAfecta(tabla);
            aud.setCampoAfecta(campo);
            aud.setObservaciones(observaciones);

            auditoriaService.guardar(aud);

            s3("registrarAuditoria() OK");
        } catch (Exception e) {
            // No romper flujo, pero LOGEAR BIEN
            s3e("registrarAuditoria() FALLÓ", e);
        }
    }

    // ===========================================================
    // WIZARD: GUARDAR POR STEP
    // ===========================================================
    public void guardarStepActual() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        s3("guardarStepActual() INICIO - currentStep=" + activeStep);

        try {
            if ("step1".equals(activeStep)) {
                s3("Ejecutando guardarStep1()");
                guardarStep1();
                s3("Fin guardarStep1() validationFailed=" + ctx.isValidationFailed());
                if (!ctx.isValidationFailed()) {
                    activeStep = "step2";
                }
                return;
            }

            if ("step2".equals(activeStep)) {
                s3("Validando Step2...");
                if (!validarStep2()) {
                    s3("validarStep2()=false -> NO avanza");
                    ctx.validationFailed();
                    return;
                }
                s3("Ejecutando guardarStep2()");
                guardarStep2();
                s3("Fin guardarStep2() validationFailed=" + ctx.isValidationFailed());
                if (!ctx.isValidationFailed()) {
                    activeStep = "step3";
                }
                return;
            }

            if ("step3".equals(activeStep)) {
                s3("Ejecutando guardarStep3()");
                guardarStep3();
                s3("Fin guardarStep3() validationFailed=" + ctx.isValidationFailed());
                if (!ctx.isValidationFailed()) {
                    activeStep = "step4";
                }
                return;
            }

            s3("No hay acción para currentStep=" + activeStep);

        } catch (Exception ex) {
            s3e("Excepción en guardarStepActual()", ex);
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "Ocurrió un error al guardar la información del paso actual."));
            ctx.validationFailed();
        }
    }

    public void retrocederStep() {
        if ("step2".equals(activeStep)) {
            activeStep = "step1";
        } else if ("step3".equals(activeStep)) {
            activeStep = "step2";
        } else if ("step4".equals(activeStep)) {
            activeStep = "step3";
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

    private String construirMedidas(List<String> medidas) {
        if (medidas == null || medidas.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < medidas.size(); i++) {
            String m = medidas.get(i);
            if (!isBlank(m)) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append("M").append(i + 1).append(": ").append(m.trim());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

// ===== parser para keys tipo: FIS_TEMP_ALTAS_1 =====
    private RiskKey parseRiskKey(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();

        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }

        String actStr = k.substring(last + 1);
        Integer act;
        try {
            act = Integer.valueOf(actStr);
        } catch (Exception ex) {
            return null;
        }

        String pref = k.substring(0, 3); // FIS / SEG / QUI / BIO / ERG / PSI
        String grupo = grupoFromPrefix(pref);

        // item “normalizado” desde la key (sin el _N final)
        String item = k.substring(0, last).replace('_', ' '); // FIS TEMP ALTAS
        // opcional: quitar prefijo del item (FIS )
        if (item.startsWith(pref + " ")) {
            item = item.substring(4);
        }

        return new RiskKey(grupo, item, act);
    }

// ===== parser para OTROS: FIS_OTROS_1 =====
    private RiskKey parseRiskKeyOtros(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();

        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }

        String actStr = k.substring(last + 1);
        Integer act;
        try {
            act = Integer.valueOf(actStr);
        } catch (Exception ex) {
            return null;
        }

        String pref = k.substring(0, 3);
        String grupo = grupoFromPrefix(pref);

        return new RiskKey(grupo, "OTROS", act);
    }

    private String grupoFromPrefix(String pref) {
        switch (pref) {
            case "FIS":
                return "FISICO";
            case "SEG":
                return "SEGURIDAD";
            case "QUI":
                return "QUIMICO";
            case "BIO":
                return "BIOLOGICO";
            case "ERG":
                return "ERGONOMICO";
            case "PSI":
                return "PSICOSOCIAL";
            default:
                return "OTROS";
        }
    }

    private static class RiskKey {

        final String grupo;
        final String item;
        final Integer actividad;

        RiskKey(String grupo, String item, Integer actividad) {
            this.grupo = grupo;
            this.item = item;
            this.actividad = actividad;
        }
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
            //1) VALIDACIONES (solo Step1 / BD)
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

            if (tallaCm == null) {
                warn("Debe ingresar la talla (cm).");
                return;
            }

            // ==============================
            //2) ORIGEN DEL PACIENTE (SIN guardar ficha aquí)
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
            //3) MAPEO A FICHA_OCUPACIONAL (Step1)
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
            //4) ARMAR / GUARDAR SIGNOS_VITALES
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
            //5) GUARDAR FICHA (BORRADOR) - ÚNICO GUARDADO
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

            System.out.println("===== [STEP1] ERROR EXCEPCIÓN =====");
            e.printStackTrace();

            // --- AGREGA ESTO PARA VER EL ERROR EN PANTALLA ---
            // Convertimos la excepción en texto largo para verla en el navegador
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String detalleError = sw.toString();

            // Enviamos el mensaje a la pantalla
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error detallado en Paso 1",
                    "Ha ocurrido un error:\n" + detalleError
            ));
            // --------------------------------------------------

            // Mantenemos el log original por si acaso
            log.error("Error en guardarStep1", e);

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

        try {
            // Validaciones iniciales
            if (!validarStep2()) {
                ctx.validationFailed();
                return;
            }

            final Date ahora = new Date();
            final String usr = "USR_APP"; // luego lo cambiamos por el usuario logueado

            if (ficha == null || ficha.getIdFicha() == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Step 2", "Primero debe existir y estar guardada la ficha (ID_FICHA)."));
                ctx.validationFailed();
                return;
            }

            // =========================
            // 1) ENCABEZADO: FICHA_RIESGO
            // =========================
            if (fichaRiesgo == null) {
                fichaRiesgo = new FichaRiesgo();
            }
            fichaRiesgo.setFicha(ficha);

            // Actividades
            fichaRiesgo.setActividad1(getSafe(actividadesLab, 0));
            fichaRiesgo.setActividad2(getSafe(actividadesLab, 1));
            fichaRiesgo.setActividad3(getSafe(actividadesLab, 2));
            fichaRiesgo.setActividad4(getSafe(actividadesLab, 3));
            fichaRiesgo.setActividad5(getSafe(actividadesLab, 4));
            fichaRiesgo.setActividad6(getSafe(actividadesLab, 5));
            fichaRiesgo.setActividad7(getSafe(actividadesLab, 6));

            // Medidas
            fichaRiesgo.setMedidasPreventivas(construirMedidas(medidasPreventivas));

            // Auditoría (ENCABEZADO)
            if (fichaRiesgo.getIdFichaRiesgo() == null) {
                fichaRiesgo.setEstado("BORRADOR");
                fichaRiesgo.setFCreacion(ahora);
                fichaRiesgo.setUsrCreacion(usr);
            } else {
                fichaRiesgo.setFActualizacion(ahora);
                fichaRiesgo.setUsrActualizacion(usr);
            }

            fichaRiesgo = fichaRiesgoService.guardar(fichaRiesgo);

            // =========================
            // 2) DETALLE: FICHA_RIESGO_DET
            //    estrategia: REEMPLAZAR todo (delete + insert)
            // =========================
            fichaRiesgoDetService.eliminarPorFicha(ficha.getIdFicha());

            // 2.1) checks marcados
            if (riesgos != null && !riesgos.isEmpty()) {
                int orden = 1;

                for (Map.Entry<String, Boolean> e : riesgos.entrySet()) {
                    if (!Boolean.TRUE.equals(e.getValue())) {
                        continue;
                    }

                    RiskKey rk = parseRiskKey(e.getKey()); // ej: FIS_TEMP_ALTAS_1
                    if (rk == null) {
                        continue;
                    }

                    FichaRiesgoDet det = new FichaRiesgoDet();
                    det.setFicha(ficha);
                    det.setGrupo(rk.grupo);
                    det.setItem(rk.item);
                    det.setActividadNro(rk.actividad);
                    det.setMarcado("S");
                    det.setOrden(orden++);

                    // ✅ IMPORTANTE: pasar usuario (auditoría en servicio)
                    fichaRiesgoDetService.guardar(det, usr);
                }
            }

            // 2.2) “otros”
            if (otrosRiesgos != null && !otrosRiesgos.isEmpty()) {
                int ordenOtros = 10000;

                for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
                    String val = e.getValue();
                    if (isBlank(val)) {
                        continue;
                    }

                    RiskKey rk = parseRiskKeyOtros(e.getKey()); // ej: FIS_OTROS_1
                    if (rk == null) {
                        continue;
                    }

                    FichaRiesgoDet det = new FichaRiesgoDet();
                    det.setFicha(ficha);
                    det.setGrupo(rk.grupo);
                    det.setItem("OTROS: " + val.trim());
                    det.setActividadNro(rk.actividad);
                    det.setMarcado("S");
                    det.setOrden(ordenOtros++);

                    // ✅ IMPORTANTE: pasar usuario
                    fichaRiesgoDetService.guardar(det, usr);
                }
            }

            registrarAuditoria("GUARDAR_STEP2", "FICHA_RIESGO / FICHA_RIESGO_DET", "*",
                    "Step 2 guardado. ID_FICHA=" + ficha.getIdFicha());

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO, "Step 2",
                    "Riesgos laborales guardados correctamente (encabezado + detalle)."));

        } catch (Exception e) {

            System.out.println("===== [STEP2] ERROR EXCEPCIÓN =====");
            e.printStackTrace();

            // --- AGREGA ESTO PARA VER EL ERROR EN PANTALLA ---
            FacesContext ctxCatch = FacesContext.getCurrentInstance();

            // Convertimos la excepción en texto largo para verla en el navegador
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String detalleError = sw.toString();

            // Enviamos el mensaje a la pantalla
            ctxCatch.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error detallado en Paso 2",
                    "Ha ocurrido un error:\n" + detalleError
            ));
            // --------------------------------------------------

            ctx.validationFailed();
        }
    }

    /**
     * Validaciones del STEP 3: - Al menos 1 diagnóstico - Aptitud seleccionada
     * - Al menos 1 recomendación - Nombre del profesional - Código del médico
     */
    private boolean validarStep3() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        s3("validarStep3() INICIO");

        boolean valido = true;

        // 1) Diagnóstico
        boolean hayDiagnostico = false;
        if (listaDiag != null) {
            for (int i = 0; i < listaDiag.size(); i++) {
                ConsultaDiagnostico d = listaDiag.get(i);
                if (d == null) {
                    continue;
                }

                boolean tiene = !isBlank(d.getCodigo()) || !isBlank(d.getDescripcion()) || d.getCie10() != null;
                if (tiene) {
                    hayDiagnostico = true;
                    s3("validarStep3(): diagnóstico encontrado en fila " + (i + 1)
                            + " codigo=" + d.getCodigo() + " cie=" + (d.getCie10() != null));
                    break;
                }
            }
        }
        if (!hayDiagnostico) {
            s3("validarStep3() FAIL: no hay diagnósticos");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe registrar al menos un diagnóstico (CIE10)."));
            valido = false;
        }

        if (isBlank(aptitudSel)) {
            s3("validarStep3() FAIL: aptitudSel vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe seleccionar la aptitud médica."));
            valido = false;
        }

        if (isBlank(recomendaciones)) {
            s3("validarStep3() FAIL: recomendaciones vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe ingresar al menos una recomendación."));
            valido = false;
        }

        if (isBlank(medicoNombre)) {
            s3("validarStep3() FAIL: medicoNombre vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe ingresar el nombre del profesional."));
            valido = false;
        }

        if (isBlank(medicoCodigo)) {
            s3("validarStep3() FAIL: medicoCodigo vacío");
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Step 3", "Debe ingresar el código del médico."));
            valido = false;
        }

        s3("validarStep3() FIN -> " + valido);
        return valido;
    }

    /**
     * STEP 3: - Signos vitales básicos (peso, talla) - Aptitud, observaciones,
     * recomendaciones - CIE10 principal, médico, fechas Aquí SÍ se persiste la
     * FICHA_OCUPACIONAL (y SIGNOS_VITALES).
     */
    public void guardarStep3() {

        System.out.println("===== [STEP3] INICIO guardarStep3 =====");

        FacesContext ctx = FacesContext.getCurrentInstance();

        try {

            // =========================================================
            // 0) VALIDAR EXISTENCIA DE FICHA
            // =========================================================
            if (ficha == null || ficha.getIdFicha() == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Atención",
                        "Primero debe guardar el Step 1 para generar la ficha."
                ));
                ctx.validationFailed();
                return;
            }

            // =========================================================
            // 1) VALIDACIONES STEP 3
            // =========================================================
//            if (!validarStep3()) {
//                ctx.validationFailed();
//                return;
//            }
            final Date ahora = new Date();
            final String usuario = "USR_APP";

            // =========================================================
            // 2) GUARDAR BLOQUES UNO POR UNO (SEGUIMIENTO)
            // =========================================================
            guardarStep3_FichaGeneral(ctx, ahora, usuario);    // CIE10 Ppal + L/M/N/O + update FICHA
            guardarStep3_H_ActividadLaboral(ahora, usuario);   // H
            guardarStep3_I_Extralaborales(ahora, usuario);     // I (serializa en FICHA_OCUPACIONAL)
            guardarStep3_J_Examenes(ahora, usuario);           // J (FICHA_EXAMEN_COMP)
            guardarStep3_K_Diagnosticos(ahora, usuario);       // K (si tu service/tabla existe)

            // =========================================================
            // 3) AUDITORÍA
            // =========================================================
            registrarAuditoria(
                    "GUARDAR_STEP3",
                    "FICHA_OCUPACIONAL / H / I / J / K",
                    "*",
                    "Step 3 guardado. ID_FICHA=" + ficha.getIdFicha()
            );

            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "OK",
                    "Step 3 guardado correctamente."
            ));

            System.out.println("===== [STEP3] FIN OK =====");

        } catch (Exception e) {

            System.out.println("===== [STEP3] ERROR EXCEPCIÓN =====");
            e.printStackTrace();

            // Convertimos la excepción en texto largo para verla en el navegador
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String detalleError = sw.toString();

            // Enviamos el mensaje a la pantalla
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Error detallado en Paso 3",
                    "Ha ocurrido un error:\n" + detalleError
            ));
            // --------------------------------------------------

            ctx.validationFailed();
        }
    }

    private void guardarStep3_FichaGeneral(FacesContext ctx, Date ahora, String usuario) {

        System.out.println("STEP3-A: Guardando datos generales en FICHA_OCUPACIONAL");

        // 1) CIE10 PRINCIPAL
        if (!isBlank(codCie10Ppal)) {
            Cie10 cie = cie10Service.buscarPorCodigo(codCie10Ppal.trim());
            if (cie == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Validación",
                        "El código CIE10 principal no existe: " + codCie10Ppal
                ));
                ctx.validationFailed();
                throw new IllegalStateException("CIE10 principal no existe: " + codCie10Ppal);
            }
            ficha.setCie10Principal(cie);
        } else {
            ficha.setCie10Principal(null);
        }

        // 2) L / M / N / O
        ficha.setAptitudSel(aptitudSel);
        ficha.setDetalleObs(detalleObservaciones);
        ficha.setRecomendaciones(recomendaciones);

        ficha.setNRetEval(nRealizaEvaluacion);    // 'S'/'N'
        ficha.setNRetRelTrab(nRelacionTrabajo);   // 'S'/'N'
        ficha.setNRetObs(nObsRetiro);

        ficha.setMedicoNombre(medicoNombre);
        ficha.setMedicoCodigo(medicoCodigo);

        ficha.setFechaEmision(fechaEmision != null ? fechaEmision : ahora);

        // Auditoría FICHA
        ficha.setFechaActualizacion(ahora);
        ficha.setUsrActualizacion(usuario);

        // Update
        ficha = fichaService.guardar(ficha);

        System.out.println("STEP3-A-OK: FICHA_OCUPACIONAL actualizada. ID_FICHA=" + ficha.getIdFicha());
    }

    private void guardarStep3_H_ActividadLaboral(Date ahora, String usuario) {

        System.out.println("STEP3-H: Procesando Actividad Laboral (FICHA_ACT_LABORAL)");

        ensureActLabSize(); // tu método actual

        for (int i = 0; i < H_ROWS; i++) {

            int nroFila = i + 1;

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

            if (!filaTieneDatos) {
                // Si está vacía, elimino el registro de esa fila si existe
                fichaActLaboralService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
                continue;
            }

            FichaActLaboral fal = fichaActLaboralService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);

            if (fal == null) {
                fal = new FichaActLaboral();
                fal.setFicha(ficha);
                fal.setNroFila(nroFila);
                fal.setFCreacion(ahora);
                fal.setUsrCreacion(usuario);
            } else {
                fal.setFActualizacion(ahora);
                fal.setUsrActualizacion(usuario);
            }

            fal.setCentroTrabajo(getSafe(actLabCentroTrabajo, i));
            fal.setActividad(getSafe(actLabActividad, i));
            fal.setTiempo(getSafe(actLabTiempo, i));

            fal.setEsAnterior(sn(getSafe(actLabTrabajoAnterior, i)));
            fal.setEsActual(sn(getSafe(actLabTrabajoActual, i)));
            fal.setIncidente(sn(getSafe(actLabIncidenteChk, i)));
            fal.setAccidente(sn(getSafe(actLabAccidenteChk, i)));
            fal.setEnfOcupacional(sn(getSafe(actLabEnfermedadChk, i)));

            // En tu UI esto es IESS: fecha y especificar
            fal.setFechaEvento(getSafe(iessFecha, i));
            fal.setEspecificar(getSafe(iessEspecificar, i));
            fal.setObservaciones(getSafe(actLabObservaciones, i));

            fichaActLaboralService.guardar(fal);
        }

        System.out.println("STEP3-H-OK");
    }

    private void guardarStep3_I_Extralaborales(Date ahora, String usuario) {

        System.out.println("STEP3-I: Procesando Actividades Extralaborales (SERIALIZADO EN FICHA)");

        // Si no hay listas, no reviento
        if (tipoAct == null || fechaAct == null || descAct == null) {
            System.out.println("STEP3-I: Listas I null -> no se guarda (no rompe)");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Date ultimaFecha = null;

        for (int i = 0; i < tipoAct.size(); i++) {

            String t = getSafe(tipoAct, i);
            Date f = getSafe(fechaAct, i);
            String d = getSafe(descAct, i);

            boolean filaTieneDatos = !isBlank(t) || f != null || !isBlank(d);
            if (!filaTieneDatos) {
                continue;
            }

            // formato simple (para seguimiento)
            sb.append(i + 1).append(") ")
                    .append(nullToDash(t)).append(" | ")
                    .append(f != null ? new java.text.SimpleDateFormat("yyyy/MM/dd").format(f) : "----/--/--")
                    .append(" | ")
                    .append(nullToDash(d))
                    .append("\n");

            if (f != null) {
                ultimaFecha = f; // te guardo la última fecha con dato
            }
        }

        ficha.setExtraLabDesc(sb.length() == 0 ? null : sb.toString().trim());
        ficha.setExtraLabFecha(ultimaFecha);

        ficha.setFechaActualizacion(ahora);
        ficha.setUsrActualizacion(usuario);

        ficha = fichaService.guardar(ficha);

        System.out.println("STEP3-I-OK");
    }

    private void guardarStep3_J_Examenes(Date ahora, String usuario) {

        System.out.println("STEP3-J: Procesando Exámenes (FICHA_EXAMEN_COMP)");

        // Seguridad: si las listas no existen, no rompo el Step3
        if (examNombre == null || examFecha == null || examResultado == null) {
            System.out.println("STEP3-J: Listas J null -> no se guarda J");
            return;
        }

        // filas = tamaño mínimo (por si una lista viene más corta)
        int filas = Math.min(examNombre.size(), Math.min(examFecha.size(), examResultado.size()));

        for (int i = 0; i < filas; i++) {

            int nroFila = i + 1;

            String nombre = getSafe(examNombre, i);
            Date fecha = getSafe(examFecha, i);
            String resultado = getSafe(examResultado, i);

            boolean filaTieneDatos
                    = !isBlank(nombre)
                    || fecha != null
                    || !isBlank(resultado);

            if (!filaTieneDatos) {
                // Si está vacía => elimino si existía
                int del = fichaExamenCompService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
                System.out.println("STEP3-J-FILA " + nroFila + ": vacía -> delete=" + del);
                continue;
            }

            // Buscar si existe esa fila
            FichaExamenComp ex = fichaExamenCompService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);

            if (ex == null) {
                ex = new FichaExamenComp();
                ex.setFicha(ficha);
                ex.setNroFila(nroFila);
                // fCreacion/usrCreacion los pone tu service cuando idFichaExamen es null
                System.out.println("STEP3-J-FILA " + nroFila + ": INSERT");
            } else {
                System.out.println("STEP3-J-FILA " + nroFila + ": UPDATE id=" + ex.getIdFichaExamen());
            }

            ex.setNombreExamen(nombre);
            ex.setFechaExamen(fecha);
            ex.setResultado(resultado);

            fichaExamenCompService.guardar(ex, usuario);
        }

        System.out.println("STEP3-J-OK");
    }

    private void guardarStep3_K_Diagnosticos(Date ahora, String usuario) {

        System.out.println("STEP3-K: Procesando Diagnósticos");

        if (listaDiag == null || listaDiag.isEmpty()) {
            System.out.println("STEP3-K: listaDiag vacía -> OK");
            return;
        }

        // si tu service no tiene nada implementado, no rompo:
        if (fichaDiagnosticoService == null) {
            System.out.println("STEP3-K: fichaDiagnosticoService null -> no se guarda K");
            return;
        }

        // ✅ OPCIÓN RECOMENDADA:
        // Implementa en tu service un método tipo:
        // guardarDiagnosticosDeFicha(Long idFicha, List<ConsultaDiagnostico> lista, Date ahora, String usuario)
        try {
            fichaDiagnosticoService.guardarDiagnosticosDeFicha(ficha.getIdFicha(), listaDiag, ahora, usuario);
            System.out.println("STEP3-K-OK (service)");
        } catch (NoSuchMethodError | RuntimeException ex) {
            System.out.println("STEP3-K: Tu service no tiene guardarDiagnosticosDeFicha(...) -> no se guarda K");
            // NO lanzo excepción para que puedas seguir guardando lo demás.
        }
    }

    private String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    /**
     * Helpers seguros (para no reventar por índices)
     */
    private <T> T getSafe(List<T> list, int idx) {
        if (list == null) {
            s3("getSafe() list=null idx=" + idx);
            return null;
        }
        if (idx < 0 || idx >= list.size()) {
            s3("getSafe() idx fuera de rango idx=" + idx + " size=" + list.size());
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
    public List<String> completarCie10PorCodigo(String query) {
        List<String> codigos = new ArrayList<>();

        if (query == null) {
            return codigos;
        }

        String q = query.trim().toUpperCase();
        if (q.isEmpty()) {
            return codigos;
        }

        List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);

        for (Cie10 c : lista) {
            if (c != null && c.getCodigo() != null) {
                String cod = c.getCodigo().toUpperCase();
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

    // =====================
    // NUEVOS MÉTODOS SIMPLIFICADOS PARA LA SECCIÓN K
    // =====================
    // Cuando se selecciona un código en la fila K (NUEVO MÉTODO)
    public void onKCieCodigoSelect(SelectEvent event) {
        String codigo = (String) event.getObject();
        UIComponent comp = event.getComponent();
        Object idxObj = comp.getAttributes().get("idx");

        if (idxObj != null) {
            int idx = Integer.parseInt(idxObj.toString());
            if (idx >= 0 && idx < listaDiag.size()) {
                ConsultaDiagnostico diag = listaDiag.get(idx);
                diag.setCodigo(codigo);

                // Buscar automáticamente la descripción
                if (codigo != null && !codigo.trim().isEmpty()) {
                    Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
                    if (cie != null) {
                        diag.setDescripcion(cie.getDescripcion());
                        diag.setCie10(cie);
                    } else {
                        diag.setDescripcion(null);
                        diag.setCie10(null);
                    }
                }
            }
        }
    }

    // Cuando se sale del campo de código (blur) en la fila K (NUEVO MÉTODO)
    public void onKCieCodigoBlur() {
        // Obtener el índice desde el contexto
        String idxParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("idx");

        if (idxParam != null) {
            int idx = Integer.parseInt(idxParam);
            if (idx >= 0 && idx < listaDiag.size()) {
                ConsultaDiagnostico diag = listaDiag.get(idx);
                String codigo = diag.getCodigo();

                // Buscar automáticamente la descripción
                if (codigo != null && !codigo.trim().isEmpty()) {
                    Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
                    if (cie != null) {
                        diag.setDescripcion(cie.getDescripcion());
                        diag.setCie10(cie);
                    } else {
                        diag.setDescripcion(null);
                        diag.setCie10(null);
                    }
                }
            }
        }
    }

    // Cuando se selecciona una descripción en la fila K (NUEVO MÉTODO)
    public void onKDescSelect(SelectEvent event) {
        String descripcion = (String) event.getObject();
        UIComponent comp = event.getComponent();
        Object idxObj = comp.getAttributes().get("idx");

        if (idxObj != null) {
            int idx = Integer.parseInt(idxObj.toString());
            if (idx >= 0 && idx < listaDiag.size()) {
                ConsultaDiagnostico diag = listaDiag.get(idx);
                diag.setDescripcion(descripcion);

                // Buscar automáticamente el código
                if (descripcion != null && !descripcion.trim().isEmpty()) {
                    Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
                    if (cie != null) {
                        diag.setCodigo(cie.getCodigo());
                        diag.setCie10(cie);
                    } else {
                        diag.setCodigo(null);
                        diag.setCie10(null);
                    }
                }
            }
        }
    }

    // Cuando se sale del campo de descripción (blur) en la fila K (NUEVO MÉTODO)
    public void onKDescBlur() {
        // Obtener el índice desde el contexto
        String idxParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("idx");

        if (idxParam != null) {
            int idx = Integer.parseInt(idxParam);
            if (idx >= 0 && idx < listaDiag.size()) {
                ConsultaDiagnostico diag = listaDiag.get(idx);
                String descripcion = diag.getDescripcion();

                // Buscar automáticamente el código
                if (descripcion != null && !descripcion.trim().isEmpty()) {
                    Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
                    if (cie != null) {
                        diag.setCodigo(cie.getCodigo());
                        diag.setCie10(cie);
                    } else {
                        diag.setCodigo(null);
                        diag.setCie10(null);
                    }
                }
            }
        }
    }

    // Método helper para obtener sugerencias de CIE10 por código (para filas K)
    public List<String> completarCie10FilaPorCodigo(String query) {
        if (query == null) {
            return new ArrayList<>();
        }
        String q = query.trim().toUpperCase();
        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);
        List<String> out = new ArrayList<>();

        for (Cie10 c : lista) {
            if (c != null && c.getCodigo() != null
                    && c.getCodigo().toUpperCase().startsWith(q)) {
                out.add(c.getCodigo());
            }
        }
        return out;
    }

    // Método helper para obtener sugerencias de CIE10 por descripción (para filas K)
    public List<String> completarCie10FilaPorDescripcion(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Cie10> lista = cie10Service.buscarPorCodigoODescripcion(query);
        List<String> out = new ArrayList<>();

        for (Cie10 c : lista) {
            if (c != null && c.getDescripcion() != null) {
                out.add(c.getDescripcion());
            }
        }
        return out;
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
public void buscarCedula() {

    FacesContext ctx = FacesContext.getCurrentInstance();
    PrimeFaces pf = PrimeFaces.current();

    permitirIngresoManual = false;
    boolean encontrado = false;
    boolean mostrarManual = false;

    if (cedulaBusqueda == null || cedulaBusqueda.trim().isEmpty()) {

        ctx.addMessage("cedulaForm:cedula", new FacesMessage(
                FacesMessage.SEVERITY_WARN,
                "Búsqueda",
                "Ingrese una cédula para realizar la búsqueda."
        ));

        pf.ajax().addCallbackParam("encontrado", false);
        pf.ajax().addCallbackParam("mostrarManual", false);

        safeUpdate(":dlgCedula:cedulaForm:msgCedula");
        safeUpdate(":dlgCedula:cedulaForm:panelBtnManualWrap");
        return;
    }

    String cedula = cedulaBusqueda.trim();

    try {
        if (ficha == null) ficha = new FichaOcupacional();
        if (personaAux == null) personaAux = new PersonaAux();

        // ✅ CLAVE: dejar la cédula lista SIEMPRE para el dialog manual
        personaAux.setCedula(cedula);

        DatEmpleado emp = empleadoService.buscarPorCedula(cedula);

        if (emp != null) {
            encontrado = true;
            mostrarManual = false;
            permitirIngresoManual = false;

            empleadoSel = emp;
            noPersonaSel = emp.getNoPersona();

            apellido1 = emp.getPriApellido();
            apellido2 = emp.getSegApellido();
            nombre1 = emp.getNombres();
            nombre2 = null;

            sexo = (emp.getSexo() != null) ? emp.getSexo().getCodigo() : null;
            fechaNacimiento = emp.getFNacimiento();
            edad = calcularEdad(fechaNacimiento);

            ficha.setNoHistoriaClinica(emp.getNoCedula());
            ficha.setEmpleado(emp);
            ficha.setPersonaAux(null);

            mostrarDlgCedula = false;

            ctx.addMessage("cedulaForm:cedula", new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Búsqueda",
                    "Información cargada desde RRHH."
            ));

        } else {
            encontrado = false;
            mostrarManual = true;
            permitirIngresoManual = true;

            empleadoSel = null;
            noPersonaSel = null;

            // ✅ limpiar manual (pero mantener cédula ya seteada arriba)
            personaAux.setApellido1(null);
            personaAux.setApellido2(null);
            personaAux.setNombre1(null);
            personaAux.setNombre2(null);
            personaAux.setSexo(null);
            personaAux.setFechaNac(null);

            ficha.setNoHistoriaClinica(cedula);
            mostrarDlgCedula = true;

            ctx.addMessage("cedulaForm:cedula", new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Búsqueda",
                    "No se encontró la cédula. Puede ingresar los datos manualmente."
            ));
        }

        // Updates del dialog de cédula
        safeUpdate(":dlgCedula:cedulaForm:msgCedula");
        safeUpdate(":dlgCedula:cedulaForm:panelBtnManualWrap");

        // ✅ CLAVE: si va a abrir manual, refresca su form para que se pinte la cédula
        if (mostrarManual) {
            safeUpdate(":dlgPersonaAuxForm:cedManual");
            safeUpdate(":dlgPersonaAuxForm:gridManual");
            safeUpdate(":dlgPersonaAuxForm:msgPersonaAux");
        }

        pf.ajax().addCallbackParam("encontrado", encontrado);
        pf.ajax().addCallbackParam("mostrarManual", mostrarManual);

    } catch (Exception e) {

        ctx.addMessage("cedulaForm:cedula", new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                "Ocurrió un error al buscar la cédula."
        ));

        pf.ajax().addCallbackParam("encontrado", false);
        pf.ajax().addCallbackParam("mostrarManual", false);

        safeUpdate(":dlgCedula:cedulaForm:msgCedula");
        safeUpdate(":dlgCedula:cedulaForm:panelBtnManualWrap");
    }
}



    private void safeUpdate(String clientId) {
        try {

            PrimeFaces.current().ajax().update(clientId);
        } catch (Exception ex) {
            // opcional: log debug
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
        return activeStep ;
    }

    public void setStepActual(String stepActual) {
        this.activeStep  = stepActual;
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
        final int n = H_ROWS; // 8

        // ===== LOG INICIO =====
        try {
            log.info("[STEP3] ensureActLabSize() INICIO - H_ROWS={}", n);
            log.info("[STEP3] ensureActLabSize() Estado inicial -> actLabRows={}, centro={}, act={}, tiempo={}, obs={}, "
                    + "trabAnt={}, trabAct={}, inc={}, acc={}, enf={}, iessSi={}, iessNo={}, iessFecha={}, iessEsp={}",
                    (actLabRows == null ? "null" : actLabRows.size()),
                    (actLabCentroTrabajo == null ? "null" : actLabCentroTrabajo.size()),
                    (actLabActividad == null ? "null" : actLabActividad.size()),
                    (actLabTiempo == null ? "null" : actLabTiempo.size()),
                    (actLabObservaciones == null ? "null" : actLabObservaciones.size()),
                    (actLabTrabajoAnterior == null ? "null" : actLabTrabajoAnterior.size()),
                    (actLabTrabajoActual == null ? "null" : actLabTrabajoActual.size()),
                    (actLabIncidenteChk == null ? "null" : actLabIncidenteChk.size()),
                    (actLabAccidenteChk == null ? "null" : actLabAccidenteChk.size()),
                    (actLabEnfermedadChk == null ? "null" : actLabEnfermedadChk.size()),
                    (iessSi == null ? "null" : iessSi.size()),
                    (iessNo == null ? "null" : iessNo.size()),
                    (iessFecha == null ? "null" : iessFecha.size()),
                    (iessEspecificar == null ? "null" : iessEspecificar.size())
            );
        } catch (Exception e) {
            e.printStackTrace(); // o Logger
        }

        // ===== 1) Inicializar listas si vienen null =====
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

        // ===== 2) Crecer a tamaño n =====
        // rows 1..n
        while (actLabRows.size() < n) {
            String val = String.valueOf(actLabRows.size() + 1);
            actLabRows.add(val);
            log.info("[STEP3] ensureActLabSize(): +actLabRows -> {}", val);
        }

        while (actLabCentroTrabajo.size() < n) {
            actLabCentroTrabajo.add("");
            log.info("[STEP3] ensureActLabSize(): +actLabCentroTrabajo (blank) idx={}", actLabCentroTrabajo.size() - 1);
        }
        while (actLabActividad.size() < n) {
            actLabActividad.add("");
            log.info("[STEP3] ensureActLabSize(): +actLabActividad (blank) idx={}", actLabActividad.size() - 1);
        }
        while (actLabTiempo.size() < n) {
            actLabTiempo.add("");
            log.info("[STEP3] ensureActLabSize(): +actLabTiempo (blank) idx={}", actLabTiempo.size() - 1);
        }

        while (actLabTrabajoAnterior.size() < n) {
            actLabTrabajoAnterior.add(Boolean.FALSE);
            log.info("[STEP3] ensureActLabSize(): +actLabTrabajoAnterior=false idx={}", actLabTrabajoAnterior.size() - 1);
        }
        while (actLabTrabajoActual.size() < n) {
            actLabTrabajoActual.add(Boolean.FALSE);
            log.info("[STEP3] ensureActLabSize(): +actLabTrabajoActual=false idx={}", actLabTrabajoActual.size() - 1);
        }
        while (actLabIncidenteChk.size() < n) {
            actLabIncidenteChk.add(Boolean.FALSE);
            log.info("[STEP3] ensureActLabSize(): +actLabIncidenteChk=false idx={}", actLabIncidenteChk.size() - 1);
        }
        while (actLabAccidenteChk.size() < n) {
            actLabAccidenteChk.add(Boolean.FALSE);
            log.info("[STEP3] ensureActLabSize(): +actLabAccidenteChk=false idx={}", actLabAccidenteChk.size() - 1);
        }
        while (actLabEnfermedadChk.size() < n) {
            actLabEnfermedadChk.add(Boolean.FALSE);
            log.info("[STEP3] ensureActLabSize(): +actLabEnfermedadChk=false idx={}", actLabEnfermedadChk.size() - 1);
        }

        while (actLabObservaciones.size() < n) {
            actLabObservaciones.add("");
            log.info("[STEP3] ensureActLabSize(): +actLabObservaciones (blank) idx={}", actLabObservaciones.size() - 1);
        }

        while (iessSi.size() < n) {
            iessSi.add(Boolean.FALSE);
            log.info("[STEP3] ensureActLabSize(): +iessSi=false idx={}", iessSi.size() - 1);
        }
        while (iessNo.size() < n) {
            iessNo.add(Boolean.FALSE);
            log.info("[STEP3] ensureActLabSize(): +iessNo=false idx={}", iessNo.size() - 1);
        }
        while (iessFecha.size() < n) {
            iessFecha.add(null);
            log.info("[STEP3] ensureActLabSize(): +iessFecha=null idx={}", iessFecha.size() - 1);
        }
        while (iessEspecificar.size() < n) {
            iessEspecificar.add("");
            log.info("[STEP3] ensureActLabSize(): +iessEspecificar (blank) idx={}", iessEspecificar.size() - 1);
        }

        // ===== 3) Normalizar nulls internos (evita NPE en Step3) =====
        for (int i = 0; i < n; i++) {
            if (actLabCentroTrabajo.get(i) == null) {
                actLabCentroTrabajo.set(i, "");
            }
            if (actLabActividad.get(i) == null) {
                actLabActividad.set(i, "");
            }
            if (actLabTiempo.get(i) == null) {
                actLabTiempo.set(i, "");
            }
            if (actLabObservaciones.get(i) == null) {
                actLabObservaciones.set(i, "");
            }
            if (iessEspecificar.get(i) == null) {
                iessEspecificar.set(i, "");
            }

            if (actLabTrabajoAnterior.get(i) == null) {
                actLabTrabajoAnterior.set(i, Boolean.FALSE);
            }
            if (actLabTrabajoActual.get(i) == null) {
                actLabTrabajoActual.set(i, Boolean.FALSE);
            }
            if (actLabIncidenteChk.get(i) == null) {
                actLabIncidenteChk.set(i, Boolean.FALSE);
            }
            if (actLabAccidenteChk.get(i) == null) {
                actLabAccidenteChk.set(i, Boolean.FALSE);
            }
            if (actLabEnfermedadChk.get(i) == null) {
                actLabEnfermedadChk.set(i, Boolean.FALSE);
            }

            if (iessSi.get(i) == null) {
                iessSi.set(i, Boolean.FALSE);
            }
            if (iessNo.get(i) == null) {
                iessNo.set(i, Boolean.FALSE);
            }
            // iessFecha puede ser null (correcto)
        }

        // ===== LOG FIN =====
        try {
            log.info("[STEP3] ensureActLabSize() FIN -> actLabRows={}, centro={}, act={}, tiempo={}, obs={}, "
                    + "trabAnt={}, trabAct={}, inc={}, acc={}, enf={}, iessSi={}, iessNo={}, iessFecha={}, iessEsp={}",
                    actLabRows.size(),
                    actLabCentroTrabajo.size(),
                    actLabActividad.size(),
                    actLabTiempo.size(),
                    actLabObservaciones.size(),
                    actLabTrabajoAnterior.size(),
                    actLabTrabajoActual.size(),
                    actLabIncidenteChk.size(),
                    actLabAccidenteChk.size(),
                    actLabEnfermedadChk.size(),
                    iessSi.size(),
                    iessNo.size(),
                    iessFecha.size(),
                    iessEspecificar.size()
            );
        } catch (Exception e) {
            e.printStackTrace(); // o Logger
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
        return ":layoutForm:wiz:" + activeStep ;
    }

    private static final int CONS_ROWS = 3; // 0=Tabaco,1=Alcohol,2=Otras

    private void initConsumoVidaCond() {

        final int N = CONS_ROWS;

        if (consTiempoConsumoMeses == null) {
            consTiempoConsumoMeses = new Integer[N];
        }
        if (consExConsumidor == null) {
            consExConsumidor = new Boolean[N];
        }
        if (consTiempoAbstinenciaMeses == null) {
            consTiempoAbstinenciaMeses = new Integer[N];
        }
        if (consNoConsume == null) {
            consNoConsume = new Boolean[N];
        }

        if (afCual == null) {
            afCual = new String[N];
        }
        if (afTiempo == null) {
            afTiempo = new String[N];
        }

        if (medCual == null) {
            medCual = new String[N];
        }
        if (medCant == null) {
            medCant = new Integer[N];
        }

        // Defaults para evitar null en checkboxes
        for (int i = 0; i < N; i++) {
            if (consExConsumidor[i] == null) {
                consExConsumidor[i] = Boolean.FALSE;
            }
            if (consNoConsume[i] == null) {
                consNoConsume[i] = Boolean.FALSE;
            }
        }

        // Observación (evitar null)
        if (consumoVidaCondObs == null) {
            consumoVidaCondObs = "";
        }
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

    // ===============================
    // Getters/Setters para campos N (retiro)
    // ===============================
    public String getNRealizaEvaluacion() {
        return nRealizaEvaluacion;
    }

    public void setNRealizaEvaluacion(String nRealizaEvaluacion) {
        this.nRealizaEvaluacion = nRealizaEvaluacion;
    }

    public String getNRelacionTrabajo() {
        return nRelacionTrabajo;
    }

    public void setNRelacionTrabajo(String nRelacionTrabajo) {
        this.nRelacionTrabajo = nRelacionTrabajo;
    }

    public String getNObsRetiro() {
        return nObsRetiro;
    }

    public void setNObsRetiro(String nObsRetiro) {
        this.nObsRetiro = nObsRetiro;
    }

    public Map<String, Boolean> getRiesgos() {
        if (riesgos == null) {
            riesgos = new java.util.LinkedHashMap<>();
        }
        return riesgos;
    }

    public Map<String, String> getOtrosRiesgos() {
        if (otrosRiesgos == null) {
            otrosRiesgos = new java.util.LinkedHashMap<>();
        }
        return otrosRiesgos;
    }

    private void s3(String msg) {
        // log al servidor (recomendado)
        log.info("[STEP3] {}", msg);

        // si quieres también consola:
        System.out.println("[STEP3] " + msg);
    }

    private void s3e(String msg, Throwable t) {
        log.error("[STEP3] " + msg, t);
        System.out.println("[STEP3-ERROR] " + msg);
        if (t != null) {
            t.printStackTrace();
        }
    }

    public long getTs() {
        return System.currentTimeMillis();
    }

}
