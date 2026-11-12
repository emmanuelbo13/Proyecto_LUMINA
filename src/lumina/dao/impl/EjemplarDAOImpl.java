package lumina.dao.impl;

import lumina.dao.EjemplarDAO;
import lumina.modelo.Ejemplar;
import lumina.modelo.EstadoEjemplar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EjemplarDAOImpl extends DAOBase implements EjemplarDAO {

    private static final String SELECT =
            "SELECT id_material, numero_ejemplar, estado, fecha_ingreso FROM ejemplar";

    public EjemplarDAOImpl() {
    }

    public EjemplarDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public void insertar(Ejemplar ejemplar) {
        int numero = ejecutarEnTransaccion(con -> {
            // Bloquea los ejemplares del libro para que dos inserciones simultáneas no tomen el mismo número
            int siguiente = uno(con, """
                    SELECT COALESCE(MAX(numero_ejemplar), 0) + 1 AS siguiente
                    FROM ejemplar WHERE id_material = ? FOR UPDATE""",
                    rs -> rs.getInt("siguiente"), ejemplar.getIdMaterial()).orElse(1);
            update(con, """
                    INSERT INTO ejemplar (id_material, numero_ejemplar, estado, fecha_ingreso)
                    VALUES (?, ?, ?, ?)""",
                    ejemplar.getIdMaterial(), siguiente, ejemplar.getEstado(), ejemplar.getFechaIngreso());
            return siguiente;
        });
        ejemplar.setNumeroEjemplar(numero);
    }

    @Override
    public boolean cambiarEstado(int idMaterial, int numeroEjemplar, EstadoEjemplar estado) {
        return ejecutarUpdate("UPDATE ejemplar SET estado = ? WHERE id_material = ? AND numero_ejemplar = ?",
                estado, idMaterial, numeroEjemplar) > 0;
    }

    @Override
    public Optional<Ejemplar> buscar(int idMaterial, int numeroEjemplar) {
        return consultarUno(SELECT + " WHERE id_material = ? AND numero_ejemplar = ?",
                EjemplarDAOImpl::mapear, idMaterial, numeroEjemplar);
    }

    @Override
    public List<Ejemplar> listarPorMaterial(int idMaterial) {
        return consultarLista(SELECT + " WHERE id_material = ? ORDER BY numero_ejemplar",
                EjemplarDAOImpl::mapear, idMaterial);
    }

    @Override
    public List<Ejemplar> listarDisponibles(int idMaterial) {
        return consultarLista(SELECT + " WHERE id_material = ? AND estado = ? ORDER BY numero_ejemplar",
                EjemplarDAOImpl::mapear, idMaterial, EstadoEjemplar.DISPONIBLE);
    }

    private static Ejemplar mapear(ResultSet rs) throws SQLException {
        return new Ejemplar(rs.getInt("id_material"), rs.getInt("numero_ejemplar"),
                EstadoEjemplar.valueOf(rs.getString("estado")),
                rs.getObject("fecha_ingreso", LocalDate.class));
    }
}
