package lumina.vista;

import lumina.vista.util.Dialogos;
import lumina.vista.util.Formulario;
import lumina.vista.util.Tabla;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * Pantalla típica de listado: título y buscador arriba, tabla en el centro
 * y botones de acción abajo. Las subclases definen columnas, botones y
 * de dónde salen los datos (método cargar).
 */
public abstract class PanelLista<T> extends JPanel implements Recargable {

    private static final long serialVersionUID = 1L;

    protected final Tabla<T> tabla = new Tabla<>();
    private final JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    protected PanelLista(String titulo) {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel etiqueta = new JLabel(titulo);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 18f));

        filtros.add(new JLabel("Buscar:"));
        filtros.add(Formulario.campoBusqueda(tabla::filtrar));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.add(etiqueta, BorderLayout.WEST);
        encabezado.add(filtros, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    /** Consulta los datos que se muestran en la tabla. */
    protected abstract List<? extends T> cargar();

    @Override
    public void recargar() {
        Dialogos.ejecutar(this, () -> tabla.setFilas(cargar()));
    }

    /** Agrega un botón de acción en la parte inferior. */
    protected JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(e -> accion.run());
        botones.add(boton);
        return boton;
    }

    /** Agrega un control de filtro junto al buscador (por ejemplo un combo de estado). */
    protected void agregarFiltro(String etiqueta, JComponent control) {
        filtros.add(new JLabel(etiqueta), 0);
        filtros.add(control, 1);
    }
}
