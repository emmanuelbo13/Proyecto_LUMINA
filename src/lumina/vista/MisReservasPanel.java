package lumina.vista;

import lumina.modelo.EstadoReserva;
import lumina.modelo.Estudiante;
import lumina.modelo.Reserva;
import lumina.servicio.ReservaService;
import lumina.vista.util.Dialogos;
import lumina.vista.util.Formatos;

import java.util.List;

/** Reservas del estudiante que inició sesión. */
public class MisReservasPanel extends PanelLista<Reserva> {

    private static final long serialVersionUID = 1L;

    private final Estudiante estudiante;
    private final ReservaService servicio = new ReservaService();

    public MisReservasPanel(Estudiante estudiante) {
        super("Mis reservas");
        this.estudiante = estudiante;

        tabla.columna("#", Integer.class, Reserva::getIdReserva)
                .columna("Material", Reserva::getTituloMaterial)
                .columna("Ejemplar", Integer.class, Reserva::getNumeroEjemplar)
                .columna("Reservada", r -> Formatos.fechaHora(r.getFechaReserva()))
                .columna("Vence", r -> Formatos.fecha(r.getFechaVencimiento()))
                .columna("Estado", EstadoReserva.class, Reserva::getEstado);

        boton("Cancelar reserva", this::cancelar);
        boton("Actualizar", this::recargar);

        recargar();
    }

    @Override
    protected List<? extends Reserva> cargar() {
        return servicio.listarPorEstudiante(estudiante.getIdPersona());
    }

    private void cancelar() {
        tabla.seleccionada().ifPresent(r -> {
            if (r.getEstado() != EstadoReserva.ACTIVA) {
                Dialogos.aviso(this, "Solo puede cancelar reservas activas.");
                return;
            }
            if (Dialogos.confirmar(this, "¿Cancelar su reserva de «" + r.getTituloMaterial() + "»?")
                    && Dialogos.ejecutar(this, () -> servicio.cancelar(r.getIdReserva()))) {
                recargar();
            }
        });
    }
}
