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
import ec.gob.prueba.prueba_maven.modelo.SignosVitales;
import ec.gob.prueba.prueba_maven.servicio.Cie10Service;
import ec.gob.prueba.prueba_maven.servicio.FichaOcupacionalService;
import ec.gob.prueba.prueba_maven.servicio.SignosVitalesService;

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
    private Double peso;   // kg (en el form está en kg)
    private Double talla;  // cm (en el form está en cm)
    private Double imc;    // kg/m2 (calculado en el bean)

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

    // ============================
    // CIE10 – Diagnóstico principal
    // ============================
    @EJB
    private Cie10Service cie10Service;

    @EJB
    private FichaOcupacionalService fichaService;

    @EJB
    private SignosVitalesService signosService;

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

        // ====== INICIALIZAR OBJETOS DE DOMINIO ======
        ficha = new FichaOcupacional();
        signos = new SignosVitales();
        consulta = new ConsultaMedica();
        listaDiag = new ArrayList<>();

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

    // ===========================================================
    // WIZARD: GUARDAR POR STEP
    // ===========================================================
    public void guardarStepActual() {
        try {
            if ("step1".equals(currentStep)) {
                guardarStep1();
                currentStep = "step2";
            } else if ("step2".equals(currentStep)) {
                guardarStep2();
                currentStep = "step3";
            } else if ("step3".equals(currentStep)) {
                guardarStep3();
                currentStep = "step4";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "Ocurrió un error al guardar la información del paso actual."));
        }
    }

    /**
     * STEP 1: - Atención prioritaria - Antecedentes personales -
     * Gineco-obstétricos (Solo llena la FichaOcupacional en memoria, aún no
     * persiste)
     */
    public void guardarStep1() {
        // Aseguramos empleado
        if (empleadoSel != null) {
            ficha.setEmpleado(empleadoSel);
        }

        // FECHA_EVALUACION y TIPO_EVALUACION
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

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Step 1",
                        "Datos de antecedentes y atención prioritaria cargados en la ficha (en memoria)."));
    }

    /**
     * STEP 2: - Riesgos (matriz G) — por ahora sólo placeholder. Cuando definas
     * tabla de riesgos la mapeamos aquí.
     */
    public void guardarStep2() {
        // TODO: Mapear matriz de riesgos a entidad cuando tengas tabla en BD.
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Step 2",
                        "Matriz de riesgos procesada (por ahora sólo en memoria)."));
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
        ficha.setAptitudSel(aptitudSel);                 // APTO / APTO_EN_OBS / APTO_LIMIT / NO_APTO
        ficha.setDetalleObs(detalleObservaciones);       // DETALLE_OBS
        ficha.setRecomendaciones(recomendaciones);       // RECOMENDACIONES

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

        // Estado inicial de la ficha
        if (ficha.getEstado() == null) {
            ficha.setEstado("EMITIDA");
        }

        // Auditoría mínima
        if (ficha.getFechaCreacion() == null) {
            ficha.setFechaCreacion(new Date());
            ficha.setUsrCreacion("USR_APP"); // reemplaza por usuario real
        } else {
            ficha.setFechaActualizacion(new Date());
            ficha.setUsrActualizacion("USR_APP");
        }

        // === Persistir en BD ===
        ficha = fichaService.guardar(ficha);

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Step 3",
                        "Ficha ocupacional guardada correctamente en la base de datos."));
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

}
