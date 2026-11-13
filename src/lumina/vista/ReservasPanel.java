package lumina.vista;

import lumina.modelo.EstadoReserva;
import lumina.modelo.Reserva;
import lumina.servicio.ReservaService;
import lumina.vista.util.Dialogos;
import lumina.vista.util.Formatos;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import java.util.List;

/** Gestión de reservas (bibliotecario). */
public class ReservasPanel extends PanelLista<Reserva> {

    private static final long serialVersionUID = 1L;

    private static final String TODAS = "Todas";

    private final ReservaService servicio = new ReservaService();
    private final JComboBox<String> filtroEstado = new JComboBox<>();

    public ReservasPanel() {
        super("Reservas");

        filtroEstado.addItem(TODAS);
        for (EstadoReserva estado : EstadoReserva.values()) {
            filtroEstado.addItem(estado.name());
        }
        filtroEstado.setSelectedItem(EstadoReserva.ACTIVA.name());
        filtroEstado.addActionListener(e -> recargar());
        agregarFiltro("Estado:", filtroEstado);

        tabla.columna("#", Integer.class, Reserva::getIdReserva)
                .columna("Estudiante", Reserva::getNombreEstudiante)
                .columna("Material", Reserva::getTituloMaterial)
                .columna("Ejemplar", Integer.class, Reserva::getNumeroEjemplar)
                .columna("Reservada", r -> Formatos.fechaHora(r.getFechaReserva()))
                .columna("Vence", r -> Formatos.fecha(r.getFechaVencimiento()))
                .columna("Estado", EstadoReserva.class, Reserva::getEstado);

        boton("Nueva reserva", this::nueva);
        boton("Cancelar reserva", this::cancelar);
        boton("Finalizar reserva", this::finalizar);
        boton("Procesar vencidas", this::procesarVencidas);
        boton("Actualizar", this::recargar);

        recargar();
    }

    @Override
    protected List<? extends Reserva> cargar() {
        String filtro = (String) filtroEstado.getSelectedItem();
        return servicio.listar(TODAS.equals(filtro) ? null : EstadoReserva.valueOf(filtro));
    }

    private void nueva() {
        Dialogos.ejecutar(this, () -> {
            NuevaReservaDialog dialogo = new NuevaReservaDialog(this);
            if (dialogo.mostrar()) {
                Reserva r = dialogo.getReserva();
                Dialogos.info(this, "Reserva #" + r.getIdReserva() + " creada para " + r.getNombreEstudiante()
                        + ".\nVence el " + Formatos.fecha(r.getFechaVencimiento()) + ".");
                recargar();
            }
        });
    }

    private void cancelar() {
        tabla.seleccionada().ifPresent(r -> {
            if (Dialogos.confirmar(this, "¿Cancelar la reserva #" + r.getIdReserva() + " de "
                    + r.getNombreEstudiante() + "?")
                    && Dialogos.ejecutar(this, () -> servicio.cancelar(r.getIdReserva()))) {
                recargar();
            }
        });
    }

    private void finalizar() {
        tabla.seleccionada().ifPresent(r -> {
            boolean danado = false;
            if (r.getNumeroEjemplar() != null) {
                // Libro físico: preguntar cómo regresó el ejemplar
                String[] opciones = {"En buen estado", "Dañado", "Cancelar"};
                int opcion = JOptionPane.showOptionDialog(this,
                        "¿En qué estado regresó el ejemplar " + r.getNumeroEjemplar()
                                + " de «" + r.getTituloMaterial() + "»?",
                        "Finalizar reserva", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, opciones, opciones[0]);
                if (opcion != 0 && opcion != 1) {
                    return;
                }
                danado = opcion == 1;
            } else if (!Dialogos.confirmar(this, "¿Finalizar la reserva #" + r.getIdReserva() + "?")) {
                return;
            }
            boolean ejemplarDanado = danado;
            if (Dialogos.ejecutar(this, () -> servicio.finalizar(r.getIdReserva(), ejemplarDanado))) {
                recargar();
            }
        });
    }

    private void procesarVencidas() {
        Dialogos.ejecutar(this, () -> {
            int vencidas = servicio.marcarVencidas();
            Dialogos.info(this, vencidas == 0
                    ? "No hay reservas vencidas."
                    : "Se marcaron " + vencidas + " reserva(s) como vencidas y se liberaron sus ejemplares.");
        });
        recargar();
    }
}
