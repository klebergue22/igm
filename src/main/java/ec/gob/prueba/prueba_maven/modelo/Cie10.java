/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.modelo;

/**
 *
 * @author GUERRA_KLEBER
 */
 

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CIE10", schema = "CONSULTORIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cie10 implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CODIGO", length = 10, nullable = false)
    private String codigo;

    @Column(name = "DESCRIPCION", length = 500, nullable = false)
    private String descripcion;

    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "ID_PARENT_ID")
    private Long idParentId;

    @Column(name = "ESTADO", length = 10, nullable = false)
    private String estado;
}
