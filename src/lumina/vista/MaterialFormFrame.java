package lumina.vista;

import lumina.modelo.Audiolibro;
import lumina.modelo.Categoria;
import lumina.modelo.Ebook;
import lumina.modelo.LibroFisico;
import lumina.modelo.Material;
import lumina.modelo.TipoMaterial;
import lumina.servicio.CatalogoService;
import lumina.servicio.MaterialService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Container;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Formulario para registrar o editar un material.
 * Hay un panel con los campos de cada tipo; solo se muestra el del tipo elegido.
 */
public class MaterialFormFrame extends JFrame {

    private MaterialesFrame ventanaMateriales;
    private Material material;   // null cuando es un material nuevo

    // Campos comunes
    private JComboBox<TipoMaterial> cbTipo;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtPaginas;
    private JTextField txtFecha;
    private JComboBox<Categoria> cbCategoria;

    // Libro físico
    private JPanel panelLibro;
    private JTextField txtIsbn;
    private JTextField txtUbicacion;
    private JTextField txtEjemplares;

    // Ebook
    private JPanel panelEbook;
    private JTextField txtFormatoArchivo;
    private JTextField txtTamano;
    private JTextField txtLicenciasEbook;

    // Audiolibro
    private JPanel panelAudio;
    private JTextField txtDuracion;
    private JTextField txtNarrador;
    private JTextField txtFormatoAudio;
    private JTextField txtLicenciasAudio;

    private MaterialService materialService = new MaterialService();

    public MaterialFormFrame(MaterialesFrame ventanaMateriales, Material material) {
        this.ventanaMateriales = ventanaMateriales;
        this.material = material;

        if (material == null) {
            setTitle("Nuevo material");
        } else {
            setTitle("Editar material");
        }
        setSize(480, 560);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        iniciarComponentes();
        if (material != null) {
            cargarDatos();
        }
        mostrarPanelDelTipo();
    }

    private void iniciarComponentes() {
        // ---------- Campos comunes ----------
        agregarEtiqueta(this, "Tipo:", 20);
        cbTipo = new JComboBox<>(TipoMaterial.values());
        cbTipo.setBounds(190, 20, 250, 30);
        add(cbTipo);

        agregarEtiqueta(this, "Título:", 60);
        txtTitulo = agregarCampo(this, 60);

        agregarEtiqueta(this, "Autor:", 100);
        txtAutor = agregarCampo(this, 100);

        agregarEtiqueta(this, "Número de páginas:", 140);
        txtPaginas = agregarCampo(this, 140);

        agregarEtiqueta(this, "Fecha (AAAA-MM-DD):", 180);
        txtFecha = agregarCampo(this, 180);

        agregarEtiqueta(this, "Categoría:", 220);
        cbCategoria = new JComboBox<>();
        cbCategoria.setBounds(190, 220, 250, 30);
        add(cbCategoria);
        for (Categoria c : new CatalogoService().categorias()) {
            cbCategoria.addItem(c);
        }

        // ---------- Panel del libro físico ----------
        panelLibro = crearPanel("Datos del libro físico");
        agregarEtiqueta(panelLibro, "ISBN (opcional):", 25);
        txtIsbn = agregarCampo(panelLibro, 25);
        agregarEtiqueta(panelLibro, "Ubicación en estante:", 65);
        txtUbicacion = agregarCampo(panelLibro, 65);
        agregarEtiqueta(panelLibro, "Ejemplares iniciales:", 105);
        txtEjemplares = agregarCampo(panelLibro, 105);
        txtEjemplares.setText("1");

        // ---------- Panel del ebook ----------
        panelEbook = crearPanel("Datos del ebook");
        agregarEtiqueta(panelEbook, "Formato (PDF, EPUB):", 25);
        txtFormatoArchivo = agregarCampo(panelEbook, 25);
        agregarEtiqueta(panelEbook, "Tamaño en MB:", 65);
        txtTamano = agregarCampo(panelEbook, 65);
        agregarEtiqueta(panelEbook, "Total de licencias:", 105);
        txtLicenciasEbook = agregarCampo(panelEbook, 105);

        // ---------- Panel del audiolibro ----------
        panelAudio = crearPanel("Datos del audiolibro");
        agregarEtiqueta(panelAudio, "Duración (minutos):", 25);
        txtDuracion = agregarCampo(panelAudio, 25);
        agregarEtiqueta(panelAudio, "Narrador:", 60);
        txtNarrador = agregarCampo(panelAudio, 60);
        agregarEtiqueta(panelAudio, "Formato (MP3):", 95);
        txtFormatoAudio = agregarCampo(panelAudio, 95);
        agregarEtiqueta(panelAudio, "Total de licencias:", 130);
        txtLicenciasAudio = agregarCampo(panelAudio, 130);

        // ---------- Botones ----------
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(120, 460, 110, 35);
        add(btnGuardar);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBounds(240, 460, 110, 35);
        add(btnCancelar);

        cbTipo.addActionListener(e -> mostrarPanelDelTipo());
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cerrar());
    }

    // ------------------------------------------------------------------
    // Métodos de ayuda para crear los componentes
    // ------------------------------------------------------------------

    private void agregarEtiqueta(Container contenedor, String texto, int y) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setBounds(20, y, 170, 30);
        contenedor.add(etiqueta);
    }

    private JTextField agregarCampo(Container contenedor, int y) {
        JTextField campo = new JTextField();
        campo.setBounds(190, y, 250, 30);
        contenedor.add(campo);
        return campo;
    }

    /** Los tres paneles ocupan el mismo lugar; solo uno queda visible. */
    private JPanel crearPanel(String titulo) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.setBounds(0, 265, 465, 175);
        add(panel);
        return panel;
    }

    private void mostrarPanelDelTipo() {
        TipoMaterial tipo = (TipoMaterial) cbTipo.getSelectedItem();
        panelLibro.setVisible(tipo == TipoMaterial.LIBRO);
        panelEbook.setVisible(tipo == TipoMaterial.EBOOK);
        panelAudio.setVisible(tipo == TipoMaterial.AUDIOLIBRO);
    }

    // ------------------------------------------------------------------
    // Cargar y guardar datos
    // ------------------------------------------------------------------

    /** Muestra en los campos los datos del material que se va a editar. */
    private void cargarDatos() {
        cbTipo.setSelectedItem(material.getTipo());
        cbTipo.setEnabled(false);   // el tipo no se puede cambiar
        txtTitulo.setText(material.getTitulo());
        txtAutor.setText(material.getAutor());
        txtPaginas.setText(String.valueOf(material.getNumPaginas()));
        if (material.getFechaPublicacion() != null) {
            txtFecha.setText(material.getFechaPublicacion().toString());
        }
        cbCategoria.setSelectedItem(material.getCategoria());

        if (material instanceof LibroFisico) {
            LibroFisico libro = (LibroFisico) material;
            txtIsbn.setText(libro.getIsbn());
            txtUbicacion.setText(libro.getUbicacionEstante());
            txtEjemplares.setEnabled(false);   // los ejemplares se agregan con el botón Ejemplares
        } else if (material instanceof Ebook) {
            Ebook ebook = (Ebook) material;
            txtFormatoArchivo.setText(ebook.getFormatoArchivo());
            txtTamano.setText(ebook.getTamanoMb().toString());
            txtLicenciasEbook.setText(String.valueOf(ebook.getTotalLicencias()));
        } else if (material instanceof Audiolibro) {
            Audiolibro audio = (Audiolibro) material;
            txtDuracion.setText(String.valueOf(audio.getDuracionMin()));
            txtNarrador.setText(audio.getNarrador());
            txtFormatoAudio.setText(audio.getFormatoAudio());
            txtLicenciasAudio.setText(String.valueOf(audio.getTotalLicencias()));
        }
    }

    private void guardar() {
        try {
            TipoMaterial tipo = (TipoMaterial) cbTipo.getSelectedItem();
            Material m;

            if (tipo == TipoMaterial.LIBRO) {
                LibroFisico libro;
                if (material == null) {
                    libro = new LibroFisico();
                } else {
                    libro = (LibroFisico) material;
                }
                libro.setIsbn(txtIsbn.getText());
                libro.setUbicacionEstante(txtUbicacion.getText());
                m = libro;

            } else if (tipo == TipoMaterial.EBOOK) {
                Ebook ebook;
                if (material == null) {
                    ebook = new Ebook();
                } else {
                    ebook = (Ebook) material;
                }
                ebook.setFormatoArchivo(txtFormatoArchivo.getText().trim().toUpperCase());
                ebook.setTamanoMb(leerDecimal(txtTamano, "tamaño en MB"));
                ebook.setTotalLicencias(leerEntero(txtLicenciasEbook, "total de licencias"));
                m = ebook;

            } else {
                Audiolibro audio;
                if (material == null) {
                    audio = new Audiolibro();
                } else {
                    audio = (Audiolibro) material;
                }
                audio.setDuracionMin(leerEntero(txtDuracion, "duración"));
                audio.setNarrador(txtNarrador.getText());
                audio.setFormatoAudio(txtFormatoAudio.getText().trim().toUpperCase());
                audio.setTotalLicencias(leerEntero(txtLicenciasAudio, "total de licencias"));
                m = audio;
            }

            m.setTitulo(txtTitulo.getText());
            m.setAutor(txtAutor.getText());
            m.setNumPaginas(leerEntero(txtPaginas, "número de páginas"));
            m.setFechaPublicacion(leerFecha());
            m.setCategoria((Categoria) cbCategoria.getSelectedItem());

            if (material == null) {
                int cantidadEjemplares = 0;
                if (tipo == TipoMaterial.LIBRO) {
                    cantidadEjemplares = leerEntero(txtEjemplares, "ejemplares iniciales");
                }
                materialService.registrar(m, cantidadEjemplares);
                JOptionPane.showMessageDialog(this, "Material registrado correctamente.");
            } else {
                materialService.actualizar(m);
                JOptionPane.showMessageDialog(this, "Material actualizado correctamente.");
            }
            cerrar();

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Cierra el formulario y refresca la lista de materiales. */
    private void cerrar() {
        ventanaMateriales.cargarTabla();
        dispose();
    }

    // ------------------------------------------------------------------
    // Conversión de los textos escritos por el usuario
    // ------------------------------------------------------------------

    private int leerEntero(JTextField campo, String nombre) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombre + " debe ser un número entero.");
        }
    }

    private BigDecimal leerDecimal(JTextField campo, String nombre) {
        try {
            return new BigDecimal(campo.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombre + " debe ser un número, por ejemplo 2.5");
        }
    }

    private LocalDate leerFecha() {
        String texto = txtFecha.getText().trim();
        if (texto.isEmpty()) {
            return null;   // la fecha de publicación es opcional
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener el formato AAAA-MM-DD, por ejemplo 2024-03-15");
        }
    }
}
