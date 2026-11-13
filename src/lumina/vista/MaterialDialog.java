package lumina.vista;

import lumina.modelo.Audiolibro;
import lumina.modelo.Categoria;
import lumina.modelo.Ebook;
import lumina.modelo.LibroFisico;
import lumina.modelo.Material;
import lumina.modelo.TipoMaterial;
import lumina.servicio.CatalogoService;
import lumina.servicio.MaterialService;
import lumina.vista.util.Formatos;
import lumina.vista.util.Formulario;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.awt.CardLayout;
import java.awt.Component;

/**
 * Formulario para crear o editar un material. Los campos propios de cada
 * tipo (libro, ebook, audiolibro) se muestran según el tipo elegido.
 */
public class MaterialDialog extends DialogoFormulario {

    private static final long serialVersionUID = 1L;

    private final Material existente;
    private final MaterialService servicio = new MaterialService();

    // Datos comunes
    private final JComboBox<TipoMaterial> tipo = new JComboBox<>(TipoMaterial.values());
    private final JTextField titulo = new JTextField(30);
    private final JTextField autor = new JTextField(30);
    private final JSpinner paginas = Formulario.spinnerEntero(100, 1, 100_000);
    private final JTextField fechaPublicacion = new JTextField(10);
    private final JComboBox<Categoria> categoria = Formulario.combo(new CatalogoService().categorias());

    // Libro físico
    private final JTextField isbn = new JTextField(17);
    private final JTextField ubicacion = new JTextField(10);
    private final JSpinner ejemplares = Formulario.spinnerEntero(1, 1, 100);

    // Ebook
    private final JComboBox<String> formatoArchivo = Formulario.comboEditable("PDF", "EPUB", "MOBI");
    private final JSpinner tamanoMb = Formulario.spinnerDecimal(1.0, 0.01, 10_000, 0.5);
    private final JSpinner licenciasEbook = Formulario.spinnerEntero(1, 1, 1_000);

    // Audiolibro
    private final JSpinner duracion = Formulario.spinnerEntero(60, 1, 100_000);
    private final JTextField narrador = new JTextField(20);
    private final JComboBox<String> formatoAudio = Formulario.comboEditable("MP3", "M4B", "AAC", "WAV");
    private final JSpinner licenciasAudio = Formulario.spinnerEntero(1, 1, 1_000);

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel detalle = new JPanel(tarjetas);

    /** @param existente material a editar, o null para registrar uno nuevo */
    public MaterialDialog(Component padre, Material existente) {
        super(padre, existente == null ? "Nuevo material" : "Editar material", "Guardar");
        this.existente = existente;

        formulario.agregar("Tipo:", tipo);
        formulario.agregar("Título:", titulo);
        formulario.agregar("Autor:", autor);
        formulario.agregar("Número de páginas:", paginas);
        formulario.agregar("Publicación (AAAA-MM-DD):", fechaPublicacion);
        formulario.agregar("Categoría:", categoria);

        Formulario libro = new Formulario("Datos del libro físico");
        libro.agregar("ISBN (opcional):", isbn);
        libro.agregar("Ubicación en estante:", ubicacion);
        libro.agregar("Ejemplares iniciales:", ejemplares);

        Formulario ebook = new Formulario("Datos del ebook");
        ebook.agregar("Formato de archivo:", formatoArchivo);
        ebook.agregar("Tamaño (MB):", tamanoMb);
        ebook.agregar("Total de licencias:", licenciasEbook);

        Formulario audio = new Formulario("Datos del audiolibro");
        audio.agregar("Duración (minutos):", duracion);
        audio.agregar("Narrador:", narrador);
        audio.agregar("Formato de audio:", formatoAudio);
        audio.agregar("Total de licencias:", licenciasAudio);

        detalle.add(libro, TipoMaterial.LIBRO.name());
        detalle.add(ebook, TipoMaterial.EBOOK.name());
        detalle.add(audio, TipoMaterial.AUDIOLIBRO.name());
        formulario.agregarCompleto(detalle);

        tipo.addActionListener(e -> mostrarDetalle());

        if (existente != null) {
            mostrarDatos(existente);
            tipo.setEnabled(false);   // el tipo no se puede cambiar
            ejemplares.setEnabled(false);
            ejemplares.setToolTipText("Use el botón Ejemplares de la lista de materiales");
        }
        mostrarDetalle();
    }

    private void mostrarDetalle() {
        tarjetas.show(detalle, ((TipoMaterial) tipo.getSelectedItem()).name());
    }

    private void mostrarDatos(Material m) {
        tipo.setSelectedItem(m.getTipo());
        titulo.setText(m.getTitulo());
        autor.setText(m.getAutor());
        paginas.setValue(m.getNumPaginas());
        fechaPublicacion.setText(Formatos.fecha(m.getFechaPublicacion()));
        categoria.setSelectedItem(m.getCategoria());

        switch (m) {
            case LibroFisico l -> {
                isbn.setText(l.getIsbn());
                ubicacion.setText(l.getUbicacionEstante());
            }
            case Ebook e -> {
                formatoArchivo.setSelectedItem(e.getFormatoArchivo());
                tamanoMb.setValue(e.getTamanoMb().doubleValue());
                licenciasEbook.setValue(e.getTotalLicencias());
            }
            case Audiolibro a -> {
                duracion.setValue(a.getDuracionMin());
                narrador.setText(a.getNarrador());
                formatoAudio.setSelectedItem(a.getFormatoAudio());
                licenciasAudio.setValue(a.getTotalLicencias());
            }
        }
    }

    @Override
    protected void guardar() {
        TipoMaterial tipoElegido = (TipoMaterial) tipo.getSelectedItem();

        Material material = switch (tipoElegido) {
            case LIBRO -> {
                LibroFisico l = existente instanceof LibroFisico x ? x : new LibroFisico();
                l.setIsbn(isbn.getText());
                l.setUbicacionEstante(ubicacion.getText());
                yield l;
            }
            case EBOOK -> {
                Ebook e = existente instanceof Ebook x ? x : new Ebook();
                e.setFormatoArchivo(Formulario.textoDe(formatoArchivo));
                e.setTamanoMb(Formulario.decimal(tamanoMb));
                e.setTotalLicencias(Formulario.entero(licenciasEbook));
                yield e;
            }
            case AUDIOLIBRO -> {
                Audiolibro a = existente instanceof Audiolibro x ? x : new Audiolibro();
                a.setDuracionMin(Formulario.entero(duracion));
                a.setNarrador(narrador.getText());
                a.setFormatoAudio(Formulario.textoDe(formatoAudio));
                a.setTotalLicencias(Formulario.entero(licenciasAudio));
                yield a;
            }
        };

        material.setTitulo(titulo.getText());
        material.setAutor(autor.getText());
        material.setNumPaginas(Formulario.entero(paginas));
        material.setFechaPublicacion(Formatos.leerFecha(fechaPublicacion.getText(), "fecha de publicación"));
        material.setCategoria((Categoria) categoria.getSelectedItem());

        if (existente == null) {
            servicio.registrar(material, tipoElegido == TipoMaterial.LIBRO ? Formulario.entero(ejemplares) : 0);
        } else {
            servicio.actualizar(material);
        }
    }
}
