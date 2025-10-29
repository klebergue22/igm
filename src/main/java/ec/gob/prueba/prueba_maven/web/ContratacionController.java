package ec.gob.prueba.prueba_maven.web;

import ec.gob.prueba.prueba_maven.modelo.Contratacion;
import ec.gob.prueba.prueba_maven.modelo.ContratacionId;
import ec.gob.prueba.prueba_maven.modelo.DatEmpleado;
import ec.gob.prueba.prueba_maven.modelo.enums.EstadoContrato;
import ec.gob.prueba.prueba_maven.modelo.enums.TipoContrato;
import ec.gob.prueba.prueba_maven.servicio.ContratacionService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped; // JSF 2.2
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;

import org.primefaces.PrimeFaces;

@ManagedBean(name = "contratacionController")
@ViewScoped
@Slf4j
@Getter @Setter
public class ContratacionController implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private ContratacionService service;

    /* Filtro / lista */
    private BigDecimal filtroNoPersona;
    private List<Contratacion> lista;

    /* Edición */
    private Contratacion seleccionado;

    /* Soporte UI */
    private String nombreEmpleado = "";
    private List<EstadoContrato> estadosEnum;
    private List<TipoContrato> tiposContratoEnum;

    @PostConstruct
    public void init() {
        log.info("Inicializando ContratacionController...");
        lista = Collections.emptyList();
        seleccionado = null;
        estadosEnum = Arrays.asList(EstadoContrato.values());
        tiposContratoEnum = Arrays.asList(TipoContrato.values());
        log.info("Controller inicializado correctamente.");
    }

    /* Para pruebas rápidas desde la vista */
    public void ping() {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Ping OK - " + new Date(), null));
    }

    /* ===================== BÚSQUEDA ===================== */
    public void buscar() {
        log.info("Ejecutando búsqueda para persona: {}", filtroNoPersona);

        if (filtroNoPersona == null) {
            msgWarn("Ingrese No. Persona");
            lista = Collections.emptyList();
            return;
        }

        try {
            if (!service.existePersona(filtroNoPersona)) {
                msgWarn("El No. Persona " + filtroNoPersona + " no existe en Datos Personales.");
                lista = Collections.emptyList();
                return;
            }

            lista = service.porPersonaTodos(filtroNoPersona.intValue());

            if (lista == null || lista.isEmpty()) {
                DatEmpleado de = service.obtenerEmpleado(filtroNoPersona);
                nombreEmpleado = (de != null && de.getNombreC() != null) ? de.getNombreC() : "(sin nombre)";
                PrimeFaces.current().ajax().update("layoutForm:dlgCrear");
                PrimeFaces.current().executeScript("PF('dlgCrear').show()");
            } else {
                msgInfo("Contratos encontrados: " + lista.size());
            }
        } catch (Exception e) {
            log.error("Error al buscar contratos", e);
            msgError("Error al buscar: " + e.getMessage());
        }
    }

    /* ===================== NUEVO ===================== */
    public void nuevoDesdeBoton() {
        if (filtroNoPersona == null) {
            msgWarn("Ingrese No. Persona");
            return;
        }
        if (!service.existePersona(filtroNoPersona)) {
            msgWarn("El No. Persona " + filtroNoPersona + " no existe en Datos Personales.");
            return;
        }
        if (service.existeContratoVigenteSinSalida(filtroNoPersona)) {
            msgError("Contrato Vigente Activo",
                    "Ya existe un contrato VIGENTE sin fecha de salida para el No. Persona " + filtroNoPersona);
            return;
        }
        prepararNuevoYMostrar();
    }

    public void crearNuevoDesdeDialogo() {
        prepararNuevoYMostrar();
        PrimeFaces.current().executeScript("PF('dlgCrear').hide()");
    }

    private void prepararNuevoYMostrar() {
        try {
            BigDecimal noCont = service.siguienteNoCont(filtroNoPersona);

            ContratacionId id = new ContratacionId(filtroNoPersona, noCont);
            seleccionado = new Contratacion();
            seleccionado.setId(id);

            // Defaults UI
            seleccionado.setEstado(EstadoContrato.VIGENTE); // enum directo
            seleccionado.setFeContrato(new Date());

            PrimeFaces.current().ajax().update("layoutForm:dlg", "layoutForm:dlgBody");
            PrimeFaces.current().executeScript("PF('dlgCont').show()");
        } catch (Exception e) {
            log.error("Error preparando nuevo contrato", e);
            msgError("Error preparando nuevo contrato: " + e.getMessage());
        }
    }

    /* ===================== EDICIÓN ===================== */
    public void prepararEdicion(Contratacion c) {
        this.seleccionado = c;
        log.info("Preparando edición de contrato: {}-{}",
                c.getId().getNoPersona(), c.getId().getNoCont());
        // Nada más: seleccionado.getEstado() ya es EstadoContrato
    }

    /* ===================== GUARDAR ===================== */
    public void guardar() {
        log.info("[GUARDAR] estado={}, feContrato={}",
                (seleccionado != null ? seleccionado.getEstado() : null),
                (seleccionado != null ? seleccionado.getFeContrato() : null));

        try {
            if (seleccionado == null) {
                msgWarn("No hay datos para guardar");
                return;
            }
            if (seleccionado.getEstado() == null) {
                msgWarn("Estado es obligatorio");
                return;
            }

            // Asegurar PK si es nuevo
            if (seleccionado.getId() == null) {
                ContratacionId id = new ContratacionId();
                id.setNoPersona(filtroNoPersona);
                id.setNoCont(service.siguienteNoCont(filtroNoPersona));
                seleccionado.setId(id);
            }

            seleccionado = service.guardar(seleccionado);

            msgInfo("Contrato guardado exitosamente");
            buscar();  // refrescar tabla
            PrimeFaces.current().executeScript("PF('dlgCont').hide()");
        } catch (Exception e) {
            log.error("Error al guardar contrato", e);
            msgError("Error al guardar: " + e.getMessage());
        }
    }

    /* ===================== MENSAJES ===================== */
    private void msg(FacesMessage.Severity s, String m) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(s, m, null));
    }
    private void msgWarn(String m) { msg(FacesMessage.SEVERITY_WARN, m); }
    private void msgInfo(String m) { msg(FacesMessage.SEVERITY_INFO, m); }
    private void msgError(String m) { msg(FacesMessage.SEVERITY_ERROR, m); }
    private void msgError(String titulo, String detalle) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, titulo, detalle);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}
