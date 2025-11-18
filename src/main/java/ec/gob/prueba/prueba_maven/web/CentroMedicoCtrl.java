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
import javax.annotation.PostConstruct;
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
import ec.gob.prueba.prueba_maven.servicio.Cie10Service;
import java.util.List;
import javax.ejb.EJB;

@ManagedBean(name = "centroMedicoCtrl")
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
    private Double peso;   // kg
    private Double talla;  // cm
    private Double imc;    // kg/m2

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

    public String getPdfObjectUrl() { return pdfObjectUrl; }
    public String getPdfToken()     { return pdfToken; }
    
    // ========= OBJETOS DE DOMINIO PARA GUARDAR EN BD =========

// Empleado seleccionado (llenado desde otra pantalla)
private DatEmpleado empleadoSel;

// Ficha ocupacional (tabla CONSULTORIO.FICHA_OCUPACIONAL)
private FichaOcupacional ficha;

// Signos vitales (tabla CONSULTORIO.SIGNOS_VITALES) – por ahora opcional
private SignosVitales signos;

// Consulta médica (tabla CONSULTORIO.CONSULTA_MEDICA)
private ConsultaMedica consulta;

// Lista de diagnósticos de la consulta (tabla CONSULTORIO.CONSULTA_DIAGNOSTICO)
private List<ConsultaDiagnostico> listaDiag;

// Paso actual del wizard (step1, step2, step3, step4)
private String currentStep = "step1";

    
    // ============================
    // CIE10 – Diagnóstico principal
    // ============================
    @EJB
    private Cie10Service cie10Service;

    // campo que se guarda (código)
    private String codCie10Ppal;

    // campo que se muestra / busca (descripción)
    private String descCie10Ppal;
    
 

    @PostConstruct
    public void init() {
        fechaAtencion = new Date();
        tipoEval = "INGRESO";
        sexo = "M";
        grupoSanguineo = "";
        lateralidad = "";

        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale("es"));

        institucion = "Instituto Geográfico Militar";
        institucion = institucion.toUpperCase();
        ruc = "1768007200001";
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

    public void calcularEdad() { this.edad = calcularEdad(this.fechaNacimiento); }

    private Integer calcularEdad(Date f) {
        if (f == null) return null;
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

    // ===========================================================
    // PDF PREVIEW Y DESCARGA (STEP 4)
    // ===========================================================
    public void prepararVistaPrevia() {
        try {
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
     * Render PDF con baseURL apuntando a /resources/ (para CSS/IMG)
     * En la PLANTILLA.html referencia imágenes externas como: <img src="images/LOGO_IGM_FULL_COLOR.png" />
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
    } catch (Throwable ignore) {}

    renderer.setDocumentFromString(xhtml, baseURL);
    renderer.layout();
    renderer.createPDF(baos);
    renderer.finishPDF();
    return baos.toByteArray();
}


    /**
     * Lee /resources/pdf/PLANTILLA.html y reemplaza {{placeholders}}
     * Inyecta además los logos MIDENA/IGM como Data URI.
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
            case "APTO":         aApto = "X"; break;
            case "APTO_EN_OBS":  aObs  = "X"; break;
            case "APTO_LIMIT":   aLim  = "X"; break;
            case "NO_APTO":      aNo   = "X"; break;
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
            case "INGRESO":   chkIngreso   = "X"; break;
            case "PERIODICO":
            case "PERIÓDICO": chkPeriodico = "X"; break;
            case "REINTEGRO": chkReintegro = "X"; break;
            case "RETIRO":    chkRetiro    = "X"; break;
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
    rep.put("institucion",       safe(institucion));
    rep.put("ruc",               safe(ruc));
    rep.put("num_formulario",    safe(noHistoria));
    rep.put("num_archivo",       safe(noArchivo));
    rep.put("centroTrabajo",     safe(centroTrabajo));
    rep.put("ciiu",              safe(ciiu));

    // Paciente
    rep.put("apellido1",         safe(apellido1));
    rep.put("apellido2",         safe(apellido2));
    rep.put("nombre1",           safe(nombre1));
    rep.put("nombre2",           safe(nombre2));
    rep.put("sexo",              safe(sexo));

    // Fecha y evaluación
    rep.put("fecha_yyyy",        yy.format(f));
    rep.put("fecha_MM",          MM.format(f));
    rep.put("fecha_dd",          dd.format(f));
    rep.put("chk_ingreso",       chkIngreso);
    rep.put("chk_periodico",     chkPeriodico);
    rep.put("chk_reintegro",     chkReintegro);
    rep.put("chk_retiro",        chkRetiro);

    // Aptitud
    rep.put("chk_apto",          aApto);
    rep.put("chk_obs",           aObs);
    rep.put("chk_lim",           aLim);
    rep.put("chk_noapto",        aNo);

    // Textos libres
    rep.put("detalleObservaciones", safe(detalleObservaciones));
    rep.put("recomendaciones",      safe(recomendaciones));
    rep.put("medicoNombre",         safe(medicoNombre));
    rep.put("medicoCodigo",         safe(medicoCodigo));

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
        if (s == null) return "";
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
        if (s == null) return "";
        return s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;")
                .replace("\"","&quot;")
                .replace("'","&#39;");
    }

    public void syncTipoEvaluacion() { this.tipoEvaluacion = this.tipoEval; }

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
            while ((r = in.read(buf)) != -1) bos.write(buf, 0, r);
            bytes = bos.toByteArray();
        }
        String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
        String mime = "image/png";
        String lower = pathFromResources.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) mime = "image/jpeg";
        else if (lower.endsWith(".gif")) mime = "image/gif";
        return "data:" + mime + ";base64," + base64;
    }
    
    public List<Cie10> completarCie10(String query) {
    return cie10Service.buscarPorCodigoODescripcion(query);
}

// Suponiendo que tienes algo como:
// private List<DiagnosticoFila> listaDiag;
// donde DiagnosticoFila tiene getCodigo()/setCodigo(), getDescripcion()/setDescripcion()

public void onCie10SelectCodigo(int index) {
    if (listaDiag == null || index < 0 || index >= listaDiag.size()) {
        return;
    }

    var diag = listaDiag.get(index);
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

// ============================
// AUTOCOMPLETE CIE10 – UN SOLO DIAGNÓSTICO
// ============================

// Cuando el usuario selecciona un CÓDIGO desde el autocomplete
public void onCie10CodigoSelect(SelectEvent event) {
    String codigo = (String) event.getObject();
    this.codCie10Ppal = codigo;

    if (codigo != null && !codigo.trim().isEmpty()) {
        Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
        if (cie != null) {
            // si tu campo se llama diagnostico en vez de descripcion, cambia aquí
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




}
