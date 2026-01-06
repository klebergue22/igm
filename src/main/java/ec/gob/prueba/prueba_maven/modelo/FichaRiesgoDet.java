package ec.gob.prueba.prueba_maven.modelo;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(
        name = "FICHA_RIESGO_DET",
        schema = "CONSULTORIO",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_FRD_FICHA_GRP_ITEM_ACT",
                columnNames = {"ID_FICHA", "GRUPO", "ITEM", "ACTIVIDAD_NRO"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString(exclude = {"ficha"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FichaRiesgoDet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
            name = "FICHA_RIESGO_DET_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA_RIESGO_DET",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FICHA_RIESGO_DET_GEN")
    @Column(name = "ID_FICHA_RIESGO_DET", nullable = false)
    @EqualsAndHashCode.Include
    private Long idFichaRiesgoDet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_FICHA", referencedColumnName = "ID_FICHA", nullable = false)
    private FichaOcupacional ficha;

    @Column(name = "GRUPO", length = 30, nullable = false)
    private String grupo; // FISICO/QUIMICO/BIOLOGICO/ERGONOMICO/SEGURIDAD/PSICOSOCIAL

    @Column(name = "ITEM", length = 300, nullable = false)
    private String item;

    @Column(name = "ACTIVIDAD_NRO", nullable = false)
    private Integer actividadNro; // 1..7

    @Column(name = "MARCADO", length = 1, nullable = false)
    private String marcado; // 'S'/'N'

    @Column(name = "ORDEN")
    private Integer orden;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    @PrePersist
    public void prePersist() {
        if (marcado == null || marcado.trim().isEmpty()) {
            marcado = "N"; // tu DDL: DEFAULT 'N'
        }
        if (fCreacion == null) {
            fCreacion = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fActualizacion = new Date();
    }
}
