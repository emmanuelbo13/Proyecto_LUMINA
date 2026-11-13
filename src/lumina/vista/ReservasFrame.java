package lumina.vista;

import lumina.modelo.EstadoReserva;
import lumina.modelo.Reserva;
import lumina.servicio.ReservaService;

import javax.swing.JButton;
import javax.swing.JComboBox;
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

/** El bibliotecario consulta y gestiona las reservas. */
public class ReservasFrame extends JFrame {

    private JComboBox<String> cbMostrar;
    private JTable tabla;
    private DefaultTableModel modelo;
    private List<Reserva> reservas = new ArrayList<>();

    private ReservaService reservaService = new ReservaService();

    public ReservasFrame() {
        setTitle("Reservas");
        setSize(800, 500);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel("Reservas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBounds(20, 15, 300, 30);
        add(lblTitulo);

        JLabel lblMostrar = new JLabel("Mostrar:");
        lblMostrar.setBounds(530, 15, 60, 30);
        add(lblMostrar);

        cbMostrar = new JComboBox<>(new String[]{"Activas", "Todas"});
        cbMostrar.setBounds(595, 15, 170, 30);
        add(cbMostrar);

        String[] columnas = {"#", "Estudiante", "Material", "Ejemplar", "Fecha reserva", "Vence", "Estado"};
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
        int[] anchos = {40, 175, 175, 70, 100, 100, 85};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 60, 745, 320);
        add(scroll);

        JButton btnNueva = new JButton("Nueva reserva");
        btnNueva.setBounds(20, 395, 140, 35);
        add(btnNueva);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(170, 395, 110, 35);
        add(btnCancelar);

        JButton btnFinalizar = new JButton("Finalizar");
        btnFinalizar.setBounds(290, 395, 110, 35);
        add(btnFinalizar);

        JButton btnVencidas = new JButton("Procesar vencidas");
        btnVencidas.setBounds(410, 395, 160, 35);
        add(btnVencidas);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setBounds(665, 395, 100, 35);
        add(btnVolver);

        cbMostrar.addActionListener(e -> cargarTabla());
        btnNueva.addActionListener(e -> new NuevaReservaFrame(this).setVisible(true));
        btnCancelar.addActionListener(e -> cancelar());
        btnFinalizar.addActionListener(e -> finalizar());
        btnVencidas.addActionListener(e -> procesarVencidas());
        btnVolver.addActionListener(e -> dispose());
    }

    /** Público para que la ventana de nueva reserva refresque la lista. */
    public void cargarTabla() {
        modelo.setRowCount(0);
        try {
            if (cbMostrar.getSelectedItem().equals("Activas")) {
                reservas = reservaService.listarActivas();
            } else {
                reservas = reservaService.listarTodas();
            }

            for (Reserva r : reservas) {
                String ejemplar = "Digital";
                if (r.getNumeroEjemplar() != null) {
                    ejemplar = "N° " + r.getNumeroEjemplar();
                }
                modelo.addRow(new Object[]{
                        r.getIdReserva(), r.getNombreEstudiante(), r.getTituloMaterial(), ejemplar,
                        r.getFechaReserva().toLocalDate(), r.getFechaVencimiento(), r.getEstado()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Devuelve la reserva seleccionada si está activa; si no, muestra un aviso y devuelve null. */
    private Reserva reservaActivaSeleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una reserva de la tabla.");
            return null;
        }
        Reserva reserva = reservas.get(fila);
        if (reserva.getEstado() != EstadoReserva.ACTIVA) {
            JOptionPane.showMessageDialog(this, "La reserva seleccionada no está activa.");
            return null;
        }
        return reserva;
    }

    private void cancelar() {
        Reserva reserva = reservaActivaSeleccionada();
        if (reserva == null) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Cancelar la reserva #" + reserva.getIdReserva() + " de " + reserva.getNombreEstudiante() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            reservaService.cancelar(reserva.getIdReserva());
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** El material fue devuelto. Si es un libro se pregunta si llegó dañado. */
    private void finalizar() {
        Reserva reserva = reservaActivaSeleccionada();
        if (reserva == null) {
            return;
        }

        boolean danado = false;
        if (reserva.getNumeroEjemplar() != null) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿El ejemplar de \"" + reserva.getTituloMaterial() + "\" regresó dañado?",
                    "Finalizar reserva", JOptionPane.YES_NO_CANCEL_OPTION);
            if (respuesta == JOptionPane.CANCEL_OPTION || respuesta == JOptionPane.CLOSED_OPTION) {
                return;
            }
            danado = (respuesta == JOptionPane.YES_OPTION);
        } else {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Finalizar la reserva #" + reserva.getIdReserva() + "?",
                    "Finalizar reserva", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            reservaService.finalizar(reserva.getIdReserva(), danado);
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void procesarVencidas() {
        try {
            int cantidad = reservaService.marcarVencidas();
            if (cantidad == 0) {
                JOptionPane.showMessageDialog(this, "No hay reservas vencidas.");
            } else {
                JOptionPane.showMessageDialog(this, "Se marcaron " + cantidad + " reserva(s) como vencidas.");
            }
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
