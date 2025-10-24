package ec.gob.prueba.prueba_maven.servicio;

import ec.gob.prueba.prueba_maven.modelo.Contratacion;
import ec.gob.prueba.prueba_maven.modelo.ContratacionId;
import ec.gob.prueba.prueba_maven.modelo.DatEmpleado;
import ec.gob.prueba.prueba_maven.modelo.enums.EstadoContrato;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Stateless
public class ContratacionService {

    @PersistenceContext(unitName = "rhPU")
    private EntityManager em;

    /* ===================== UTIL ===================== */
    private static BigDecimal toBD(Integer i) {
        return (i == null) ? null : BigDecimal.valueOf(i.longValue());
    }

    /* ===================== CRUD BÁSICO ===================== */

    public Contratacion crear(Contratacion c) {
        em.persist(c);
        return c;
    }

    /** Busca por PK compuesta (BigDecimal, BigDecimal). */
    public Contratacion porId(BigDecimal noPersona, BigDecimal noCont) {
        try {
            ContratacionId pk = new ContratacionId(noPersona, noCont);
            Contratacion c = em.find(Contratacion.class, pk);
            if (c != null) {
                System.out.println(">>> SERVICE.porId() OK: " +
                        "NO_PERSONA=" + c.getId().getNoPersona() +
                        " NO_CONT=" + c.getId().getNoCont() +
                        " ESTADO=" + c.getEstado() +
                        " RMU=" + c.getRmu());
            }
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar contrato: " + e.getMessage(), e);
        }
    }

    /** Lista por persona (BigDecimal) usando EmbeddedId. */
    public List<Contratacion> listarPorPersona(BigDecimal noPersona) {
        String jpql = "SELECT c FROM Contratacion c " +
                      "WHERE c.id.noPersona = :noPersona " +
                      "ORDER BY c.id.noCont";
        return em.createQuery(jpql, Contratacion.class)
                 .setParameter("noPersona", noPersona)
                 .getResultList();
    }

    /** Siguiente NO_CONT (BigDecimal) por persona. */
    public BigDecimal siguienteNoCont(BigDecimal noPersona) {
        try {
            String jpql = "SELECT MAX(c.id.noCont) FROM Contratacion c " +
                          "WHERE c.id.noPersona = :noPersona";
            BigDecimal max = em.createQuery(jpql, BigDecimal.class)
                               .setParameter("noPersona", noPersona)
                               .getSingleResult();
            return (max == null) ? BigDecimal.ONE : max.add(BigDecimal.ONE);
        } catch (Exception e) {
            System.err.println("Error al obtener siguiente NO_CONT: " + e.getMessage());
            return BigDecimal.ONE;
        }
    }

    public Contratacion actualizar(Contratacion c) {
        return em.merge(c);
    }

    public void eliminar(BigDecimal noPersona, BigDecimal noCont) {
        Contratacion c = porId(noPersona, noCont);
        if (c != null) em.remove(c);
    }

    /* ===================== QUERIES DE LISTADO/FILTRO ===================== */

    /** Paginado por persona (BigDecimal) y fecha contrato desc. */
    public List<Contratacion> porPersona(BigDecimal noPersona, int page, int size) {
        String jpql = "SELECT c FROM Contratacion c " +
                      "WHERE c.id.noPersona = :noPersona " +
                      "ORDER BY c.feContrato DESC";
        return em.createQuery(jpql, Contratacion.class)
                 .setParameter("noPersona", noPersona)
                 .setFirstResult(page * size)
                 .setMaxResults(size)
                 .getResultList();
    }

    /** Vigentes por persona (BigDecimal, enum EstadoContrato). */
    public List<Contratacion> vigentesPorPersona(BigDecimal noPersona) {
        String jpql = "SELECT c FROM Contratacion c " +
                      "WHERE c.id.noPersona = :p AND c.estado = :est " +
                      "ORDER BY c.feContrato DESC";
        return em.createQuery(jpql, Contratacion.class)
                 .setParameter("p", noPersona)
                 .setParameter("est", EstadoContrato.VIGENTE.name()) // estado es String en la entidad
                 .getResultList();
    }

    /** Por rango de fechas (feContrato) con paginado. */
    public List<Contratacion> porRangoFechas(Date desde, Date hasta, int page, int size) {
        String jpql = "SELECT c FROM Contratacion c " +
                      "WHERE c.feContrato BETWEEN :d AND :h " +
                      "ORDER BY c.feContrato DESC";
        return em.createQuery(jpql, Contratacion.class)
                 .setParameter("d", desde, TemporalType.DATE)
                 .setParameter("h", hasta, TemporalType.DATE)
                 .setFirstResult(page * size)
                 .setMaxResults(size)
                 .getResultList();
    }

    /** Conteo por persona (BigDecimal). */
    public long contarPorPersona(BigDecimal noPersona) {
        String jpql = "SELECT COUNT(c) FROM Contratacion c WHERE c.id.noPersona = :noPersona";
        return em.createQuery(jpql, Long.class)
                 .setParameter("noPersona", noPersona)
                 .getSingleResult();
    }

    /** Existe persona en T_DAT_EMPLEADO (BigDecimal). */
    public boolean existePersona(BigDecimal noPersona) {
        Long cnt = em.createQuery(
                "SELECT COUNT(d) FROM DatEmpleado d WHERE d.noPersona = :p", Long.class)
                .setParameter("p", noPersona)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    /** Trae empleado (para nombre/campos iniciales). */
    public DatEmpleado obtenerEmpleado(BigDecimal noPersona) {
        List<DatEmpleado> l = em.createQuery(
                "SELECT d FROM DatEmpleado d WHERE d.noPersona = :p", DatEmpleado.class)
                .setParameter("p", noPersona)
                .setMaxResults(1)
                .getResultList();
        return l.isEmpty() ? null : l.get(0);
    }

    /** Cuenta contratos por persona (BigDecimal). */
    public long contarContratosPorPersona(BigDecimal noPersona) {
        String jpql = "SELECT COUNT(c) FROM Contratacion c WHERE c.id.noPersona = :p";
        return em.createQuery(jpql, Long.class)
                 .setParameter("p", noPersona)
                 .getSingleResult();
    }

    /** Cuenta vigentes por persona (BigDecimal). */
    public long contarVigentesPorPersona(BigDecimal noPersona) {
        String jpql = "SELECT COUNT(c) FROM Contratacion c " +
                      "WHERE c.id.noPersona = :p AND c.estado = :est";
        return em.createQuery(jpql, Long.class)
                 .setParameter("p", noPersona)
                 .setParameter("est", EstadoContrato.VIGENTE.name()) // estado es String
                 .getSingleResult();
    }

    /**
     * Usa el NamedQuery de tu entidad:
     *  @NamedQuery(name="Contratacion.porPersonaTodos",
     *              query="SELECT c FROM Contratacion c WHERE c.id.noPersona = :noPersona ORDER BY c.fContrato DESC")
     * El controller te pasa Integer → convertimos a BigDecimal aquí.
     */
    public List<Contratacion> porPersonaTodos(Integer noPersona) {
        BigDecimal p = toBD(noPersona);
        return em.createNamedQuery("Contratacion.porPersonaTodos", Contratacion.class)
                 .setParameter("noPersona", p)
                 .getResultList();
    }

    /**
     * ¿Existe contrato VIGENTE sin fecha de salida? (BigDecimal)
     * OJO: estado es String en la entidad → usar .name()
     */
public boolean existeContratoVigenteSinSalida(BigDecimal noPersona) {
    Long cnt = em.createQuery(
            "SELECT COUNT(c) FROM Contratacion c " +
            "WHERE c.id.noPersona = :p " +
            "AND c.estado = :est " +
            "AND c.feSalida IS NULL", Long.class)
        .setParameter("p", noPersona)
        .setParameter("est", EstadoContrato.VIGENTE)  // <-- ENUM, no String
        .getSingleResult();

    return cnt != null && cnt > 0;
}


    /** Obtiene el contrato vigente sin salida (o null). */
    public Contratacion obtenerContratoVigenteSinSalida(BigDecimal noPersona) {
        try {
            String jpql = "SELECT c FROM Contratacion c " +
                          "WHERE c.id.noPersona = :noPersona AND c.estado = :est AND c.feSalida IS NULL";
            return em.createQuery(jpql, Contratacion.class)
                     .setParameter("noPersona", noPersona)
                     .setParameter("est", EstadoContrato.VIGENTE.name())
                     .setMaxResults(1)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /* ===================== GUARDAR (nuevo/editar) ===================== */

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Contratacion guardarNuevo(Contratacion c) {
        em.persist(c);
        return c;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Contratacion guardar(Contratacion c) {
        System.out.println(">>> SERVICE.guardar() - Inicio");
        System.out.println("    ID: " + c.getId());
        System.out.println("    NO_CONT: " + (c.getId() != null ? c.getId().getNoCont() : null));
        System.out.println("    ESTADO: " + c.getEstado());
        //System.out.println("    TIPO CONTRATO: " + c.);

        try {
            // Si no tiene NO_CONT, es nuevo → asignar correlativo y EmbeddedId
            if (c.getId() == null || c.getId().getNoCont() == null) {
                BigDecimal persona = (c.getId() != null) ? c.getId().getNoPersona() : null;
                if (persona == null) {
                    throw new IllegalArgumentException("No se ha definido NO_PERSONA en el ID.");
                }
                BigDecimal sig = siguienteNoCont(persona);

                if (c.getId() == null) {
                    ContratacionId id = new ContratacionId();
                    id.setNoPersona(persona);
                    id.setNoCont(sig);
                    c.setId(id);
                } else {
                    c.getId().setNoCont(sig);
                }

                System.out.println(">>> PERSIST (nuevo) con ID: " + c.getId());
                em.persist(c);
                em.flush();
                return c;
            } else {
                System.out.println(">>> MERGE (actualización)");
                Contratacion merged = em.merge(c);
                em.flush();
                return merged;
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR en SERVICE.guardar(): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar contratación: " + e.getMessage(), e);
        }
    }
}
