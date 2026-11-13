package lumina.vista;

import lumina.modelo.EstadoReserva;
import lumina.modelo.Estudiante;
import lumina.modelo.Reserva;
import lumina.servicio.ReservaService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** Reservas del estudiante que inició sesión. */
public class MisReservasFrame extends JFrame {

    private Estudiante estudiante;
    private JTable tabla;
    private DefaultTableModel modelo;
    private List<Reserva> reservas = new ArrayList<>();

    private ReservaService reservaService = new ReservaService();

    public MisReservasFrame(Estudiante estudiante) {
        this.estudiante = estudiante;
        setTitle("Mis reservas");
        setSize(800, 500);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel("Mis reservas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBounds(20, 15, 300, 30);
        add(lblTitulo);

        String[] columnas = {"#", "Material", "Ejemplar", "Fecha reserva", "Vence", "Estado"};
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
        int[] anchos = {40, 260, 90, 120, 120, 100};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 60, 745, 320);
        add(scroll);

        JButton btnCancelar = new JButton("Cancelar reserva");
        btnCancelar.setBounds(20, 395, 160, 35);
        add(btnCancelar);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setBounds(635, 395, 130, 35);
        add(btnVolver);

        btnCancelar.addActionListener(e -> cancelarReserva());
        btnVolver.addActionListener(e -> dispose());
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        try {
            reservas = reservaService.listarPorEstudiante(estudiante.getIdPersona());
            for (Reserva r : reservas) {
                String ejemplar = "Digital";
                if (r.getNumeroEjemplar() != null) {
                    ejemplar = "N° " + r.getNumeroEjemplar();
                }
                modelo.addRow(new Object[]{
                        r.getIdReserva(), r.getTituloMaterial(), ejemplar,
                        r.getFechaReserva().toLocalDate(), r.getFechaVencimiento(), r.getEstado()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelarReserva() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.");
            return;
        }

        Reserva reserva = reservas.get(fila);
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            JOptionPane.showMessageDialog(this, "Solo se pueden cancelar reservas activas.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Cancelar la reserva de \"" + reserva.getTituloMaterial() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservaService.cancelar(reserva.getIdReserva());
            JOptionPane.showMessageDialog(this, "Reserva cancelada.");
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
