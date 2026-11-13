package lumina.vista.util;

import lumina.servicio.ServicioException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Panel de formulario en dos columnas (etiqueta | campo) usando GridBagLayout,
 * más utilidades para crear campos comunes.
 */
public class Formulario extends JPanel {

    private static final long serialVersionUID = 1L;

    private int fila;

    public Formulario() {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }

    /** Formulario con un borde titulado, para agrupar campos. */
    public Formulario(String titulo) {
        super(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
    }

    /** Agrega una fila "etiqueta: campo" y devuelve el campo. */
    public <C extends JComponent> C agregar(String etiqueta, C campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(4, 4, 4, 8);
        JLabel label = new JLabel(etiqueta);
        label.setLabelFor(campo);
        add(label, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = fila++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(4, 0, 4, 4);
        add(campo, c);
        return campo;
    }

    /** Agrega un componente que ocupa las dos columnas. */
    public <C extends JComponent> C agregarCompleto(C componente) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.insets = new Insets(6, 4, 4, 4);
        add(componente, c);
        return componente;
    }

    // ------------------------------------------------------------------
    // Fábricas de componentes
    // ------------------------------------------------------------------

    public static <T> JComboBox<T> combo(List<T> elementos) {
        return new JComboBox<>(new Vector<>(elementos));
    }

    /** Define el texto con que se muestra cada elemento de un combo. */
    public static <T> void mostrarCon(JComboBox<T> combo, Function<T, String> texto) {
        combo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                          boolean seleccionado, boolean foco) {
                @SuppressWarnings("unchecked")
                T elemento = (T) valor;
                String mostrar = elemento == null ? "" : texto.apply(elemento);
                return super.getListCellRendererComponent(lista, mostrar, indice, seleccionado, foco);
            }
        });
    }

    /** Selecciona el primer elemento del combo que cumpla la condición (si existe). */
    public static <T> void seleccionar(JComboBox<T> combo, Predicate<T> condicion) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (condicion.test(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    /** Combo editable con opciones sugeridas (por ejemplo formatos de archivo). */
    public static JComboBox<String> comboEditable(String... opciones) {
        JComboBox<String> combo = new JComboBox<>(opciones);
        combo.setEditable(true);
        return combo;
    }

    /** Texto escrito o elegido en un combo editable. */
    public static String textoDe(JComboBox<String> combo) {
        Object valor = combo.getEditor().getItem();
        return valor == null ? "" : valor.toString().trim().toUpperCase();
    }

    public static JSpinner spinnerEntero(int valor, int minimo, int maximo) {
        return new JSpinner(new SpinnerNumberModel(valor, minimo, maximo, 1));
    }

    public static JSpinner spinnerDecimal(double valor, double minimo, double maximo, double paso) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(valor, minimo, maximo, paso));
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "0.00"));
        return spinner;
    }

    public static int entero(JSpinner spinner) {
        confirmar(spinner);
        return ((Number) spinner.getValue()).intValue();
    }

    public static BigDecimal decimal(JSpinner spinner) {
        confirmar(spinner);
        return BigDecimal.valueOf(((Number) spinner.getValue()).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }

    /** Aplica lo que el usuario escribió en el spinner (sin esto se perdería si no presionó Enter). */
    private static void confirmar(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            throw new ServicioException("Hay un valor numérico inválido en el formulario");
        }
    }

    /** Campo de texto que avisa cada vez que cambia su contenido. */
    public static JTextField campoBusqueda(Consumer<String> alCambiar) {
        JTextField campo = new JTextField(18);
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alCambiar.accept(campo.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                alCambiar.accept(campo.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                alCambiar.accept(campo.getText());
            }
        });
        return campo;
    }

    /** Fila de botones alineados a la derecha. */
    public static JPanel botonera(JComponent... componentes) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        for (JComponent c : componentes) {
            panel.add(c);
        }
        return panel;
    }
}
