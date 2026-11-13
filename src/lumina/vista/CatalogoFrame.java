package lumina.vista;

import lumina.modelo.EstadoMaterial;
import lumina.modelo.Estudiante;
import lumina.modelo.Material;
import lumina.modelo.Reserva;
import lumina.servicio.MaterialService;
import lumina.servicio.ReservaService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** El estudiante consulta los materiales activos y hace reservas. */
public class CatalogoFrame extends JFrame {

    private Estudiante estudiante;
    private JTextField txtBuscar;
    private JTable tabla;
    private DefaultTableModel modelo;

    // Materiales en el mismo orden que las filas de la tabla
    private List<Material> materiales = new ArrayList<>();

    private MaterialService materialService = new MaterialService();
    private ReservaService reservaService = new ReservaService();

    public CatalogoFrame(Estudiante estudiante) {
        this.estudiante = estudiante;
        setTitle("Catálogo de materiales");
        setSize(800, 500);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel("Catálogo de materiales");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBounds(20, 15, 300, 30);
        add(lblTitulo);

        JLabel lblBuscar = new JLabel("Título o autor:");
        lblBuscar.setBounds(390, 15, 100, 30);
        add(lblBuscar);

        txtBuscar = new JTextField();
        txtBuscar.setBounds(490, 15, 170, 30);
        add(txtBuscar);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.setBounds(670, 15, 95, 30);
        add(btnBuscar);

        String[] columnas = {"ID", "Título", "Autor", "Tipo", "Categoría", "Disponibles"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;   // la tabla es solo de consulta
            }
        };
        tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);

        // Ancho de cada columna (en píxeles)
        int[] anchos = {40, 230, 190, 90, 115, 80};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBounds(20, 60, 745, 320);
        add(scroll);

        JButton btnReservar = new JButton("Reservar");
        btnReservar.setBounds(20, 395, 130, 35);
        add(btnReservar);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setBounds(635, 395, 130, 35);
        add(btnVolver);

        btnBuscar.addActionListener(e -> cargarTabla());
        btnReservar.addActionListener(e -> reservar());
        btnVolver.addActionListener(e -> dispose());
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        materiales.clear();
        String texto = txtBuscar.getText().trim().toLowerCase();

        try {
            for (Material m : materialService.listar()) {
                // El estudiante solo ve materiales activos
                if (m.getEstado() != EstadoMaterial.ACTIVO) {
                    continue;
                }
                // Filtro de búsqueda por título o autor
                if (!texto.isEmpty()
                        && !m.getTitulo().toLowerCase().contains(texto)
                        && !m.getAutor().toLowerCase().contains(texto)) {
                    continue;
                }
                int disponibles = reservaService.consultarDisponibilidad(m.getIdMaterial());
                materiales.add(m);
                modelo.addRow(new Object[]{
                        m.getIdMaterial(), m.getTitulo(), m.getAutor(), m.getTipo(),
                        m.getCategoria().getNombre(), disponibles
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reservar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un material de la tabla.");
            return;
        }

        Material material = materiales.get(fila);
        int disponibles = (int) modelo.getValueAt(fila, 5);
        if (disponibles == 0) {
            JOptionPane.showMessageDialog(this, "No hay unidades disponibles de este material.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea reservar \"" + material.getTitulo() + "\"?\nLa reserva vence en "
                        + ReservaService.DIAS_VIGENCIA + " días.",
                "Confirmar reserva", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Reserva reserva = reservaService.reservar(estudiante.getIdPersona(), material.getIdMaterial());
            String mensaje = "Reserva #" + reserva.getIdReserva() + " creada.\n";
            if (reserva.getNumeroEjemplar() != null) {
                mensaje = mensaje + "Recoja el ejemplar " + reserva.getNumeroEjemplar() + " en la biblioteca.\n";
            } else {
                mensaje = mensaje + "Ya puede acceder al material digital.\n";
            }
            mensaje = mensaje + "Vence el " + reserva.getFechaVencimiento();
            JOptionPane.showMessageDialog(this, mensaje);
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
