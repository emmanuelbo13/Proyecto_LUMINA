package lumina.vista.util;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * JTable que muestra una lista de objetos de tipo T.
 * Las columnas se definen indicando cómo obtener el valor desde el objeto:
 *
 * <pre>
 *   Tabla&lt;Persona&gt; tabla = new Tabla&lt;Persona&gt;()
 *       .columna("Nombres", Persona::getNombres)
 *       .columna("Estado", EstadoPersona.class, Persona::getEstado);
 * </pre>
 */
public class Tabla<T> extends JTable {

    private static final long serialVersionUID = 1L;

    private static final Color VERDE = new Color(0x2E7D32);
    private static final Color ROJO = new Color(0xC62828);
    private static final Color AZUL = new Color(0x1565C0);
    private static final Color GRIS = new Color(0x757575);

    /** Color con el que se pinta cada estado en las columnas de tipo enum. */
    private static final Map<String, Color> COLORES = Map.ofEntries(
            Map.entry("ACTIVO", VERDE), Map.entry("ACTIVA", VERDE), Map.entry("DISPONIBLE", VERDE),
            Map.entry("INACTIVO", ROJO), Map.entry("RETIRADO", ROJO), Map.entry("DANADO", ROJO),
            Map.entry("VENCIDA", ROJO),
            Map.entry("RESERVADO", AZUL), Map.entry("FINALIZADA", AZUL),
            Map.entry("CANCELADA", GRIS));

    private final Modelo<T> modelo = new Modelo<>();
    private final TableRowSorter<Modelo<T>> ordenador = new TableRowSorter<>(modelo);

    public Tabla() {
        setModel(modelo);
        setRowSorter(ordenador);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFillsViewportHeight(true);
        setRowHeight(26);
        getTableHeader().setReorderingAllowed(false);
        setDefaultRenderer(Enum.class, new EstadoRenderer());

        DefaultTableCellRenderer centrado = new DefaultTableCellRenderer();
        centrado.setHorizontalAlignment(SwingConstants.CENTER);
        setDefaultRenderer(Number.class, centrado);
    }

    public Tabla<T> columna(String titulo, Function<? super T, ?> valor) {
        return columna(titulo, Object.class, valor);
    }

    /** El tipo permite ordenar bien los números y colorear los estados (enums). */
    public Tabla<T> columna(String titulo, Class<?> tipo, Function<? super T, ?> valor) {
        modelo.columnas.add(new Columna<>(titulo, tipo, valor));
        modelo.fireTableStructureChanged();
        ajustarColumnasNumericas();
        return this;
    }

    /** Las columnas numéricas (ID, cantidades) son angostas para dejar espacio al texto. */
    private void ajustarColumnasNumericas() {
        for (int i = 0; i < getColumnCount(); i++) {
            if (Number.class.isAssignableFrom(modelo.getColumnClass(i))) {
                TableColumn columna = getColumnModel().getColumn(i);
                columna.setPreferredWidth(80);
                columna.setMaxWidth(110);
            }
        }
    }

    public void setFilas(List<? extends T> filas) {
        modelo.setFilas(filas);
    }

    /** Objeto de la fila seleccionada, o null si no hay selección. */
    public T getSeleccion() {
        int fila = getSelectedRow();
        return fila < 0 ? null : modelo.filas.get(convertRowIndexToModel(fila));
    }

    /** Igual que getSeleccion(), pero avisa al usuario si no ha seleccionado nada. */
    public Optional<T> seleccionada() {
        T seleccion = getSeleccion();
        if (seleccion == null) {
            Dialogos.aviso(this, "Seleccione primero un registro de la tabla.");
        }
        return Optional.ofNullable(seleccion);
    }

    /** Muestra solo las filas que contienen el texto en alguna columna (sin distinguir mayúsculas). */
    public void filtrar(String texto) {
        ordenador.setRowFilter(texto == null || texto.isBlank()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(texto.trim())));
    }

    public void alHacerDobleClic(Runnable accion) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && getSelectedRow() >= 0) {
                    accion.run();
                }
            }
        });
    }

    // ------------------------------------------------------------------

    private record Columna<T>(String titulo, Class<?> tipo, Function<? super T, ?> valor) {
    }

    private static final class Modelo<T> extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private final List<Columna<T>> columnas = new ArrayList<>();
        private List<T> filas = new ArrayList<>();

        void setFilas(List<? extends T> nuevas) {
            filas = new ArrayList<>(nuevas);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.size();
        }

        @Override
        public String getColumnName(int columna) {
            return columnas.get(columna).titulo();
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columnas.get(columna).tipo();
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            return columnas.get(columna).valor().apply(filas.get(fila));
        }
    }

    /** Pinta los estados con color y en negrita. */
    private static final class EstadoRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                                                       boolean foco, int fila, int columna) {
            String texto = valor == null ? "" : valor.toString().replace("DANADO", "DAÑADO");
            super.getTableCellRendererComponent(tabla, texto, seleccionado, foco, fila, columna);
            Color color = valor == null ? null : COLORES.get(valor.toString());
            if (color != null) {
                setFont(getFont().deriveFont(Font.BOLD));
                if (!seleccionado) {
                    setForeground(color);
                }
            } else if (!seleccionado) {
                setForeground(tabla.getForeground());
            }
            return this;
        }
    }
}
