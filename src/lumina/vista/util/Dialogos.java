package lumina.vista.util;

import lumina.dao.DAOException;
import lumina.servicio.ServicioException;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.awt.Component;

/** Mensajes al usuario y manejo uniforme de errores en la interfaz. */
public final class Dialogos {

    private static final String TITULO = "Lumina";

    private Dialogos() {
    }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void aviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, TITULO, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, TITULO, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /**
     * Ejecuta una acción. Si se rompe una regla de negocio muestra el motivo;
     * si falla la base de datos muestra el error.
     *
     * @return true si la acción terminó sin errores
     */
    public static boolean ejecutar(Component padre, Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (ServicioException e) {
            aviso(padre, e.getMessage());
        } catch (DAOException e) {
            error(padre, "Error de base de datos:\n" + e.getMessage());
        }
        return false;
    }

    /** Pide una clave en un cuadro de diálogo. Devuelve null si el usuario cancela. */
    public static String pedirClave(Component padre, String mensaje) {
        JPasswordField campo = new JPasswordField(18);
        int opcion = JOptionPane.showConfirmDialog(padre, new Object[]{mensaje, campo}, TITULO,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opcion == JOptionPane.OK_OPTION ? new String(campo.getPassword()) : null;
    }
}
