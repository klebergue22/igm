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
@Table(name = "FICHA_OCUPACIONAL", schema = "CONSULTORIO")
@Access(AccessType.FIELD) // fuerza FIELD access
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"empleado", "signos", "cie10Principal", "personaAux"})
public class FichaOcupacional implements Serializable {

    private static final long serialVersionUID = 1L;

    // =====================================================
    // PK
    // =====================================================
    @Id
    @SequenceGenerator(
            name = "FICHA_OCUP_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FICHA_OCUP_GEN")
    @Column(name = "ID_FICHA", nullable = false)
    private Long idFicha;

    // =====================================================
    // Relaciones
    // =====================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_PERSONA")
    private DatEmpleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA_AUX")
    private PersonaAux personaAux;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SIGNOS")
    private SignosVitales signos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_CIE10_PPAL", referencedColumnName = "CODIGO")
    private Cie10 cie10Principal;

    // =====================================================
    // Campos generales
    // =====================================================
    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EVALUACION", nullable = false)
    private Date fechaEvaluacion;

    @Column(name = "TIPO_EVALUACION", length = 20, nullable = false)
    private String tipoEvaluacion;

    // Atención prioritaria (S/N)
    @Column(name = "AP_EMBARAZADA", length = 1)
    private String apEmbarazada;

    @Column(name = "AP_DISCAPACIDAD", length = 1)
    private String apDiscapacidad;

    @Column(name = "AP_CATASTROFICA", length = 1)
    private String apCatastrofica;

    @Column(name = "AP_LACTANCIA", length = 1)
    private String apLactancia;

    @Column(name = "AP_ADULTO_MAYOR", length = 1)
    private String apAdultoMayor;

    // Antecedentes
    @Column(name = "ANT_CLINICO_QUIR", length = 2000)
    private String antClinicoQuir;

    @Column(name = "ANT_FAMILIARES", length = 2000)
    private String antFamiliares;

    @Column(name = "CONDICION_ESPECIAL", length = 500)
    private String condicionEspecial;

    @Column(name = "AUTORIZA_TRANSFUSION", length = 2)
    private String autorizaTransfusion; // SI/NO

    @Column(name = "TRAT_HORMONAL", length = 2)
    private String tratHormonal; // SI/NO

    @Column(name = "TRAT_HORMONAL_CUAL", length = 500)
    private String tratHormonalCual;

    @Column(name = "EXAM_REPRO_MASC", length = 500)
    private String examReproMasc;

    @Column(name = "TIEMPO_REPRO_MASC")
    private Integer tiempoReproMasc;

    // Gineco-obstétricos
    @Temporal(TemporalType.DATE)
    @Column(name = "FUM")
    private Date fum;

    @Column(name = "GESTAS")
    private Integer gestas;

    @Column(name = "PARTOS")
    private Integer partos;

    @Column(name = "CESAREAS")
    private Integer cesareas;

    @Column(name = "ABORTOS")
    private Integer abortos;

    @Column(name = "PLANIFICACION", length = 50)
    private String planificacion;

    @Column(name = "PLANIFICACION_CUAL", length = 200)
    private String planificacionCual;

    // =====================================================
    // Consumo / Vida / Condiciones
    // =====================================================
    // TABACO
    @Column(name = "TAB_CONS_MESES")
    private Integer tabConsMeses;

    @Column(name = "TAB_EX_CONS", length = 1)
    private String tabExCons; // S/N

    @Column(name = "TAB_ABS_MESES")
    private Integer tabAbsMeses;

    @Column(name = "TAB_NO_CONS", length = 1)
    private String tabNoCons; // S/N

    // ALCOHOL
    @Column(name = "ALC_CONS_MESES")
    private Integer alcConsMeses;

    @Column(name = "ALC_EX_CONS", length = 1)
    private String alcExCons; // S/N

    @Column(name = "ALC_ABS_MESES")
    private Integer alcAbsMeses;

    @Column(name = "ALC_NO_CONS", length = 1)
    private String alcNoCons; // S/N

    // OTRAS
    @Column(name = "OTR_CUAL", length = 500)
    private String otrCual;

    @Column(name = "OTR_CONS_MESES")
    private Integer otrConsMeses;

    @Column(name = "OTR_EX_CONS", length = 1)
    private String otrExCons; // S/N

    @Column(name = "OTR_ABS_MESES")
    private Integer otrAbsMeses;

    @Column(name = "OTR_NO_CONS", length = 1)
    private String otrNoCons; // S/N

    // ACTIVIDAD FISICA (3)
    @Column(name = "AF_CUAL_1", length = 500)
    private String afCual1;

    @Column(name = "AF_TIEMPO_1", length = 200)
    private String afTiempo1;

    @Column(name = "AF_CUAL_2", length = 500)
    private String afCual2;

    @Column(name = "AF_TIEMPO_2", length = 200)
    private String afTiempo2;

    @Column(name = "AF_CUAL_3", length = 500)
    private String afCual3;

    @Column(name = "AF_TIEMPO_3", length = 200)
    private String afTiempo3;

    // MEDICACION (3)
    @Column(name = "MED_CUAL_1", length = 500)
    private String medCual1;

    @Column(name = "MED_CANT_1")
    private Integer medCant1;

    @Column(name = "MED_CUAL_2", length = 500)
    private String medCual2;

    @Column(name = "MED_CANT_2")
    private Integer medCant2;

    @Column(name = "MED_CUAL_3", length = 500)
    private String medCual3;

    @Column(name = "MED_CANT_3")
    private Integer medCant3;

    @Column(name = "OBS_CONSUMO_VIDA_COND", length = 2000)
    private String obsConsumoVidaCond;

    // =====================================================
    // Aptitud / Emisión
    // =====================================================
    @Column(name = "APTITUD_SEL", length = 20)
    private String aptitudSel;

    @Column(name = "DETALLE_OBS", length = 2000)
    private String detalleObs;

    @Column(name = "RECOMENDACIONES", length = 2000)
    private String recomendaciones;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EMISION")
    private Date fechaEmision;

    @Column(name = "MEDICO_NOMBRE", length = 150)
    private String medicoNombre;

    @Column(name = "MEDICO_CODIGO", length = 50)
    private String medicoCodigo;

    @Column(name = "ESTADO", length = 20)
    private String estado; // BORRADOR, EMITIDA, ANULADA, etc.

    // =====================================================
    // Auditoría  ✅ (AQUÍ estaba tu error)
    // =====================================================
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fechaActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    // =====================================================
    // Step 1
    // =====================================================
    @Column(name = "INST_SISTEMA", length = 200)
    private String instSistema;

    @Column(name = "RUC_ESTABLECIMIENTO", length = 20)
    private String rucEstablecimiento;

    @Column(name = "CIIU", length = 20)
    private String ciiu;

    @Column(name = "ESTABLECIMIENTO_CT", length = 250)
    private String establecimientoCt;

    @Column(name = "NO_HISTORIA_CLINICA", length = 30)
    private String noHistoriaClinica;

    @Column(name = "NO_ARCHIVO", length = 30)
    private String noArchivo;

    @Column(name = "AREA_TRABAJO", length = 200)
    private String areaTrabajo;

    @Column(name = "PUESTO_TRABAJO_TXT", length = 200)
    private String puestoTrabajoTxt;

    // =====================================================
    // Step 3
    // =====================================================
    @Column(name = "EXTRA_LAB_DESC", length = 2000)
    private String extraLabDesc;

    @Column(name = "ENFERMEDAD_PROB_ACTUAL", length = 2000)
    private String enfermedadProbActual;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXTRA_LAB_FECHA")
    private Date extraLabFecha;

    // =====================================================
    // EXAMEN FISICO (persistencia S/N) - ya en BD
    // =====================================================
    @Column(name = "EXF_PIEL_CICATRICES", length = 1)
    private String exfPielCicatrices; // S/N

    @Column(name = "EXF_OJOS_PARPADOS", length = 1)
    private String exfOjosParpados;

    @Column(name = "EXF_OJOS_CONJUNTIVAS", length = 1)
    private String exfOjosConjuntivas;

    @Column(name = "EXF_OJOS_PUPILAS", length = 1)
    private String exfOjosPupilas;

    @Column(name = "EXF_OJOS_CORNEA", length = 1)
    private String exfOjosCornea;

    @Column(name = "EXF_OJOS_MOTILIDAD", length = 1)
    private String exfOjosMotilidad;

    @Column(name = "EXF_OIDO_CONDUCTO", length = 1)
    private String exfOidoConducto;

    @Column(name = "EXF_OIDO_PABELLON", length = 1)
    private String exfOidoPabellon;

    @Column(name = "EXF_OIDO_TIMPANOS", length = 1)
    private String exfOidoTimpanos;

    @Column(name = "OBS_EXAMEN_FISICO_REG", length = 2000)
    private String obsExamenFisicoReg;

    // =====================================================
    // N. RETIRO (evaluación) (S/N)
    // =====================================================
    @Column(name = "N_RET_EVAL", length = 1)
    private String nRetEval; // S/N

    @Column(name = "N_RET_REL_TRAB", length = 1)
    private String nRetRelTrab; // S/N

    @Column(name = "N_RET_OBS", length = 2000)
    private String nRetObs;

    // =====================================================
    // BOOLs SOLO UI (no están en BD todavía)
    // =====================================================
    @Transient private Boolean exfNarizTabiqueBool;
    @Transient private Boolean exfNarizCornetesBool;
    @Transient private Boolean exfNarizMucosasBool;
    @Transient private Boolean exfNarizSenosBool;

    @Transient private Boolean exfOroLabiosBool;
    @Transient private Boolean exfOroLenguaBool;
    @Transient private Boolean exfOroDentaduraBool;
    @Transient private Boolean exfOroAmigdalasBool;
    @Transient private Boolean exfOroFaringeBool;

    @Transient private Boolean exfCuelloMovilidadBool;
    @Transient private Boolean exfCuelloTiroidesBool;

    @Transient private Boolean exfToraxMamasBool;
    @Transient private Boolean exfToraxParrillaBool;
    @Transient private Boolean exfToraxPulmonesBool;
    @Transient private Boolean exfToraxCorazonBool;

    @Transient private Boolean exfAbdomenParedBool;
    @Transient private Boolean exfAbdomenViscerasBool;

    @Transient private Boolean exfPelvisPelvisBool;
    @Transient private Boolean exfPelvisGenitalesBool;

    @Transient private Boolean exfExtSupBool;
    @Transient private Boolean exfExtInfBool;
    @Transient private Boolean exfExtVascularBool;

    @Transient private Boolean exfColumnaDesviacionBool;
    @Transient private Boolean exfColumnaFlexibilidadBool;
    @Transient private Boolean exfColumnaDolorBool;

    @Transient private Boolean exfNeuroFuerzaBool;
    @Transient private Boolean exfNeuroSensibilidadBool;
    @Transient private Boolean exfNeuroReflejosBool;
    @Transient private Boolean exfNeuroMarchaBool;

    // =====================================================
    // Conversión S/N <-> Boolean
    // =====================================================
    private static boolean snToBool(String v) { return "S".equalsIgnoreCase(v); }
    private static String boolToSn(Boolean b) { return (b != null && b) ? "S" : "N"; }

    // =====================================================
    // PROPIEDADES ...Bool para campos S/N persistentes
    // =====================================================
    public Boolean getExfPielCicatricesBool() { return snToBool(exfPielCicatrices); }
    public void setExfPielCicatricesBool(Boolean v) { this.exfPielCicatrices = boolToSn(v); }

    public Boolean getExfOjosParpadosBool() { return snToBool(exfOjosParpados); }
    public void setExfOjosParpadosBool(Boolean v) { this.exfOjosParpados = boolToSn(v); }

    public Boolean getExfOjosConjuntivasBool() { return snToBool(exfOjosConjuntivas); }
    public void setExfOjosConjuntivasBool(Boolean v) { this.exfOjosConjuntivas = boolToSn(v); }

    public Boolean getExfOjosPupilasBool() { return snToBool(exfOjosPupilas); }
    public void setExfOjosPupilasBool(Boolean v) { this.exfOjosPupilas = boolToSn(v); }

    public Boolean getExfOjosCorneaBool() { return snToBool(exfOjosCornea); }
    public void setExfOjosCorneaBool(Boolean v) { this.exfOjosCornea = boolToSn(v); }

    public Boolean getExfOjosMotilidadBool() { return snToBool(exfOjosMotilidad); }
    public void setExfOjosMotilidadBool(Boolean v) { this.exfOjosMotilidad = boolToSn(v); }

    public Boolean getExfOidoConductoBool() { return snToBool(exfOidoConducto); }
    public void setExfOidoConductoBool(Boolean v) { this.exfOidoConducto = boolToSn(v); }

    public Boolean getExfOidoPabellonBool() { return snToBool(exfOidoPabellon); }
    public void setExfOidoPabellonBool(Boolean v) { this.exfOidoPabellon = boolToSn(v); }

    public Boolean getExfOidoTimpanosBool() { return snToBool(exfOidoTimpanos); }
    public void setExfOidoTimpanosBool(Boolean v) { this.exfOidoTimpanos = boolToSn(v); }

    public Boolean getNRetEvalBool() { return snToBool(nRetEval); }
    public void setNRetEvalBool(Boolean v) { this.nRetEval = boolToSn(v); }

    public Boolean getNRetRelTrabBool() { return snToBool(nRetRelTrab); }
    public void setNRetRelTrabBool(Boolean v) { this.nRetRelTrab = boolToSn(v); }

    // =====================================================
    // Auditoría / defaults
    // =====================================================
    @PrePersist
    public void prePersist() {
        Date ahora = new Date();

        if (fechaCreacion == null) fechaCreacion = ahora;
        if (estado == null || estado.trim().isEmpty()) estado = "BORRADOR";

        if (apEmbarazada == null) apEmbarazada = "N";
        if (apDiscapacidad == null) apDiscapacidad = "N";
        if (apCatastrofica == null) apCatastrofica = "N";
        if (apLactancia == null) apLactancia = "N";
        if (apAdultoMayor == null) apAdultoMayor = "N";

        if (tabExCons == null) tabExCons = "N";
        if (tabNoCons == null) tabNoCons = "N";
        if (alcExCons == null) alcExCons = "N";
        if (alcNoCons == null) alcNoCons = "N";
        if (otrExCons == null) otrExCons = "N";
        if (otrNoCons == null) otrNoCons = "N";

        if (exfPielCicatrices == null) exfPielCicatrices = "N";
        if (exfOjosParpados == null) exfOjosParpados = "N";
        if (exfOjosConjuntivas == null) exfOjosConjuntivas = "N";
        if (exfOjosPupilas == null) exfOjosPupilas = "N";
        if (exfOjosCornea == null) exfOjosCornea = "N";
        if (exfOjosMotilidad == null) exfOjosMotilidad = "N";
        if (exfOidoConducto == null) exfOidoConducto = "N";
        if (exfOidoPabellon == null) exfOidoPabellon = "N";
        if (exfOidoTimpanos == null) exfOidoTimpanos = "N";

        if (nRetEval == null) nRetEval = "N";
        if (nRetRelTrab == null) nRetRelTrab = "N";

        if (!"BORRADOR".equalsIgnoreCase(estado)) {
            if (fechaEmision == null) fechaEmision = ahora;
            if (aptitudSel == null || aptitudSel.trim().isEmpty()) aptitudSel = "APTO";
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
    }
}
