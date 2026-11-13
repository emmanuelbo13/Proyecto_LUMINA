package lumina.vista;

import lumina.modelo.EstadoMaterial;
import lumina.modelo.LibroFisico;
import lumina.modelo.Material;
import lumina.servicio.MaterialService;
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

/** El bibliotecario administra los materiales de la biblioteca. */
public class MaterialesFrame extends JFrame {

    private JTable tabla;
    private DefaultTableModel modelo;
    private List<Material> materiales = new ArrayList<>();

    private MaterialService materialService = new MaterialService();
    private ReservaService reservaService = new ReservaService();

    public MaterialesFrame() {
        setTitle("Materiales");
        setSize(800, 500);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        iniciarComponentes();
        cargarTabla();
    }

    private void iniciarComponentes() {
        JLabel lblTitulo = new JLabel("Materiales");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBounds(20, 15, 300, 30);
        add(lblTitulo);

        String[] columnas = {"ID", "Tipo", "Título", "Autor", "Categoría", "Disponibles", "Estado"};
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
        int[] anchos = {40, 90, 200, 170, 120, 80, 80};
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

        JButton btnEjemplares = new JButton("Ejemplares");
        btnEjemplares.setBounds(240, 395, 120, 35);
        add(btnEjemplares);

        JButton btnEstado = new JButton("Retirar / Reactivar");
        btnEstado.setBounds(370, 395, 170, 35);
        add(btnEstado);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setBounds(665, 395, 100, 35);
        add(btnVolver);

        btnNuevo.addActionListener(e -> new MaterialFormFrame(this, null).setVisible(true));
        btnEditar.addActionListener(e -> editar());
        btnEjemplares.addActionListener(e -> verEjemplares());
        btnEstado.addActionListener(e -> cambiarEstado());
        btnVolver.addActionListener(e -> dispose());
    }

    /** Público para que el formulario y la ventana de ejemplares refresquen la lista. */
    public void cargarTabla() {
        modelo.setRowCount(0);
        try {
            materiales = materialService.listar();
            for (Material m : materiales) {
                int disponibles = reservaService.consultarDisponibilidad(m.getIdMaterial());
                modelo.addRow(new Object[]{
                        m.getIdMaterial(), m.getTipo(), m.getTitulo(), m.getAutor(),
                        m.getCategoria().getNombre(), disponibles, m.getEstado()
                });
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Devuelve el material seleccionado o null si no hay selección. */
    private Material materialSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un material de la tabla.");
            return null;
        }
        return materiales.get(fila);
    }

    private void editar() {
        Material material = materialSeleccionado();
        if (material != null) {
            new MaterialFormFrame(this, material).setVisible(true);
        }
    }

    private void verEjemplares() {
        Material material = materialSeleccionado();
        if (material == null) {
            return;
        }
        if (material instanceof LibroFisico) {
            new EjemplaresFrame(this, (LibroFisico) material).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Solo los libros físicos tienen ejemplares.\n"
                            + "Los ebooks y audiolibros se manejan con licencias (botón Editar).");
        }
    }

    private void cambiarEstado() {
        Material material = materialSeleccionado();
        if (material == null) {
            return;
        }

        EstadoMaterial nuevoEstado;
        String accion;
        if (material.getEstado() == EstadoMaterial.ACTIVO) {
            nuevoEstado = EstadoMaterial.RETIRADO;
            accion = "retirar";
        } else {
            nuevoEstado = EstadoMaterial.ACTIVO;
            accion = "reactivar";
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Desea " + accion + " \"" + material.getTitulo() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            materialService.cambiarEstado(material.getIdMaterial(), nuevoEstado);
            cargarTabla();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
