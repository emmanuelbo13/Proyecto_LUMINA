package lumina.vista;

import lumina.modelo.EstadoPersona;
import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Persona;
import lumina.modelo.TipoPersona;
import lumina.servicio.PersonaService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Lista de personas de un tipo:
 *  - ESTUDIANTE:  la usa el bibliotecario.
 *  - FUNCIONARIO: la usa el administrador.
 */
public class PersonasFrame extends JFrame {

    private TipoPersona tipo;
    private Persona usuarioActual;
    private JTable tabla;
    private DefaultTableModel modelo;
    private List<Persona> personas = new ArrayList<>();

    private PersonaService personaService = new PersonaService();

    public PersonasFrame(TipoPersona tipo, Persona usuarioActual) {
        this.tipo = tipo;
        this.usuarioActual = usuarioActual;
        if (tipo == TipoPersona.ESTUDIANTE) {
            setTitle("Estudiantes");
        } else {
            setTitle("Funcionarios");
        }
        setSize(800, 500);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel(getTitle());
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBounds(20, 15, 300, 30);
        add(lblTitulo);

        String columnaExtra;
        if (tipo == TipoPersona.ESTUDIANTE) {
            columnaExtra = "Grado";
        } else {
            columnaExtra = "Rol";
        }
        String[] columnas = {"ID", "Documento", "Nombres", "Apellidos", columnaExtra, "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);

        // Ancho de cada columna (en píxeles)
        int[] anchos = {40, 130, 150, 170, 130, 90};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 60, 745, 320);
        add(scroll);

        JButton btnNuevo = new JButton("Nuevo");
        btnNuevo.setBounds(20, 395, 100, 35);
        add(btnNuevo);

        JButton btnEditar = new JButton("Editar");
        btnEditar.setBounds(130, 395, 100, 35);
        add(btnEditar);

        JButton btnEstado = new JButton("Activar / Inactivar");
        btnEstado.setBounds(240, 395, 160, 35);
        add(btnEstado);

        JButton btnClave = new JButton("Restablecer clave");
        btnClave.setBounds(410, 395, 160, 35);
        add(btnClave);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setBounds(665, 395, 100, 35);
        add(btnVolver);

        btnNuevo.addActionListener(e -> new PersonaFormFrame(this, tipo, null).setVisible(true));
        btnEditar.addActionListener(e -> editar());
        btnEstado.addActionListener(e -> cambiarEstado());
        btnClave.addActionListener(e -> restablecerClave());
        btnVolver.addActionListener(e -> dispose());
    }

    /** Público para que el formulario refresque la lista al guardar. */
    public void cargarTabla() {
        modelo.setRowCount(0);
        personas.clear();
        try {
            if (tipo == TipoPersona.ESTUDIANTE) {
                for (Estudiante e : personaService.listarEstudiantes()) {
                    personas.add(e);
                    modelo.addRow(new Object[]{
                            e.getIdPersona(), documento(e), e.getNombres(), e.getApellidos(),
                            e.getGrado().getNombre(), e.getEstado()
                    });
                }
            } else {
                for (Funcionario f : personaService.listarFuncionarios()) {
                    personas.add(f);
                    modelo.addRow(new Object[]{
                            f.getIdPersona(), documento(f), f.getNombres(), f.getApellidos(),
                            f.getRol().getNombre(), f.getEstado()
                    });
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String documento(Persona p) {
        return p.getTipoDocumento().getCodigo() + " " + p.getNumeroDocumento();
    }

    /** Devuelve la persona seleccionada o null si no hay selección. */
    private Persona personaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una persona de la tabla.");
            return null;
        }
        return personas.get(fila);
    }

    private void editar() {
        Persona persona = personaSeleccionada();
        if (persona != null) {
            new PersonaFormFrame(this, tipo, persona).setVisible(true);
        }
    }

    private void cambiarEstado() {
        Persona persona = personaSeleccionada();
        if (persona == null) {
            return;
        }
        if (persona.getIdPersona() == usuarioActual.getIdPersona()) {
            JOptionPane.showMessageDialog(this, "No puede inactivar su propia cuenta.");
            return;
        }

        EstadoPersona nuevoEstado;
        if (persona.getEstado() == EstadoPersona.ACTIVO) {
            nuevoEstado = EstadoPersona.INACTIVO;
        } else {
            nuevoEstado = EstadoPersona.ACTIVO;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Cambiar el estado de " + persona.getNombreCompleto() + " a " + nuevoEstado + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            personaService.cambiarEstado(persona.getIdPersona(), nuevoEstado);
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void restablecerClave() {
        Persona persona = personaSeleccionada();
        if (persona == null) {
            return;
        }

        String regla;
        if (tipo == TipoPersona.ESTUDIANTE) {
            regla = "PIN de 4 dígitos";
        } else {
            regla = "mínimo 8 caracteres, con letras y números";
        }

        // Cuadro de diálogo con un campo de clave (no muestra lo que se escribe)
        JPasswordField txtClave = new JPasswordField();
        Object[] contenido = {"Nueva clave para " + persona.getNombreCompleto() + " (" + regla + "):", txtClave};
        int opcion = JOptionPane.showConfirmDialog(this, contenido, "Restablecer clave",
                JOptionPane.OK_CANCEL_OPTION);
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            personaService.restablecerClave(persona.getIdPersona(), new String(txtClave.getPassword()));
            JOptionPane.showMessageDialog(this, "La clave se actualizó correctamente.");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
