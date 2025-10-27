package ec.gob.prueba.prueba_maven.modelo;

import ec.gob.prueba.prueba_maven.modelo.converter.EstadoContratoConverter;
import ec.gob.prueba.prueba_maven.modelo.converter.TipoContratoConverter;
import ec.gob.prueba.prueba_maven.modelo.enums.EstadoContrato;
import ec.gob.prueba.prueba_maven.modelo.enums.TipoContrato;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@NamedQueries({
    @NamedQuery(
            name = "Contratacion.porPersonaTodos",
            query = "SELECT c FROM Contratacion c "
            + "WHERE c.id.noPersona = :noPersona "
            + "ORDER BY c.id.noCont DESC"
    )
})

@Entity
@Table(name = "T_CONTRATACIONES", schema = "RH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contratacion implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==========================
    // Clave primaria compuesta
    // ==========================
    @EmbeddedId
    private ContratacionId id;

    // ==========================
    // Relaciones (FK)
    // ==========================
    /**
     * NO_PERSONA -> RH.T_DAT_EMPLEADO(NO_PERSONA)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_PERSONA", referencedColumnName = "NO_PERSONA",
            insertable = false, updatable = false)
    private DatEmpleado empleado;

//    /** NO_FUNCION -> RH.T_FUNCION(NO_FUNCION) */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "NO_FUNCION", referencedColumnName = "NO_FUNCION",
//            insertable = false, updatable = false)
//    private Funcion funcion;
//    /** (NO_CD, NO_EMP_MATRIZ) -> RH.T_AREAS(NO_CD, NO_EMP_MATRIZ) */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumns({
//            @JoinColumn(name = "NO_CD", referencedColumnName = "NO_CD", insertable = false, updatable = false),
//            @JoinColumn(name = "NO_EMP_MATRIZ", referencedColumnName = "NO_EMP_MATRIZ", insertable = false, updatable = false)
//    })
//    private Area area;
    // ==========================
    // Enums con Converter JPA
    // ==========================
    /**
     * C_CONTRATO NUMBER(3,0) -> enum TipoContrato (usa tu
     * TipoContratoConverter)
     */
    @Convert(converter = TipoContratoConverter.class)
    @Column(name = "C_CONTRATO", precision = 3, scale = 0)
    private TipoContrato tipoContrato;

    /**
     * ESTADO ('V','C','P','S') -> enum EstadoContrato (usa tu
     * EstadoContratoConverter)
     */
    @Convert(converter = EstadoContratoConverter.class)
    @Column(name = "ESTADO", length = 1)
    private EstadoContrato estado;

    // ==========================
    // Columnas escalares
    // ==========================
    @Column(name = "NO_DOC", length = 10)
    private String noDoc;

    @Column(name = "RESPONSABLE", length = 50)
    private String responsable;

    @Column(name = "NIVEL", length = 2)
    private String nivel;

    @Column(name = "CATEGORIA", length = 2)
    private String categoria;

    /**
     * espejo del FK para escritura; la relación está en solo lectura
     */
    @Column(name = "NO_FUNCION", precision = 3, scale = 0)
    private BigDecimal noFuncion;

    @Column(name = "NO_EMP", precision = 3, scale = 0)
    private BigDecimal noEmp;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_CONTRATO")
    private Date feContrato;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_SALIDA")
    private Date feSalida;

    @Column(name = "T_SALIDA", length = 1)
    private String tSalida; // si luego lo mapeas a enum, hacemos otro converter

    @Column(name = "C_MONEDA", precision = 3, scale = 0)
    private BigDecimal cMoneda;

    @Column(name = "V_SUELDO_IMPONIBLE", precision = 15, scale = 2)
    private BigDecimal vSueldoImponible;

    @Column(name = "COSTO_HORA", precision = 15, scale = 2)
    private BigDecimal costoHora;

    @Column(name = "COSTO_HORA_EXTRA", precision = 15, scale = 2)
    private BigDecimal costoHoraExtra;

    @Column(name = "NO_BANCO", precision = 3, scale = 0)
    private BigDecimal noBanco;

    @Column(name = "NO_CTA", length = 15)
    private String noCta;

    /**
     * flags 0/1; si prefieres Boolean te paso converter 0/1 ↔ Boolean
     */
    @Column(name = "L_LICENCIA", precision = 1, scale = 0)
    private BigDecimal lLicencia;

    @Column(name = "DIAS_LICENCIA", precision = 3, scale = 0)
    private BigDecimal diasLicencia;

    @Column(name = "DIAS_UTILIZADOS", precision = 3, scale = 0)
    private BigDecimal diasUtilizados;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "HORA_INGRESO")
    private Date horaIngreso;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "HORA_SALIDA")
    private Date horaSalida;

    /**
     * flag 0/1; ver comentario de Boolean arriba
     */
    @Column(name = "L_GENERA_ROL", precision = 1, scale = 0)
    private BigDecimal lGeneraRol;

    @Column(name = "OBS", length = 250)
    private String obs;

    @Column(name = "NO_EMP_PRE", precision = 3, scale = 0)
    private BigDecimal noEmpPre;

    @Column(name = "NO_CD_PRE", precision = 3, scale = 0)
    private BigDecimal noCdPre;

    @Column(name = "NO_CARGO", precision = 3, scale = 0)
    private BigDecimal noCargo;

    @Column(name = "V_SUELDO_IMPONIBLE_SUC", precision = 15, scale = 2)
    private BigDecimal vSueldoImponibleSuc;

    @Column(name = "COSTO_HORA_SUC", precision = 15, scale = 2)
    private BigDecimal costoHoraSuc;

    @Column(name = "COSTO_HORA_EXTRA_SUC", precision = 15, scale = 2)
    private BigDecimal costoHoraExtraSuc;

    /**
     * espejos FK compuesta hacia Area para escritura
     */
    @Column(name = "NO_EMP_MATRIZ", precision = 3, scale = 0)
    private BigDecimal noEmpMatriz;

    @Column(name = "NO_CD", precision = 4, scale = 0)
    private BigDecimal noCd;

    @Column(name = "MANO_OBRA", length = 1)
    private String manoObra;

    @Column(name = "PROYECTO_CODIGO", precision = 3, scale = 0)
    private BigDecimal proyectoCodigo;

    @Column(name = "TIPO_VIATICO", length = 1)
    private String tipoViatico;

    @Column(name = "NO_CD_PRE_ANT", precision = 3, scale = 0)
    private BigDecimal noCdPreAnt;

    @Column(name = "NIVEL_APROBACION", precision = 2, scale = 0)
    private BigDecimal nivelAprobacion;

    @Column(name = "RMU", precision = 15, scale = 2)
    private BigDecimal rmu;

    // ==========================
    // Helpers PK (opcionales)
    // ==========================
    public BigDecimal getNoPersona() {
        return (id != null) ? id.getNoPersona() : null;
    }

    public BigDecimal getNoCont() {
        return (id != null) ? id.getNoCont() : null;
    }
}
