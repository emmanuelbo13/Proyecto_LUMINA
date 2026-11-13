package lumina.vista;

import lumina.modelo.EstadoMaterial;
import lumina.modelo.EstadoPersona;
import lumina.modelo.Estudiante;
import lumina.modelo.Material;
import lumina.modelo.Reserva;
import lumina.servicio.MaterialService;
import lumina.servicio.PersonaService;
import lumina.servicio.ReservaService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

/** El bibliotecario registra una reserva a nombre de un estudiante. */
public class NuevaReservaFrame extends JFrame {

    private ReservasFrame ventanaReservas;

    // Los combos muestran texto; las listas guardan el objeto de cada posición
    private JComboBox<String> cbEstudiante;
    private JComboBox<String> cbMaterial;
    private List<Estudiante> estudiantes = new ArrayList<>();
    private List<Material> materiales = new ArrayList<>();

    private ReservaService reservaService = new ReservaService();

    public NuevaReservaFrame(ReservasFrame ventanaReservas) {
        this.ventanaReservas = ventanaReservas;
        setTitle("Nueva reserva");
        setSize(560, 260);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        cargarCombos();
    }

    private void iniciarComponentes() {
        JLabel lblEstudiante = new JLabel("Estudiante:");
        lblEstudiante.setBounds(25, 25, 100, 30);
        add(lblEstudiante);

        cbEstudiante = new JComboBox<>();
        cbEstudiante.setBounds(125, 25, 395, 30);
        add(cbEstudiante);

        JLabel lblMaterial = new JLabel("Material:");
        lblMaterial.setBounds(25, 70, 100, 30);
        add(lblMaterial);

        cbMaterial = new JComboBox<>();
        cbMaterial.setBounds(125, 70, 395, 30);
        add(cbMaterial);

        JLabel lblNota = new JLabel("Solo aparecen materiales con unidades disponibles. La reserva vence en "
                + ReservaService.DIAS_VIGENCIA + " días.");
        lblNota.setBounds(25, 110, 500, 25);
        add(lblNota);

        JButton btnReservar = new JButton("Reservar");
        btnReservar.setBounds(160, 160, 110, 35);
        add(btnReservar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(280, 160, 110, 35);
        add(btnCancelar);

        btnReservar.addActionListener(e -> reservar());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void cargarCombos() {
        try {
            for (Estudiante e : new PersonaService().listarEstudiantes()) {
                if (e.getEstado() == EstadoPersona.ACTIVO) {
                    estudiantes.add(e);
                    cbEstudiante.addItem(e.getNombreCompleto() + " - " + e.getTipoDocumento().getCodigo()
                            + " " + e.getNumeroDocumento());
                }
            }

            for (Material m : new MaterialService().listar()) {
                if (m.getEstado() != EstadoMaterial.ACTIVO) {
                    continue;
                }
                int disponibles = reservaService.consultarDisponibilidad(m.getIdMaterial());
                if (disponibles > 0) {
                    materiales.add(m);
                    cbMaterial.addItem(m.getTitulo() + " (" + m.getTipo() + ") - disponibles: " + disponibles);
                }
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reservar() {
        int posEstudiante = cbEstudiante.getSelectedIndex();
        int posMaterial = cbMaterial.getSelectedIndex();
        if (posEstudiante == -1 || posMaterial == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione el estudiante y el material.");
            return;
        }

        Estudiante estudiante = estudiantes.get(posEstudiante);
        Material material = materiales.get(posMaterial);

        try {
            Reserva reserva = reservaService.reservar(estudiante.getIdPersona(), material.getIdMaterial());
            JOptionPane.showMessageDialog(this, "Reserva #" + reserva.getIdReserva() + " creada para "
                    + estudiante.getNombreCompleto() + ".\nVence el " + reserva.getFechaVencimiento() + ".");
            ventanaReservas.cargarTabla();
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
