package ec.gob.prueba.prueba_maven.modelo;

import ec.gob.prueba.prueba_maven.modelo.converter.GrupoSangreConverter;
import ec.gob.prueba.prueba_maven.modelo.converter.EstadoCivilConverterJPA;
import ec.gob.prueba.prueba_maven.modelo.converter.SexoConverterJPA;

import ec.gob.prueba.prueba_maven.modelo.enums.EstadoCivil;
import ec.gob.prueba.prueba_maven.modelo.enums.GrupoSangre;
import ec.gob.prueba.prueba_maven.modelo.enums.Sexo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;

import lombok.*;

/**
 * Entidad RH.T_DAT_EMPLEADO
 * Mapea los datos personales del empleado,
 * incluyendo códigos compactos (sexo, estado civil, grupo sanguíneo)
 * mediante converters JPA personalizados.
 */
@Entity
@Access(AccessType.FIELD) // fuerza acceso por campos (soluciona warning de tipo de acceso)
@Table(
    name = "T_DAT_EMPLEADO",
    schema = "RH",
    uniqueConstraints = @UniqueConstraint(name = "T_DAT_EMPLEADO_U01", columnNames = "NO_CEDULA")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DatEmpleado implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== Clave primaria =====
    @Id
    @SequenceGenerator(
        name = "EMP_GEN",
        sequenceName = "RH.SQ_T_DAT_EMPLEADO",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EMP_GEN")
    @Column(name = "NO_PERSONA", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer noPersona;

    // ===== Campos básicos =====
    @Column(name = "NO_RELIG")
    private Integer noRelig;

    @Column(name = "NO_SEG")
    private Integer noSeg;

    @Column(name = "NO_PROVEEDOR")
    private Long noProveedor;

    @Column(name = "CODIGO", length = 10)
    private String codigo;

    @Column(name = "NO_CEDULA", length = 10, nullable = false)
    private String noCedula;

    @Column(name = "PRI_APELLIDO", length = 20)
    private String priApellido;

    @Column(name = "SEG_APELLIDO", length = 20)
    private String segApellido;

    @Column(name = "NOMBRES", length = 40)
    private String nombres;

    @Column(name = "NOMBRE_C", length = 81)
    private String nombreC;

    @Column(name = "LIB_MILITAR", length = 15)
    private String libMilitar;

    @Column(name = "SEGURO_SOCIAL", length = 15)
    private String seguroSocial;

    // === Converters personalizados ===
    @Convert(converter = SexoConverterJPA.class)
    @Column(name = "SEXO", length = 1)
    private Sexo sexo; // M/F

    @Convert(converter = EstadoCivilConverterJPA.class)
    @Column(name = "EST_CIVIL", length = 1)
    private EstadoCivil estCivil; // S,C,D,V,U

    @Convert(converter = GrupoSangreConverter.class)
    @Column(name = "G_SANGRE", length = 3)
    private GrupoSangre grupoSangre; // O+, A-, etc.

    @Temporal(TemporalType.DATE)
    @Column(name = "F_NACIMIENTO")
    private Date fNacimiento;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_MUERTE")
    private Date fMuerte;

    @Column(name = "NO_LICENCIA")
    private Integer noLicencia;

    @Column(name = "NO_PROFESION")
    private Integer noProfesion;

    @Column(name = "DIRECCION", length = 200)
    private String direccion;

    @Column(name = "TELEFONO", length = 16)
    private String telefono;

    @Column(name = "TIPO", length = 1)
    private String tipo; // E,C,J,M,F,T

    @Column(name = "FOTO", length = 50)
    private String foto;

    @Column(name = "COLOR_PIEL", length = 1)
    private String colorPiel; // B,N,T,A,O

    @Column(name = "COLOR_CABELLO", length = 1)
    private String colorCabello; // N,C,R,P,A,O

    @Column(name = "COLOR_OJOS", length = 1)
    private String colorOjos; // C,N,V,P,O,A

    @Column(name = "ESTATURA", precision = 3, scale = 2)
    private BigDecimal estatura;

    @Column(name = "PESO", precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(name = "TALLA_CAMISA", precision = 5, scale = 2)
    private BigDecimal tallaCamisa;

    @Column(name = "TALLA_PANTALON", precision = 5, scale = 2)
    private BigDecimal tallaPantalon;

    @Column(name = "NO_CALZADO")
    private Integer noCalzado;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_INGRESO")
    private Date fIngreso;

    @Column(name = "TELEFONO2", length = 16)
    private String telefono2;

    @Column(name = "L_VIV_PROPIA")
    private Integer lVivPropia;

    @Column(name = "ALERGIA", length = 100)
    private String alergia;

    @Column(name = "NO_LOC_NACE")
    private Integer noLocNace;

    @Column(name = "NO_LOC_DIR")
    private Integer noLocDir;

    @Column(name = "NO_CABEZA", precision = 5, scale = 2)
    private BigDecimal noCabeza;

    @Column(name = "L_USA_LENTES")
    private Integer lUsaLentes;

    @Column(name = "COMISARIATO", length = 1)
    private String comisariato;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_REINGRESO")
    private Date fReingreso;

    @Column(name = "ALIAS_BASE_DATOS", length = 30)
    private String aliasBaseDatos;

    @Column(name = "PIE_FIRMA", length = 50)
    private String pieFirma;

    @Column(name = "NO_DIREC", length = 10)
    private String noDirec;

    @Column(name = "NIVEL")
    private Integer nivel;

    @Column(name = "CARGO_LOSSCA", length = 50)
    private String cargoLossca;

    @Column(name = "PATRONAL", precision = 14, scale = 2)
    private BigDecimal patronal;

    @Column(name = "PERSONAL", precision = 14, scale = 2)
    private BigDecimal personal;

    @Column(name = "IECE", precision = 14, scale = 2)
    private BigDecimal iece;

    @Column(name = "PROCESO", length = 100)
    private String proceso;

    @Column(name = "SUBPROCESO", length = 100)
    private String subproceso;

    @Column(name = "PARTIDA_PRESUPUESTARIA", length = 60)
    private String partidaPresupuestaria;

    @Column(name = "GESTION", length = 100)
    private String gestion;

    @Column(name = "UNIDAD", length = 100)
    private String unidad;

    @Column(name = "EMAIL", length = 60)
    private String email;

    @Column(name = "L_DISCAPACIDAD")
    private Integer lDiscapacidad;

    @Column(name = "NO_CONADIS", length = 10)
    private String noConadis;

    @Column(name = "ID_NACIONALIDAD")
    private Integer idNacionalidad;

    @Column(name = "AUTOIDENTIFICACION_ETNICA", length = 35)
    private String autoidentificacionEtnica;

    @Column(name = "NACIONALIDAD_INDIGENA", length = 10)
    private String nacionalidadIndigena;

    @Column(name = "L_CATASTROFICA")
    private Integer lCatastrofica;

    @Column(name = "NO_CONADIS_CATASTROFICA", length = 10)
    private String noConadisCatastrofica;

    @Column(name = "CALLE_SECUNDARIA", length = 100)
    private String calleSecundaria;

    @Column(name = "REFERENCIA", length = 200)
    private String referencia;

    @Column(name = "EXTENSION", length = 10)
    private String extension;

    @Column(name = "CONTACTO_APELLIDOS", length = 50)
    private String contactoApellidos;

    @Column(name = "CONTACTO_NOMBRES", length = 50)
    private String contactoNombres;

    @Column(name = "CONTACTO_TELEFONO", length = 20)
    private String contactoTelefono;

    @Column(name = "CONTACTO_CELULAR", length = 10)
    private String contactoCelular;

    @Column(name = "EMAIL_INSTITUCIONAL", length = 60)
    private String emailInstitucional;

    @Column(name = "ESTADO", length = 10)
    private String estado;

    // ===== Campo derivado (no persistente) =====
    public String getApellidos() {
        String a1 = priApellido == null ? "" : priApellido;
        String a2 = segApellido == null ? "" : segApellido;
        return (a1 + " " + a2).trim();
    }

    // ===== Normalización previa a persistencia =====
    @PrePersist
    @PreUpdate
    public void normalizar() {
        if (noCedula != null) noCedula = noCedula.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (emailInstitucional != null) emailInstitucional = emailInstitucional.trim().toLowerCase();
    }
}
