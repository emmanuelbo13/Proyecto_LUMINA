package lumina.vista;

import lumina.modelo.Ejemplar;
import lumina.modelo.EstadoEjemplar;
import lumina.modelo.LibroFisico;
import lumina.servicio.MaterialService;
import lumina.vista.util.Dialogos;
import lumina.vista.util.Formatos;
import lumina.vista.util.Formulario;
import lumina.vista.util.Tabla;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/** Lista los ejemplares de un libro y permite agregarlos o marcarlos dañados / reparados. */
public class EjemplaresDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final LibroFisico libro;
    private final MaterialService servicio = new MaterialService();
    private final Tabla<Ejemplar> tabla = new Tabla<Ejemplar>()
            .columna("Número", Integer.class, Ejemplar::getNumeroEjemplar)
            .columna("Estado", EstadoEjemplar.class, Ejemplar::getEstado)
            .columna("Fecha de ingreso", e -> Formatos.fecha(e.getFechaIngreso()));

    public EjemplaresDialog(Component padre, LibroFisico libro) {
        super(SwingUtilities.getWindowAncestor(padre), "Ejemplares de «" + libro.getTitulo() + "»",
                ModalityType.APPLICATION_MODAL);
        this.libro = libro;

        JButton agregar = new JButton("Agregar ejemplar");
        JButton danado = new JButton("Marcar dañado");
        JButton disponible = new JButton("Marcar disponible");
        JButton cerrar = new JButton("Cerrar");
        agregar.addActionListener(e -> agregar());
        danado.addActionListener(e -> cambiarEstado(EstadoEjemplar.DANADO));
        disponible.addActionListener(e -> cambiarEstado(EstadoEjemplar.DISPONIBLE));
        cerrar.addActionListener(e -> dispose());

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 0, 12), scroll.getBorder()));
        scroll.setPreferredSize(new Dimension(460, 240));

        setLayout(new BorderLayout());
        add(scroll, BorderLayout.CENTER);
        add(Formulario.botonera(agregar, danado, disponible, cerrar), BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        recargar();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void recargar() {
        Dialogos.ejecutar(this, () -> tabla.setFilas(servicio.listarEjemplares(libro.getIdMaterial())));
    }

    private void agregar() {
        if (Dialogos.ejecutar(this, () -> servicio.agregarEjemplar(libro.getIdMaterial()))) {
            recargar();
        }
    }

    private void cambiarEstado(EstadoEjemplar estado) {
        tabla.seleccionada().ifPresent(ej -> {
            if (Dialogos.ejecutar(this, () ->
                    servicio.cambiarEstadoEjemplar(ej.getIdMaterial(), ej.getNumeroEjemplar(), estado))) {
                recargar();
            }
        });
    }
}
