package lumina;

import lumina.conexion.Conexion;
import lumina.dao.DAOException;
import lumina.servicio.ReservaService;
import lumina.vista.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicación Lumina. */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            // Si falla se usa el aspecto por defecto de Swing
        }

        if (!Conexion.probarConexion()) {
            JOptionPane.showMessageDialog(null,
                    "No fue posible conectarse a la base de datos.\n"
                            + "Verifique que MySQL esté encendido y revise src/db.properties.",
                    "Lumina", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Al iniciar, las reservas que ya vencieron liberan sus ejemplares y licencias
        try {
            new ReservaService().marcarVencidas();
        } catch (DAOException e) {
            System.err.println("No se pudieron procesar las reservas vencidas: " + e.getMessage());
        }

        new LoginFrame().setVisible(true);
    }
}
