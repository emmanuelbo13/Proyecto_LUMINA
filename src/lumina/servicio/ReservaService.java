package lumina.servicio;

import lumina.conexion.Transaccion;
import lumina.dao.EjemplarDAO;
import lumina.dao.MaterialDAO;
import lumina.dao.PersonaDAO;
import lumina.dao.ReservaDAO;
import lumina.dao.impl.EjemplarDAOImpl;
import lumina.dao.impl.MaterialDAOImpl;
import lumina.dao.impl.PersonaDAOImpl;
import lumina.dao.impl.ReservaDAOImpl;
import lumina.modelo.Ejemplar;
import lumina.modelo.EstadoEjemplar;
import lumina.modelo.EstadoMaterial;
import lumina.modelo.EstadoPersona;
import lumina.modelo.EstadoReserva;
import lumina.modelo.Estudiante;
import lumina.modelo.LibroFisico;
import lumina.modelo.Licenciable;
import lumina.modelo.Material;
import lumina.modelo.Persona;
import lumina.modelo.Reserva;

import java.time.LocalDate;
import java.util.List;

/**
 * Reglas de negocio de las reservas.
 *
 * Cada operación que escribe usa una sola transacción para todos los DAO:
 * si algo falla (por ejemplo, al marcar el ejemplar), la reserva tampoco se guarda.
 */
public class ReservaService {

    /** Días que dura una reserva (los datos de prueba usan 3). */
    public static final int DIAS_VIGENCIA = 3;

    /** Máximo de reservas ACTIVAS simultáneas por estudiante. */
    public static final int MAX_RESERVAS_ACTIVAS = 3;

    // ------------------------------------------------------------------
    // Operaciones que modifican datos
    // ------------------------------------------------------------------

    /**
     * Reserva un material para un estudiante.
     *  - Libro físico: toma el primer ejemplar DISPONIBLE y lo marca RESERVADO.
     *  - Ebook / audiolibro: verifica que quede al menos una licencia libre.
     */
    public Reserva reservar(int idEstudiante, int idMaterial) {
        return Transaccion.ejecutar(con -> {
            PersonaDAO personaDAO = new PersonaDAOImpl(con);
            MaterialDAO materialDAO = new MaterialDAOImpl(con);
            ReservaDAO reservaDAO = new ReservaDAOImpl(con);

            Estudiante estudiante = validarEstudiante(personaDAO, idEstudiante);
            Material material = validarMaterial(materialDAO, idMaterial);
            validarCupoDelEstudiante(reservaDAO, idEstudiante, idMaterial);

            Integer numeroEjemplar = null;
            if (material instanceof LibroFisico) {
                numeroEjemplar = apartarEjemplar(new EjemplarDAOImpl(con), idMaterial);
            } else if (material instanceof Licenciable licenciable) {
                validarLicenciaLibre(reservaDAO, licenciable, idMaterial);
            }

            Reserva reserva = new Reserva(idEstudiante, idMaterial, numeroEjemplar,
                    LocalDate.now().plusDays(DIAS_VIGENCIA));
            reservaDAO.insertar(reserva);

            // Se vuelve a leer para devolverla completa (fecha de reserva, nombres)
            return reservaDAO.buscarPorId(reserva.getIdReserva()).orElseThrow();
        });
    }

    /** El estudiante o el bibliotecario cancela una reserva ACTIVA. El ejemplar queda DISPONIBLE. */
    public Reserva cancelar(int idReserva) {
        return cerrar(idReserva, EstadoReserva.CANCELADA, EstadoEjemplar.DISPONIBLE);
    }

    /**
     * El bibliotecario finaliza una reserva ACTIVA (el material fue entregado y devuelto).
     * Si el libro regresó en mal estado, el ejemplar queda DANADO en lugar de DISPONIBLE.
     */
    public Reserva finalizar(int idReserva, boolean ejemplarDanado) {
        return cerrar(idReserva, EstadoReserva.FINALIZADA,
                ejemplarDanado ? EstadoEjemplar.DANADO : EstadoEjemplar.DISPONIBLE);
    }

    /**
     * Pasa a VENCIDA toda reserva ACTIVA cuya fecha de vencimiento ya pasó
     * y libera sus ejemplares. Pensado para ejecutarse al iniciar la aplicación.
     *
     * @return cantidad de reservas vencidas
     */
    public int marcarVencidas() {
        return Transaccion.ejecutar(con -> {
            ReservaDAO reservaDAO = new ReservaDAOImpl(con);
            EjemplarDAO ejemplarDAO = new EjemplarDAOImpl(con);
            LocalDate hoy = LocalDate.now();
            int vencidas = 0;

            for (Reserva r : reservaDAO.listarPorEstado(EstadoReserva.ACTIVA)) {
                if (r.getFechaVencimiento().isBefore(hoy)
                        && reservaDAO.cambiarEstado(r.getIdReserva(), EstadoReserva.ACTIVA, EstadoReserva.VENCIDA)) {
                    liberarEjemplar(ejemplarDAO, r, EstadoEjemplar.DISPONIBLE);
                    vencidas++;
                }
            }
            return vencidas;
        });
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Ejemplares disponibles (libro físico) o licencias libres (ebook / audiolibro). */
    public int consultarDisponibilidad(int idMaterial) {
        Material material = new MaterialDAOImpl().buscarPorId(idMaterial)
                .orElseThrow(() -> new ServicioException("El material no existe"));
        if (material.getEstado() != EstadoMaterial.ACTIVO) {
            return 0;
        }
        return switch (material) {
            case LibroFisico l -> new EjemplarDAOImpl().listarDisponibles(idMaterial).size();
            case Licenciable l -> Math.max(0,
                    l.getTotalLicencias() - new ReservaDAOImpl().contarActivasPorMaterial(idMaterial));
        };
    }

    public List<Reserva> listarPorEstudiante(int idEstudiante) {
        return new ReservaDAOImpl().listarPorEstudiante(idEstudiante);
    }

    public List<Reserva> listarActivas() {
        return new ReservaDAOImpl().listarPorEstado(EstadoReserva.ACTIVA);
    }

    /** Lista las reservas en un estado, o todas si estado es null. */
    public List<Reserva> listar(EstadoReserva estado) {
        ReservaDAO dao = new ReservaDAOImpl();
        return estado == null ? dao.listar() : dao.listarPorEstado(estado);
    }

    // ------------------------------------------------------------------
    // Reglas internas
    // ------------------------------------------------------------------

    private Reserva cerrar(int idReserva, EstadoReserva nuevoEstado, EstadoEjemplar estadoEjemplar) {
        return Transaccion.ejecutar(con -> {
            ReservaDAO reservaDAO = new ReservaDAOImpl(con);
            Reserva reserva = reservaDAO.buscarPorId(idReserva)
                    .orElseThrow(() -> new ServicioException("La reserva #" + idReserva + " no existe"));

            if (!reservaDAO.cambiarEstado(idReserva, EstadoReserva.ACTIVA, nuevoEstado)) {
                throw new ServicioException("Solo se pueden cerrar reservas ACTIVAS; la reserva #"
                        + idReserva + " está " + reserva.getEstado());
            }
            liberarEjemplar(new EjemplarDAOImpl(con), reserva, estadoEjemplar);
            reserva.setEstado(nuevoEstado);
            return reserva;
        });
    }

    private static Estudiante validarEstudiante(PersonaDAO personaDAO, int idEstudiante) {
        Persona persona = personaDAO.buscarPorId(idEstudiante)
                .orElseThrow(() -> new ServicioException("La persona no existe"));
        if (!(persona instanceof Estudiante estudiante)) {
            throw new ServicioException("Solo los estudiantes pueden hacer reservas");
        }
        if (estudiante.getEstado() != EstadoPersona.ACTIVO) {
            throw new ServicioException("El estudiante está inactivo");
        }
        return estudiante;
    }

    private static Material validarMaterial(MaterialDAO materialDAO, int idMaterial) {
        Material material = materialDAO.buscarPorId(idMaterial)
                .orElseThrow(() -> new ServicioException("El material no existe"));
        if (material.getEstado() != EstadoMaterial.ACTIVO) {
            throw new ServicioException("El material '" + material.getTitulo() + "' fue retirado");
        }
        return material;
    }

    private static void validarCupoDelEstudiante(ReservaDAO reservaDAO, int idEstudiante, int idMaterial) {
        List<Reserva> activas = reservaDAO.listarPorEstudiante(idEstudiante).stream()
                .filter(r -> r.getEstado() == EstadoReserva.ACTIVA)
                .toList();
        if (activas.stream().anyMatch(r -> r.getIdMaterial() == idMaterial)) {
            throw new ServicioException("El estudiante ya tiene una reserva activa de este material");
        }
        if (activas.size() >= MAX_RESERVAS_ACTIVAS) {
            throw new ServicioException("El estudiante ya tiene " + MAX_RESERVAS_ACTIVAS
                    + " reservas activas, que es el máximo permitido");
        }
    }

    /** Marca como RESERVADO el primer ejemplar disponible y devuelve su número. */
    private static int apartarEjemplar(EjemplarDAO ejemplarDAO, int idMaterial) {
        for (Ejemplar ejemplar : ejemplarDAO.listarDisponibles(idMaterial)) {
            // El cambio condicional falla si otro usuario tomó este ejemplar justo antes
            if (ejemplarDAO.cambiarEstado(idMaterial, ejemplar.getNumeroEjemplar(),
                    EstadoEjemplar.DISPONIBLE, EstadoEjemplar.RESERVADO)) {
                return ejemplar.getNumeroEjemplar();
            }
        }
        throw new ServicioException("No hay ejemplares disponibles de este libro");
    }

    private static void validarLicenciaLibre(ReservaDAO reservaDAO, Licenciable material, int idMaterial) {
        int enUso = reservaDAO.contarActivasPorMaterial(idMaterial);
        if (enUso >= material.getTotalLicencias()) {
            throw new ServicioException("Todas las licencias de este material están en uso ("
                    + enUso + "/" + material.getTotalLicencias() + ")");
        }
    }

    private static void liberarEjemplar(EjemplarDAO ejemplarDAO, Reserva reserva, EstadoEjemplar nuevoEstado) {
        if (reserva.getNumeroEjemplar() != null) {
            ejemplarDAO.cambiarEstado(reserva.getIdMaterial(), reserva.getNumeroEjemplar(),
                    EstadoEjemplar.RESERVADO, nuevoEstado);
        }
    }
}
