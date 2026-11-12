package lumina.dao.impl;

import lumina.conexion.Conexion;
import lumina.dao.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Base común de los DAO JDBC.
 *
 * Maneja dos modos de trabajo:
 *  - Sin conexión externa (constructor vacío): cada operación abre su propia
 *    conexión y la cierra al terminar.
 *  - Con conexión externa: todas las operaciones usan esa conexión y NO la
 *    cierran ni hacen commit. Así una capa de servicio puede agrupar varias
 *    operaciones de distintos DAO en una sola transacción.
 */
abstract class DAOBase {

    /** Operación que trabaja sobre una conexión abierta. */
    @FunctionalInterface
    protected interface OperacionSQL<R> {
        R ejecutar(Connection con) throws SQLException;
    }

    /** Convierte la fila actual de un ResultSet en un objeto. */
    @FunctionalInterface
    protected interface Mapeador<T> {
        T mapear(ResultSet rs) throws SQLException;
    }

    private final Connection conexionExterna;

    protected DAOBase() {
        this(null);
    }

    protected DAOBase(Connection conexionExterna) {
        this.conexionExterna = conexionExterna;
    }

    // ------------------------------------------------------------------
    // Manejo de conexión y transacciones
    // ------------------------------------------------------------------

    protected <R> R ejecutar(OperacionSQL<R> operacion) {
        try {
            if (conexionExterna != null) {
                return operacion.ejecutar(conexionExterna);
            }
            try (Connection con = Conexion.getConexion()) {
                return operacion.ejecutar(con);
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    /**
     * Ejecuta varias sentencias como una unidad: si una falla, se deshacen todas.
     * Con conexión externa, la transacción la controla quien creó el DAO.
     */
    protected <R> R ejecutarEnTransaccion(OperacionSQL<R> operacion) {
        if (conexionExterna != null) {
            return ejecutar(operacion);
        }
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                R resultado = operacion.ejecutar(con);
                con.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e);
        }
    }

    private static DAOException traducir(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return new DAOException("Operación rechazada por una restricción de la base de datos: "
                    + e.getMessage(), e);
        }
        return new DAOException("Error de base de datos: " + e.getMessage(), e);
    }

    // ------------------------------------------------------------------
    // Utilidades sobre una conexión ya abierta
    // ------------------------------------------------------------------

    protected static <T> List<T> lista(Connection con, String sql, Mapeador<T> mapeador,
                                       Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(con, sql, false, parametros);
             ResultSet rs = ps.executeQuery()) {
            List<T> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapeador.mapear(rs));
            }
            return resultado;
        }
    }

    protected static <T> Optional<T> uno(Connection con, String sql, Mapeador<T> mapeador,
                                         Object... parametros) throws SQLException {
        List<T> resultado = lista(con, sql, mapeador, parametros);
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }

    /** INSERT, UPDATE o DELETE. Devuelve el número de filas afectadas. */
    protected static int update(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(con, sql, false, parametros)) {
            return ps.executeUpdate();
        }
    }

    /** INSERT sobre una tabla AUTO_INCREMENT. Devuelve el id generado. */
    protected static int insertarConId(Connection con, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = preparar(con, sql, true, parametros)) {
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("La base de datos no devolvió el id generado");
                }
                return claves.getInt(1);
            }
        }
    }

    private static PreparedStatement preparar(Connection con, String sql, boolean devolverClaves,
                                              Object... parametros) throws SQLException {
        PreparedStatement ps = devolverClaves
                ? con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : con.prepareStatement(sql);
        try {
            for (int i = 0; i < parametros.length; i++) {
                Object valor = parametros[i];
                // Los enums se guardan por nombre en las columnas ENUM de MySQL
                ps.setObject(i + 1, valor instanceof Enum<?> e ? e.name() : valor);
            }
            return ps;
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
    }

    // ------------------------------------------------------------------
    // Atajos que abren y cierran la conexión automáticamente
    // ------------------------------------------------------------------

    protected <T> List<T> consultarLista(String sql, Mapeador<T> mapeador, Object... parametros) {
        return ejecutar(con -> lista(con, sql, mapeador, parametros));
    }

    protected <T> Optional<T> consultarUno(String sql, Mapeador<T> mapeador, Object... parametros) {
        return ejecutar(con -> uno(con, sql, mapeador, parametros));
    }

    protected int ejecutarUpdate(String sql, Object... parametros) {
        return ejecutar(con -> update(con, sql, parametros));
    }

    /** Lee una columna INT que admite NULL. */
    protected static Integer getInteger(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }
}
