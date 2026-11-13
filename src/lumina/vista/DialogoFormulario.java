package lumina.vista;

import lumina.vista.util.Dialogos;
import lumina.vista.util.Formulario;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;

/**
 * Ventana modal con un formulario y los botones Aceptar / Cancelar.
 * Si guardar() lanza un error de negocio, se muestra el mensaje y la
 * ventana sigue abierta para que el usuario corrija los datos.
 */
public abstract class DialogoFormulario extends JDialog {

    private static final long serialVersionUID = 1L;

    protected final Formulario formulario = new Formulario();
    private boolean aceptado;

    protected DialogoFormulario(Component padre, String titulo, String textoAceptar) {
        super(ventanaDe(padre), titulo, ModalityType.APPLICATION_MODAL);

        JButton aceptar = new JButton(textoAceptar);
        JButton cancelar = new JButton("Cancelar");
        aceptar.addActionListener(e -> {
            if (Dialogos.ejecutar(this, this::guardar)) {
                aceptado = true;
                dispose();
            }
        });
        cancelar.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(aceptar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setLayout(new BorderLayout());
        add(formulario, BorderLayout.CENTER);
        add(Formulario.botonera(aceptar, cancelar), BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /** Lee los campos y llama al servicio. Lanza ServicioException si los datos no son válidos. */
    protected abstract void guardar();

    /** Muestra la ventana y espera a que se cierre. Devuelve true si se guardó. */
    public boolean mostrar() {
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return aceptado;
    }

    private static Window ventanaDe(Component componente) {
        return componente instanceof Window w ? w : SwingUtilities.getWindowAncestor(componente);
    }
}
