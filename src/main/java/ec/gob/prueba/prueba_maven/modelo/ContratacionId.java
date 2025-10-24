package ec.gob.prueba.prueba_maven.modelo;

import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class ContratacionId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "NO_PERSONA", nullable = false, precision = 6, scale = 0)
    private BigDecimal noPersona;

    @Column(name = "NO_CONT", nullable = false, precision = 6, scale = 0)
    private BigDecimal noCont;
}
