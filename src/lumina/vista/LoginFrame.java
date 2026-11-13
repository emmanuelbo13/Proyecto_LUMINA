package lumina.vista;

import lumina.modelo.Persona;
import lumina.modelo.TipoDocumento;
import lumina.servicio.AutenticacionService;
import lumina.servicio.CatalogoService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Font;

/**
 * Ventana de inicio de sesión.
 * Según quién ingrese, el menú muestra opciones distintas.
 */
public class LoginFrame extends JFrame {

    private JComboBox<TipoDocumento> cbTipoDocumento;
    private JTextField txtNumero;
    private JPasswordField txtClave;
    private JButton btnIngresar;
    private JButton btnSalir;

    private AutenticacionService autenticacionService = new AutenticacionService();

    public LoginFrame() {
        setTitle("Lumina - Iniciar sesión");
        setSize(420, 330);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel("LUMINA", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setBounds(0, 20, 420, 35);
        add(lblTitulo);

        JLabel lblSubtitulo = new JLabel("Sistema de biblioteca multimedia", SwingConstants.CENTER);
        lblSubtitulo.setBounds(0, 55, 420, 20);
        add(lblSubtitulo);

        JLabel lblTipo = new JLabel("Tipo de documento:");
        lblTipo.setBounds(30, 100, 160, 30);
        add(lblTipo);

        cbTipoDocumento = new JComboBox<>();
        cbTipoDocumento.setBounds(190, 100, 190, 30);
        add(cbTipoDocumento);

        // Se llenan los tipos de documento desde la base de datos
        for (TipoDocumento tipo : new CatalogoService().tiposDocumento()) {
            cbTipoDocumento.addItem(tipo);
            if (tipo.getCodigo().equals("TI")) {
                cbTipoDocumento.setSelectedItem(tipo);
            }
        }

        JLabel lblNumero = new JLabel("Número de documento:");
        lblNumero.setBounds(30, 140, 160, 30);
        add(lblNumero);

        txtNumero = new JTextField();
        txtNumero.setBounds(190, 140, 190, 30);
        add(txtNumero);

        JLabel lblClave = new JLabel("Clave o PIN:");
        lblClave.setBounds(30, 180, 160, 30);
        add(lblClave);

        txtClave = new JPasswordField();
        txtClave.setBounds(190, 180, 190, 30);
        add(txtClave);

        btnIngresar = new JButton("Ingresar");
        btnIngresar.setBounds(90, 235, 110, 35);
        add(btnIngresar);

        btnSalir = new JButton("Salir");
        btnSalir.setBounds(220, 235, 110, 35);
        add(btnSalir);

        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> System.exit(0));

        // Al presionar Enter se oprime el botón Ingresar
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        TipoDocumento tipo = (TipoDocumento) cbTipoDocumento.getSelectedItem();
        String numero = txtNumero.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (numero.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el número de documento y la clave.");
            return;
        }

        try {
            Persona usuario = autenticacionService.iniciarSesion(tipo.getIdTipoDocumento(), numero, clave);
            new MenuFrame(usuario).setVisible(true);
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            txtClave.setText("");
        }
    }
}
