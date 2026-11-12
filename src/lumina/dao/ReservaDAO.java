package lumina.dao;

import lumina.modelo.EstadoReserva;
import lumina.modelo.Reserva;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla reserva. */
public interface ReservaDAO {

    /** Inserta la reserva (estado ACTIVA, fecha actual) y le asigna el id generado. */
    void insertar(Reserva reserva);

    boolean cambiarEstado(int idReserva, EstadoReserva estado);

    Optional<Reserva> buscarPorId(int idReserva);

    List<Reserva> listar();

    List<Reserva> listarPorEstudiante(int idEstudiante);

    List<Reserva> listarPorEstado(EstadoReserva estado);

    /** Reservas ACTIVAS de un material; sirve para controlar licencias de ebooks y audiolibros. */
    int contarActivasPorMaterial(int idMaterial);
}
