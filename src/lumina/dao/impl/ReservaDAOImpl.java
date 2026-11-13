package lumina.dao.impl;

import lumina.dao.ReservaDAO;
import lumina.modelo.EstadoReserva;
import lumina.modelo.Reserva;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservaDAOImpl extends DAOBase implements ReservaDAO {

    private static final String SELECT = """
            SELECT r.id_reserva, r.id_estudiante, r.id_material, r.numero_ejemplar,
                   r.fecha_reserva, r.fecha_vencimiento, r.estado,
                   CONCAT(p.nombres, ' ', p.apellidos) AS nombre_estudiante,
                   m.titulo
            FROM reserva r
            JOIN persona p  ON p.id_persona = r.id_estudiante
            JOIN material m ON m.id_material = r.id_material
            """;

    private static final String ORDEN = " ORDER BY r.fecha_reserva DESC";

    public ReservaDAOImpl() {
    }

    public ReservaDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public void insertar(Reserva reserva) {
        // fecha_reserva toma el valor por defecto de la BD (CURRENT_TIMESTAMP)
        int id = ejecutar(con -> insertarConId(con, """
                INSERT INTO reserva (id_estudiante, id_material, numero_ejemplar, fecha_vencimiento, estado)
                VALUES (?, ?, ?, ?, ?)""",
                reserva.getIdEstudiante(), reserva.getIdMaterial(), reserva.getNumeroEjemplar(),
                reserva.getFechaVencimiento(), reserva.getEstado()));
        reserva.setIdReserva(id);
    }

    @Override
    public boolean cambiarEstado(int idReserva, EstadoReserva estado) {
        return ejecutarUpdate("UPDATE reserva SET estado = ? WHERE id_reserva = ?", estado, idReserva) > 0;
    }

    @Override
    public boolean cambiarEstado(int idReserva, EstadoReserva estadoActual, EstadoReserva nuevoEstado) {
        return ejecutarUpdate("UPDATE reserva SET estado = ? WHERE id_reserva = ? AND estado = ?",
                nuevoEstado, idReserva, estadoActual) > 0;
    }

    @Override
    public Optional<Reserva> buscarPorId(int idReserva) {
        return consultarUno(SELECT + " WHERE r.id_reserva = ?", ReservaDAOImpl::mapear, idReserva);
    }

    @Override
    public List<Reserva> listar() {
        return consultarLista(SELECT + ORDEN, ReservaDAOImpl::mapear);
    }

    @Override
    public List<Reserva> listarPorEstudiante(int idEstudiante) {
        return consultarLista(SELECT + " WHERE r.id_estudiante = ?" + ORDEN, ReservaDAOImpl::mapear, idEstudiante);
    }

    @Override
    public List<Reserva> listarPorEstado(EstadoReserva estado) {
        return consultarLista(SELECT + " WHERE r.estado = ?" + ORDEN, ReservaDAOImpl::mapear, estado);
    }

    @Override
    public int contarActivasPorMaterial(int idMaterial) {
        return consultarUno("SELECT COUNT(*) AS total FROM reserva WHERE id_material = ? AND estado = ?",
                rs -> rs.getInt("total"), idMaterial, EstadoReserva.ACTIVA).orElse(0);
    }

    private static Reserva mapear(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("id_reserva"));
        r.setIdEstudiante(rs.getInt("id_estudiante"));
        r.setIdMaterial(rs.getInt("id_material"));
        r.setNumeroEjemplar(getInteger(rs, "numero_ejemplar"));
        r.setFechaReserva(rs.getObject("fecha_reserva", LocalDateTime.class));
        r.setFechaVencimiento(rs.getObject("fecha_vencimiento", LocalDate.class));
        r.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
        r.setNombreEstudiante(rs.getString("nombre_estudiante"));
        r.setTituloMaterial(rs.getString("titulo"));
        return r;
    }
}
