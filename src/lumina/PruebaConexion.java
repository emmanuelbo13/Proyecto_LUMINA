package lumina;

import lumina.conexion.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Prueba manual de la conexión: se conecta a Lumina y lista los roles.
 */
public class PruebaConexion {

    public static void main(String[] args) {
        String sql = "SELECT id_rol, nombre, descripcion FROM rol ORDER BY id_rol";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("Conectado a: " + con.getMetaData().getURL());
            System.out.println("Roles registrados:");
            while (rs.next()) {
                System.out.printf("  %d - %s: %s%n",
                        rs.getInt("id_rol"), rs.getString("nombre"), rs.getString("descripcion"));
            }
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a la base de datos: " + e.getMessage());
        }
    }
}
