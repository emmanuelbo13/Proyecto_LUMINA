package lumina.vista;

import lumina.modelo.Persona;
import lumina.modelo.TipoPersona;
import lumina.servicio.AutenticacionService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/** Ventana para que el usuario cambie su propia clave. */
public class CambiarClaveFrame extends JFrame {

    private Persona usuario;
    private JPasswordField txtActual;
    private JPasswordField txtNueva;
    private JPasswordField txtConfirmar;

    private AutenticacionService autenticacionService = new AutenticacionService();

    public CambiarClaveFrame(Persona usuario) {
        this.usuario = usuario;
        setTitle("Cambiar clave");
        setSize(400, 290);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        JLabel lblActual = new JLabel("Clave actual:");
        lblActual.setBounds(30, 25, 120, 30);
        add(lblActual);

        txtActual = new JPasswordField();
        txtActual.setBounds(160, 25, 190, 30);
        add(txtActual);

        JLabel lblNueva = new JLabel("Clave nueva:");
        lblNueva.setBounds(30, 65, 120, 30);
        add(lblNueva);

        txtNueva = new JPasswordField();
        txtNueva.setBounds(160, 65, 190, 30);
        add(txtNueva);

        JLabel lblConfirmar = new JLabel("Confirmar clave:");
        lblConfirmar.setBounds(30, 105, 120, 30);
        add(lblConfirmar);

        txtConfirmar = new JPasswordField();
        txtConfirmar.setBounds(160, 105, 190, 30);
        add(txtConfirmar);

        String regla;
        if (usuario.getTipo() == TipoPersona.ESTUDIANTE) {
            regla = "El PIN debe tener 4 dígitos.";
        } else {
            regla = "Mínimo 8 caracteres, con letras y números.";
        }
        JLabel lblRegla = new JLabel(regla);
        lblRegla.setBounds(30, 145, 330, 25);
        add(lblRegla);

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(80, 190, 110, 35);
        add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(200, 190, 110, 35);
        add(btnCancelar);

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void guardar() {
        String actual = new String(txtActual.getPassword());
        String nueva = new String(txtNueva.getPassword());
        String confirmar = new String(txtConfirmar.getPassword());

        if (!nueva.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "La clave nueva y la confirmación no coinciden.");
            return;
        }

        try {
            autenticacionService.cambiarClave(usuario.getIdPersona(), actual, nueva);
            JOptionPane.showMessageDialog(this, "La clave se cambió correctamente.");
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
