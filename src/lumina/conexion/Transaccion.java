package lumina.conexion;

import lumina.dao.DAOException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta un bloque de trabajo dentro de una transacción.
 *
 * El bloque recibe una conexión abierta para crear los DAO que necesite
 * (por ejemplo new ReservaDAOImpl(con)). Si el bloque termina bien se hace
 * commit; si lanza cualquier excepción se hace rollback y la excepción sigue
 * su camino.
 */
public final class Transaccion {

    @FunctionalInterface
    public interface Trabajo<R> {
        R ejecutar(Connection con);
    }

    private Transaccion() {
    }

    public static <R> R ejecutar(Trabajo<R> trabajo) {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                R resultado = trabajo.ejecutar(con);
                con.commit();
                return resultado;
            } catch (RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al manejar la transacción: " + e.getMessage(), e);
        }
    }
}
