package ec.gob.prueba.prueba_maven.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.primefaces.model.StreamedContent;

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

    // Fecha nacimiento / Edad
    private Date fechaNacimiento;
    private Date fechaAtencion;
    private String tipoEval;
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

    private Date fechaEmision;           // Step 3
    private String tipoEvaluacion;         // Step 3: INGRESO / PERIODICO / REINTEGRO / RETIRO

    // Aptitud (Step 3)
    private boolean apto;
    private boolean aptoObservacion;
    private boolean aptoLimitaciones;
    private boolean noApto;

    private String detalleObservaciones;   // Step 3
    private String recomendaciones;        // Step 3

    private String medicoNombre;           // Step 3
    private String medicoCodigo;           // Step 3

    // ====== Vista previa / descarga ======
    private StreamedContent pdfPreview;
    private StreamedContent pdfDescarga;
    private boolean certificadoListo;
    
    // === PREVIEW POR <object> ===
private String pdfObjectUrl;        // URL pública para el <object>
public String getPdfObjectUrl() {   // getter requerido por la vista
    return pdfObjectUrl;
    
}
private String pdfToken; // getter/setter ya los genera lombok

public String getPdfToken() {
    return pdfToken;
}

    @PostConstruct
    public void init() {
        fechaAtencion = new Date();
        tipoEval = "INGRESO";
        sexo = "M";
        grupoSanguineo = "";
        lateralidad = "";
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale("es"));
    }

    // ===========================================================
    // MÉTODOS DE FECHAS Y EDAD
    // ===========================================================
    public void onFechaNacimientoSelect(org.primefaces.event.SelectEvent e) {
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

    public void setFechaNacimiento(Date f) {  // por si cambia vía binding
        this.fechaNacimiento = f;
        this.edad = calcularEdad(f);
    }

    // Añade esto dentro de CentroMedicoCtrl
    public void calcularEdad() {
        // JSF ya habrá seteado fechaNacimiento; sólo recalculamos
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
    // ===========================================================
    // PDF PREVIEW Y DESCARGA (STEP 4)
    // ===========================================================
public void prepararVistaPrevia() {
    try {
        String html = construirHtmlDesdePlantilla();
        byte[] bytes = renderizarPdf(html);

        // 1) Generar token único por vista/ejecución
        this.pdfToken = "CERT_" + System.currentTimeMillis();

        // 2) Guardar bytes en sesión
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .put(pdfToken, bytes);

        // (opcional) si además quieres escribir archivo físico, puedes seguir usando tu bloque pdfObjectUrl
        this.pdfObjectUrl = null; // lo desactivamos y usamos el servlet como fuente principal

        certificadoListo = true;

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "PDF listo",
                    "Se generó el certificado para vista previa y descarga."));
    } catch (Exception e) {
        certificadoListo = false;
        pdfToken = null;
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .remove(pdfToken);
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
     * Renderizado PDF con baseURL apuntando a /resources/ (para CSS/IMG)
     */
    private byte[] renderizarPdf(String xhtml) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        org.xhtmlrenderer.pdf.ITextRenderer renderer = new org.xhtmlrenderer.pdf.ITextRenderer();

        // baseURL = /resources/ del WAR (resuelve href="pdf.css" y src="img/logo.png")
        String basePath = javax.faces.context.FacesContext.getCurrentInstance()
                .getExternalContext().getRealPath("/resources/");
        String baseURL = new java.io.File(basePath).toURI().toURL().toString();

        // (Opcional) registrar fuente para tildes/Unicode
        try {
            renderer.getFontResolver().addFont(
                    basePath + "fonts/DejaVuSans.ttf",
                    com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                    true
            );
        } catch (Throwable ignore) {
            // si no pones la fuente, no se cae
        }

        renderer.setDocumentFromString(xhtml, baseURL);
        renderer.layout();
        renderer.createPDF(baos);
        renderer.finishPDF();
        return baos.toByteArray();
    }

    /**
     * Carga plantilla /resources/pdf/PLANTILLA.html y reemplaza
     * {{placeholders}}
     */
    private String construirHtmlDesdePlantilla() throws Exception {
        String template = cargarRecursoComoString("PLANTILLA.html");
        template = normalizarXhtml(template);
        java.util.Date f = (fechaEmision != null) ? fechaEmision : new java.util.Date();
        java.text.SimpleDateFormat yy = new java.text.SimpleDateFormat("yyyy");
        java.text.SimpleDateFormat MM = new java.text.SimpleDateFormat("MM");
        java.text.SimpleDateFormat dd = new java.text.SimpleDateFormat("dd");

        String checkApto = apto ? "X" : "&nbsp;";
        String checkObs = aptoObservacion ? "X" : "&nbsp;";
        String checkLim = aptoLimitaciones ? "X" : "&nbsp;";
        String checkNo = noApto ? "X" : "&nbsp;";

        java.util.Map replacements = new java.util.LinkedHashMap();
        // A. Identificación
        replacements.put("institucion", safe(institucion));
        replacements.put("ruc", safe(ruc));
        replacements.put("num_formulario", safe(noHistoria));
        replacements.put("num_archivo", safe(noArchivo));

        // Paciente
        replacements.put("apellido1", safe(apellido1));
        replacements.put("apellido2", safe(apellido2));
        replacements.put("nombre1", safe(nombre1));
        replacements.put("nombre2", safe(nombre2));
        replacements.put("sexo", safe(sexo));

        // Tipo evaluación / fecha
        replacements.put("tipo_eval", safe(tipoEvaluacion));
        replacements.put("fecha_yyyy", yy.format(f));
        replacements.put("fecha_MM", MM.format(f));
        replacements.put("fecha_dd", dd.format(f));

        // Aptitud
        replacements.put("chk_apto", checkApto);
        replacements.put("chk_obs", checkObs);
        replacements.put("chk_lim", checkLim);
        replacements.put("chk_noapto", checkNo);

        // Observaciones / recomendaciones / médico
        replacements.put("obs_detalle", safe(detalleObservaciones));
        replacements.put("recomendaciones", safe(recomendaciones));
        replacements.put("medico_nombre", safe(medicoNombre));
        replacements.put("medico_codigo", safe(medicoCodigo));

        // Reemplazo simple {{clave}} -> valor
        java.util.Iterator it = replacements.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry e = (java.util.Map.Entry) it.next();
            String key = (String) e.getKey();
            String val = (String) e.getValue();
            template = template.replace("{{" + key + "}}", (val == null ? "" : val));
        }
        return template;
    }

    /**
     * Lee un recurso del classpath a String (JSF/GlassFish friendly)
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
        // meta charset -> meta http-equiv XHTML
        s = s.replaceAll("(?i)<meta\\s+charset\\s*=\\s*\"?utf-8\"?\\s*>",
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        // <br> -> <br />
        s = s.replaceAll("(?i)<br(\\s*)>", "<br />");
        // <hr> -> <hr />
        s = s.replaceAll("(?i)<hr(\\s*)>", "<hr />");
        // <img ...> -> <img ... />
        s = s.replaceAll("(?i)<img([^>]*?)(?<!/)>", "<img$1 />");
        return s;
    }

    /**
     * Escape básico para evitar romper XHTML al inyectar texto
     */
    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
