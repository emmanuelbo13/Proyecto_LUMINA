package lumina.vista;

import lumina.modelo.EstadoMaterial;
import lumina.modelo.LibroFisico;
import lumina.modelo.Material;
import lumina.modelo.TipoMaterial;
import lumina.servicio.MaterialDisponible;
import lumina.servicio.MaterialService;
import lumina.vista.util.Dialogos;

import java.util.List;

/** Gestión de materiales (bibliotecario). */
public class MaterialesPanel extends PanelLista<MaterialDisponible> {

    private static final long serialVersionUID = 1L;

    private final MaterialService servicio = new MaterialService();

    public MaterialesPanel() {
        super("Materiales");

        tabla.columna("ID", Integer.class, md -> md.material().getIdMaterial())
                .columna("Tipo", TipoMaterial.class, md -> md.material().getTipo())
                .columna("Título", md -> md.material().getTitulo())
                .columna("Autor", md -> md.material().getAutor())
                .columna("Categoría", md -> md.material().getCategoria())
                .columna("Disponibles", Integer.class, MaterialDisponible::disponibles)
                .columna("Estado", EstadoMaterial.class, md -> md.material().getEstado());
        tabla.alHacerDobleClic(this::editar);

        boton("Nuevo", this::nuevo);
        boton("Editar", this::editar);
        boton("Ejemplares", this::ejemplares);
        boton("Retirar / Reactivar", this::cambiarEstado);
        boton("Actualizar", this::recargar);

        recargar();
    }

    @Override
    protected List<? extends MaterialDisponible> cargar() {
        return servicio.listarConDisponibilidad();
    }

    private void nuevo() {
        Dialogos.ejecutar(this, () -> {
            if (new MaterialDialog(this, null).mostrar()) {
                recargar();
            }
        });
    }

    private void editar() {
        tabla.seleccionada().ifPresent(md -> Dialogos.ejecutar(this, () -> {
            new MaterialDialog(this, md.material()).mostrar();
            recargar();
        }));
    }

    private void ejemplares() {
        tabla.seleccionada().ifPresent(md -> {
            if (md.material() instanceof LibroFisico libro) {
                new EjemplaresDialog(this, libro).setVisible(true);
                recargar();
            } else {
                Dialogos.aviso(this, "Solo los libros físicos tienen ejemplares.\n"
                        + "Los ebooks y audiolibros se controlan por licencias (botón Editar).");
            }
        });
    }

    private void cambiarEstado() {
        tabla.seleccionada().ifPresent(md -> {
            Material m = md.material();
            EstadoMaterial nuevo = m.getEstado() == EstadoMaterial.ACTIVO
                    ? EstadoMaterial.RETIRADO : EstadoMaterial.ACTIVO;
            String accion = nuevo == EstadoMaterial.RETIRADO ? "retirar" : "reactivar";
            if (Dialogos.confirmar(this, "¿Desea " + accion + " «" + m.getTitulo() + "»?")
                    && Dialogos.ejecutar(this, () -> servicio.cambiarEstado(m.getIdMaterial(), nuevo))) {
                recargar();
            }
        });
    }
}
