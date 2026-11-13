package lumina.vista;

import lumina.modelo.Persona;
import lumina.modelo.TipoPersona;
import lumina.servicio.AutenticacionService;
import lumina.servicio.ServicioException;

import javax.swing.JLabel;
import javax.swing.JPasswordField;
import java.awt.Component;

/** Permite al usuario que inició sesión cambiar su propia clave. */
public class CambiarClaveDialog extends DialogoFormulario {

    private static final long serialVersionUID = 1L;

    private final Persona usuario;
    private final JPasswordField actual = new JPasswordField(18);
    private final JPasswordField nueva = new JPasswordField(18);
    private final JPasswordField confirmacion = new JPasswordField(18);

    public CambiarClaveDialog(Component padre, Persona usuario) {
        super(padre, "Cambiar clave", "Cambiar");
        this.usuario = usuario;

        formulario.agregar("Clave actual:", actual);
        formulario.agregar("Clave nueva:", nueva);
        formulario.agregar("Confirmar:", confirmacion);
        formulario.agregarCompleto(new JLabel(usuario.getTipo() == TipoPersona.ESTUDIANTE
                ? "El PIN debe tener 4 dígitos."
                : "Mínimo 8 caracteres, con al menos una letra y un número."));
    }

    @Override
    protected void guardar() {
        String claveNueva = new String(nueva.getPassword());
        if (!claveNueva.equals(new String(confirmacion.getPassword()))) {
            throw new ServicioException("La clave nueva y su confirmación no coinciden");
        }
        new AutenticacionService().cambiarClave(usuario.getIdPersona(),
                new String(actual.getPassword()), claveNueva);
    }
}
