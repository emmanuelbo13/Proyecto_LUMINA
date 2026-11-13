package lumina.vista;

import lumina.modelo.Ejemplar;
import lumina.modelo.EstadoEjemplar;
import lumina.modelo.LibroFisico;
import lumina.servicio.MaterialService;

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

/** Copias físicas de un libro: agregar ejemplares y marcarlos dañados o disponibles. */
public class EjemplaresFrame extends JFrame {

    private MaterialesFrame ventanaMateriales;
    private LibroFisico libro;
    private JTable tabla;
    private DefaultTableModel modelo;
    private List<Ejemplar> ejemplares = new ArrayList<>();

    private MaterialService materialService = new MaterialService();

    public EjemplaresFrame(MaterialesFrame ventanaMateriales, LibroFisico libro) {
        this.ventanaMateriales = ventanaMateriales;
        this.libro = libro;
        setTitle("Ejemplares");
        setSize(600, 420);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel("Ejemplares de \"" + libro.getTitulo() + "\"");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBounds(20, 15, 550, 30);
        add(lblTitulo);

        String[] columnas = {"Número", "Estado", "Fecha de ingreso"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 55, 545, 240);
        add(scroll);

        JButton btnAgregar = new JButton("Agregar ejemplar");
        btnAgregar.setBounds(20, 310, 150, 35);
        add(btnAgregar);

        JButton btnDanado = new JButton("Marcar dañado");
        btnDanado.setBounds(180, 310, 140, 35);
        add(btnDanado);

        JButton btnDisponible = new JButton("Marcar disponible");
        btnDisponible.setBounds(330, 310, 150, 35);
        add(btnDisponible);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(490, 310, 75, 35);
        add(btnCerrar);

        btnAgregar.addActionListener(e -> agregarEjemplar());
        btnDanado.addActionListener(e -> cambiarEstado(EstadoEjemplar.DANADO));
        btnDisponible.addActionListener(e -> cambiarEstado(EstadoEjemplar.DISPONIBLE));
        btnCerrar.addActionListener(e -> cerrar());
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        try {
            ejemplares = materialService.listarEjemplares(libro.getIdMaterial());
            for (Ejemplar e : ejemplares) {
                modelo.addRow(new Object[]{e.getNumeroEjemplar(), e.getEstado(), e.getFechaIngreso()});
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarEjemplar() {
        try {
            Ejemplar nuevo = materialService.agregarEjemplar(libro.getIdMaterial());
            JOptionPane.showMessageDialog(this, "Se agregó el ejemplar número " + nuevo.getNumeroEjemplar());
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstado(EstadoEjemplar nuevoEstado) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un ejemplar de la tabla.");
            return;
        }

        Ejemplar ejemplar = ejemplares.get(fila);
        try {
            materialService.cambiarEstadoEjemplar(ejemplar.getIdMaterial(), ejemplar.getNumeroEjemplar(), nuevoEstado);
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Al cerrar se refresca la lista de materiales (cambia la disponibilidad). */
    private void cerrar() {
        ventanaMateriales.cargarTabla();
        dispose();
    }
}
