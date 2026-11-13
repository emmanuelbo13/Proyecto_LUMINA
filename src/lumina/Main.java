package lumina;

import lumina.conexion.Conexion;
import lumina.servicio.ReservaService;
import lumina.vista.LoginFrame;

import javax.swing.JOptionPane;

/** Punto de entrada de la aplicación Lumina. */
public class Main {

    public static void main(String[] args) {
        // 1. Verificar que la base de datos responda
        if (!Conexion.probarConexion()) {
            JOptionPane.showMessageDialog(null,
                    "No fue posible conectarse a la base de datos.\n"
                            + "Verifique que MySQL esté encendido y revise src/db.properties.",
                    "Lumina", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Las reservas que ya vencieron liberan sus ejemplares y licencias
        try {
            new ReservaService().marcarVencidas();
        } catch (RuntimeException e) {
            System.out.println("No se pudieron procesar las reservas vencidas: " + e.getMessage());
        }

        // 3. Mostrar la ventana de inicio de sesión
        new LoginFrame().setVisible(true);
    }
}
