package lumina.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Proporciona conexiones JDBC a la base de datos Lumina (MySQL).
 *
 * Los datos de conexión se leen una sola vez del archivo db.properties
 * ubicado en la raíz del classpath (carpeta src). Cada llamada a
 * getConexion() entrega una conexión nueva, que el DAO que la pide debe
 * cerrar usando try-with-resources.
 */
public final class Conexion {

    private static final String ARCHIVO_CONFIG = "db.properties";

    private static final String URL;
    private static final String USUARIO;
    private static final String CLAVE;

    static {
        Properties props = new Properties();
        try (InputStream in = Conexion.class.getClassLoader().getResourceAsStream(ARCHIVO_CONFIG)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO_CONFIG + " en el classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO_CONFIG, e);
        }
        URL = props.getProperty("db.url");
        USUARIO = props.getProperty("db.usuario");
        CLAVE = props.getProperty("db.clave", "");
    }

    private Conexion() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Abre una nueva conexión a la base de datos.
     *
     * @return conexión abierta; quien la usa es responsable de cerrarla
     * @throws SQLException si no es posible conectarse
     */
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }

    /**
     * Verifica que la base de datos responda.
     *
     * @return true si se pudo abrir una conexión válida
     */
    public static boolean probarConexion() {
        try (Connection con = getConexion()) {
            return con.isValid(2);
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }
}
