package ec.gob.prueba.prueba_maven.web;

import ec.gob.prueba.prueba_maven.modelo.DatEmpleado;
import ec.gob.prueba.prueba_maven.servicio.EmpleadoService;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "atencionMedicaCtrl")
@ViewScoped
public class AtencionMedicaCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private EmpleadoService empleadoService;   // <-- tu servicio real

    private List<DatEmpleado> lista;           // lista para la tabla
    private DatEmpleado seleccionado;          // empleado seleccionado
    private String filtroApellidos;            // opcional, por si filtras

    // ================== CICLO DE VIDA ==================

    @PostConstruct
    public void init() {
        cargarEmpleados();
    }

    // Si usas <f:viewAction>, puedes tener también:
    public void preRenderView() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            cargarEmpleados();
        }
    }

    // ================== LÓGICA ==================

    public void cargarEmpleados() {
        try {
            if (filtroApellidos == null || filtroApellidos.trim().isEmpty()) {
                lista = empleadoService.listarTodos();
            } else {
                lista = empleadoService.buscarPorApellidos(filtroApellidos);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // aquí puedes meter mensajes Faces si tienes utilitario
        }
    }

    public void buscar() {
        cargarEmpleados();
    }

    /**
     * Acción del botón "Atender" en la tabla.
     * Envía el empleado seleccionado vía Flash y navega al centroMedico.xhtml
     */
    public String abrirAtencion(DatEmpleado emp) {
        this.seleccionado = emp;

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Flash flash = ec.getFlash();
        flash.put("empleadoAtencion", emp);

        // ajusta la ruta si tu xhtml está en otra carpeta
        return "/centroMedico.xhtml?faces-redirect=true";
    }

    // ================== GETTERS / SETTERS ==================

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
}
