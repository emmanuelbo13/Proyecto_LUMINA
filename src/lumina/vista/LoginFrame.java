package lumina.vista;

import lumina.modelo.Persona;
import lumina.modelo.TipoDocumento;
import lumina.servicio.AutenticacionService;
import lumina.servicio.CatalogoService;
import lumina.vista.util.Dialogos;
import lumina.vista.util.Formulario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/** Pantalla de inicio de sesión. */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AutenticacionService autenticacion = new AutenticacionService();
    private final JComboBox<TipoDocumento> tipoDocumento;
    private final JTextField numero = new JTextField(18);
    private final JPasswordField clave = new JPasswordField(18);

    public LoginFrame() {
        super("Lumina - Iniciar sesión");

        tipoDocumento = Formulario.combo(new CatalogoService().tiposDocumento());
        Formulario.mostrarCon(tipoDocumento, TipoDocumento::toString);
        // TI es el documento más común entre los estudiantes
        Formulario.seleccionar(tipoDocumento, td -> "TI".equals(td.getCodigo()));

        JLabel titulo = new JLabel("Lumina", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 34f));
        titulo.setForeground(new Color(0x1565C0));
        JLabel subtitulo = new JLabel("Sistema de gestión de biblioteca multimedia", SwingConstants.CENTER);

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 4));
        encabezado.setBorder(BorderFactory.createEmptyBorder(24, 24, 4, 24));
        encabezado.add(titulo);
        encabezado.add(subtitulo);

        Formulario formulario = new Formulario();
        formulario.agregar("Tipo de documento:", tipoDocumento);
        formulario.agregar("Número de documento:", numero);
        formulario.agregar("Clave o PIN:", clave);

        JButton ingresar = new JButton("Ingresar");
        JButton salir = new JButton("Salir");
        ingresar.addActionListener(e -> ingresar());
        salir.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(ingresar);

        setLayout(new BorderLayout());
        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(Formulario.botonera(ingresar, salir), BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        boolean correcto = Dialogos.ejecutar(this, () -> {
            TipoDocumento td = (TipoDocumento) tipoDocumento.getSelectedItem();
            Persona usuario = autenticacion.iniciarSesion(td.getIdTipoDocumento(),
                    numero.getText(), new String(clave.getPassword()));
            dispose();
            new PrincipalFrame(usuario).setVisible(true);
        });
        if (!correcto) {
            clave.setText("");
            clave.requestFocusInWindow();
        }
    }
}
