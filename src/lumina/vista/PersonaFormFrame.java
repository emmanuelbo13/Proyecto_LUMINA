package lumina.vista;

import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Grado;
import lumina.modelo.Persona;
import lumina.modelo.Rol;
import lumina.modelo.TipoDocumento;
import lumina.modelo.TipoPersona;
import lumina.servicio.CatalogoService;
import lumina.servicio.PersonaService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Formulario para registrar o editar un estudiante o un funcionario. */
public class PersonaFormFrame extends JFrame {

    private PersonasFrame ventanaPersonas;
    private TipoPersona tipo;
    private Persona persona;   // null cuando es una persona nueva

    private JComboBox<TipoDocumento> cbTipoDocumento;
    private JTextField txtNumero;
    private JTextField txtNombres;
    private JTextField txtApellidos;
    private JComboBox<Grado> cbGrado;   // solo para estudiantes
    private JComboBox<Rol> cbRol;       // solo para funcionarios
    private JPasswordField txtClave;
    private JPasswordField txtConfirmar;

    private PersonaService personaService = new PersonaService();
    private CatalogoService catalogoService = new CatalogoService();

    public PersonaFormFrame(PersonasFrame ventanaPersonas, TipoPersona tipo, Persona persona) {
        this.ventanaPersonas = ventanaPersonas;
        this.tipo = tipo;
        this.persona = persona;

        String nombreTipo;
        if (tipo == TipoPersona.ESTUDIANTE) {
            nombreTipo = "estudiante";
        } else {
            nombreTipo = "funcionario";
        }
        if (persona == null) {
            setTitle("Nuevo " + nombreTipo);
        } else {
            setTitle("Editar " + nombreTipo);
        }

        setSize(450, 420);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        if (persona != null) {
            cargarDatos();
        }
    }

    private void iniciarComponentes() {
        agregarEtiqueta("Tipo de documento:", 20);
        cbTipoDocumento = new JComboBox<>();
        cbTipoDocumento.setBounds(180, 20, 230, 30);
        add(cbTipoDocumento);
        for (TipoDocumento td : catalogoService.tiposDocumento()) {
            cbTipoDocumento.addItem(td);
            // Sugerencia: TI para estudiantes, CC para funcionarios
            if (tipo == TipoPersona.ESTUDIANTE && td.getCodigo().equals("TI")) {
                cbTipoDocumento.setSelectedItem(td);
            }
            if (tipo == TipoPersona.FUNCIONARIO && td.getCodigo().equals("CC")) {
                cbTipoDocumento.setSelectedItem(td);
            }
        }

        agregarEtiqueta("Número de documento:", 60);
        txtNumero = agregarCampo(60);

        agregarEtiqueta("Nombres:", 100);
        txtNombres = agregarCampo(100);

        agregarEtiqueta("Apellidos:", 140);
        txtApellidos = agregarCampo(140);

        if (tipo == TipoPersona.ESTUDIANTE) {
            agregarEtiqueta("Grado:", 180);
            cbGrado = new JComboBox<>();
            cbGrado.setBounds(180, 180, 230, 30);
            add(cbGrado);
            for (Grado g : catalogoService.grados()) {
                cbGrado.addItem(g);
            }
        } else {
            agregarEtiqueta("Rol:", 180);
            cbRol = new JComboBox<>();
            cbRol.setBounds(180, 180, 230, 30);
            add(cbRol);
            for (Rol r : catalogoService.roles()) {
                cbRol.addItem(r);
            }
        }

        // La clave solo se pide al registrar; para cambiarla está "Restablecer clave"
        if (persona == null) {
            if (tipo == TipoPersona.ESTUDIANTE) {
                agregarEtiqueta("PIN (4 dígitos):", 220);
            } else {
                agregarEtiqueta("Clave:", 220);
            }
            txtClave = new JPasswordField();
            txtClave.setBounds(180, 220, 230, 30);
            add(txtClave);

            agregarEtiqueta("Confirmar:", 260);
            txtConfirmar = new JPasswordField();
            txtConfirmar.setBounds(180, 260, 230, 30);
            add(txtConfirmar);
        }

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(100, 320, 110, 35);
        add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(220, 320, 110, 35);
        add(btnCancelar);

        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cerrar());
    }

    private void agregarEtiqueta(String texto, int y) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setBounds(25, y, 150, 30);
        add(etiqueta);
    }

    private JTextField agregarCampo(int y) {
        JTextField campo = new JTextField();
        campo.setBounds(180, y, 230, 30);
        add(campo);
        return campo;
    }

    /** Muestra en los campos los datos de la persona que se va a editar. */
    private void cargarDatos() {
        cbTipoDocumento.setSelectedItem(persona.getTipoDocumento());
        txtNumero.setText(persona.getNumeroDocumento());
        txtNombres.setText(persona.getNombres());
        txtApellidos.setText(persona.getApellidos());

        if (persona instanceof Estudiante) {
            cbGrado.setSelectedItem(((Estudiante) persona).getGrado());
        } else {
            cbRol.setSelectedItem(((Funcionario) persona).getRol());
        }
    }

    private void guardar() {
        try {
            Persona p = persona;
            if (p == null) {
                if (tipo == TipoPersona.ESTUDIANTE) {
                    p = new Estudiante();
                } else {
                    p = new Funcionario();
                }
            }

            p.setTipoDocumento((TipoDocumento) cbTipoDocumento.getSelectedItem());
            p.setNumeroDocumento(txtNumero.getText());
            p.setNombres(txtNombres.getText());
            p.setApellidos(txtApellidos.getText());

            if (p instanceof Estudiante) {
                ((Estudiante) p).setGrado((Grado) cbGrado.getSelectedItem());
            } else {
                ((Funcionario) p).setRol((Rol) cbRol.getSelectedItem());
            }

            if (persona == null) {
                String clave = new String(txtClave.getPassword());
                String confirmar = new String(txtConfirmar.getPassword());
                if (!clave.equals(confirmar)) {
                    JOptionPane.showMessageDialog(this, "La clave y la confirmación no coinciden.");
                    return;
                }
                personaService.registrar(p, clave);
                JOptionPane.showMessageDialog(this, "Registro guardado correctamente.");
            } else {
                personaService.actualizar(p);
                JOptionPane.showMessageDialog(this, "Datos actualizados correctamente.");
            }
            cerrar();

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Cierra el formulario y refresca la lista de personas. */
    private void cerrar() {
        ventanaPersonas.cargarTabla();
        dispose();
    }
}
