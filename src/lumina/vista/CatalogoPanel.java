package lumina.vista;

import lumina.modelo.Audiolibro;
import lumina.modelo.Ebook;
import lumina.modelo.Estudiante;
import lumina.modelo.LibroFisico;
import lumina.modelo.Material;
import lumina.modelo.Reserva;
import lumina.modelo.TipoMaterial;
import lumina.servicio.MaterialDisponible;
import lumina.servicio.MaterialService;
import lumina.servicio.ReservaService;
import lumina.vista.util.Dialogos;
import lumina.vista.util.Formatos;

import java.util.List;

/** Catálogo de materiales activos donde el estudiante hace sus reservas. */
public class CatalogoPanel extends PanelLista<MaterialDisponible> {

    private static final long serialVersionUID = 1L;

    private final Estudiante estudiante;
    private final MaterialService materialService = new MaterialService();
    private final ReservaService reservaService = new ReservaService();

    public CatalogoPanel(Estudiante estudiante) {
        super("Catálogo de materiales");
        this.estudiante = estudiante;

        tabla.columna("Título", md -> md.material().getTitulo())
                .columna("Autor", md -> md.material().getAutor())
                .columna("Tipo", TipoMaterial.class, md -> md.material().getTipo())
                .columna("Categoría", md -> md.material().getCategoria())
                .columna("Detalle", md -> describir(md.material()))
                .columna("Disponibles", Integer.class, MaterialDisponible::disponibles);
        tabla.alHacerDobleClic(this::reservar);

        boton("Reservar", this::reservar);
        boton("Actualizar", this::recargar);

        recargar();
    }

    @Override
    protected List<? extends MaterialDisponible> cargar() {
        return materialService.catalogo();
    }

    private static String describir(Material m) {
        return switch (m) {
            case LibroFisico l -> "Estante " + l.getUbicacionEstante() + " · " + l.getNumPaginas() + " págs.";
            case Ebook e -> e.getFormatoArchivo() + " · " + e.getTamanoMb() + " MB";
            case Audiolibro a -> a.getDuracionMin() + " min · narra " + a.getNarrador();
        };
    }

    private void reservar() {
        tabla.seleccionada().ifPresent(md -> {
            Material m = md.material();
            if (md.disponibles() == 0) {
                Dialogos.aviso(this, "En este momento no hay unidades disponibles de «" + m.getTitulo() + "».");
                return;
            }
            if (!Dialogos.confirmar(this, "¿Desea reservar «" + m.getTitulo() + "»?\n"
                    + "La reserva vence en " + ReservaService.DIAS_VIGENCIA + " días.")) {
                return;
            }
            Dialogos.ejecutar(this, () -> {
                Reserva r = reservaService.reservar(estudiante.getIdPersona(), m.getIdMaterial());
                String indicacion = r.getNumeroEjemplar() != null
                        ? "Recoja el ejemplar " + r.getNumeroEjemplar() + " en la biblioteca."
                        : "Ya puede acceder al material digital.";
                Dialogos.info(this, "Reserva #" + r.getIdReserva() + " creada.\n" + indicacion
                        + "\nVence el " + Formatos.fecha(r.getFechaVencimiento()) + ".");
            });
            recargar();
        });
    }
}
