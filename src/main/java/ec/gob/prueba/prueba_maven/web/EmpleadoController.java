/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.gob.prueba.prueba_maven.web;

import ec.gob.prueba.prueba_maven.modelo.DatEmpleado;
import ec.gob.prueba.prueba_maven.modelo.enums.EstadoCivil;
import ec.gob.prueba.prueba_maven.modelo.enums.GrupoSangre;
import ec.gob.prueba.prueba_maven.modelo.enums.Sexo;
import ec.gob.prueba.prueba_maven.servicio.EmpleadoService;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author GUERRA_KLEBER
 */
@ManagedBean(name = "empleadoController")
@ViewScoped
 
public class EmpleadoController implements Serializable {
     private static final long serialVersionUID = 1L; // <- fijo y estable

    @EJB
    private EmpleadoService empleadoService;

    private List<DatEmpleado> lista = java.util.Collections.emptyList();

    private DatEmpleado seleccionado;
    private String filtroApellidos;
    private String filtroGlobal;

    private Date fechaMaxNacimiento; // hoy - 18 años

    @PostConstruct
   public void init() {


    seleccionado = new DatEmpleado();
    fechaMaxNacimiento = calcularFechaMaxNacimiento();
    recargarListaSegura(); // como ya lo dejaste con try/catch
    }

    //METODOS
public void preRenderView() {
    // Solo en el primer GET (evita recargar en postbacks/AJAX)
    if (!FacesContext.getCurrentInstance().isPostback()) {
        recargarListaSegura();
    }
}

   private void recargarListaSegura() {
        try {
            lista = empleadoService.listarTodos();
        } catch (Exception e) {
            lista = java.util.Collections.emptyList();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo cargar empleados", e.getMessage()));
            // importante: NO relanzar excepción aquí
        }
    }
    public void debugPing() {
    System.out.println(">>>> PING llegó al servidor");
    FacesContext.getCurrentInstance().addMessage(null,
        new FacesMessage(FacesMessage.SEVERITY_INFO, "PING OK", "El submit llegó"));
}

    public void debugLog() {
        System.out.println("DEBUG: Se ejecutó debugLog()");
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "DEBUG", "Se ejecutó debugLog()"));
    }

    public void ping() {
        System.out.println("DEBUG: Ping desde botón fuera del diálogo");
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "PING", "Botón de prueba ejecutado"));
    }

 

    public void validarMayorDeEdad(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) {
            return;
        }
        Date fecha = (Date) value;
        Date limite = calcularFechaMaxNacimiento(); // hoy - 18
        if (fecha.after(limite)) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Debe ser mayor de 18 años.",
                            "Seleccione una fecha de nacimiento válida (≥ 18 años).")
            );
        }
    }

    private Date calcularFechaMaxNacimiento() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.YEAR, -18); // hoy - 18
        return cal.getTime();
    }

    public void buscarPorApellidos() {
        try {
            lista = empleadoService.buscarPorApellidos(filtroApellidos);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al buscar por apellidos", e.getMessage()));
        }
    }

    public void nuevo() {
        seleccionado = new DatEmpleado();
    }

public void editar(Integer noPersona) {
        try {
            seleccionado = empleadoService.buscarPorId(noPersona);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se pudo cargar el empleado", e.getMessage()));
        }
    }

   public void guardar() {
        System.out.println(">>> GUARDAR :: GS=" + seleccionado.getGrupoSangre()
            + ", SEXO=" + seleccionado.getSexo()
            + ", ESTCIVIL=" + seleccionado.getEstCivil());
        boolean esNuevo = (seleccionado.getNoPersona() == null);

        try {
            empleadoService.guardar(seleccionado);
            recargarListaSegura();  // <- refresca con protección

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    esNuevo ? "Empleado creado" : "Empleado actualizado",
                    "Cédula: " + seleccionado.getNoCedula()));

            org.primefaces.PrimeFaces.current().ajax()
                .addCallbackParam("validationFailed", false);
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al guardar", ex.getMessage()));
            org.primefaces.PrimeFaces.current().ajax()
                .addCallbackParam("validationFailed", true);
        }
    }

 public void eliminar(Integer noPersona) {
        try {
            empleadoService.eliminar(noPersona);
            recargarListaSegura();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Registro eliminado", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al eliminar", e.getMessage()));
        }
    }

    private void info(String m) {
        javax.faces.context.FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    // getters/setters
    public List<DatEmpleado> getLista() {
        return lista;
    }

    public void setLista(List<DatEmpleado> lista) {
        this.lista = lista;
    }

    public DatEmpleado getSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(DatEmpleado seleccionado) {
        this.seleccionado = seleccionado;
    }

    public String getFiltroApellidos() {
        return filtroApellidos;
    }

    public void setFiltroApellidos(String filtroApellidos) {
        this.filtroApellidos = filtroApellidos;
    }

    public String getFiltroGlobal() {
        return filtroGlobal;
    }

    public void setFiltroGlobal(String filtroGlobal) {
        this.filtroGlobal = filtroGlobal;
    }

    public Date getFechaMaxNacimiento() {
        return fechaMaxNacimiento;
    }
    // Items siempre disponibles en postback

    public GrupoSangre[] getGruposSangre() {
        return GrupoSangre.values();
    }

    public Sexo[] getSexos() {
        return Sexo.values();
    }

    public EstadoCivil[] getEstadosCiviles() {
        return EstadoCivil.values();
    }

    public int getAnioActual() {
        return java.time.Year.now().getValue();
    }

}
