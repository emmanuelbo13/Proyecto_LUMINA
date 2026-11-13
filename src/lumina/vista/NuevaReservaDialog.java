package lumina.vista;

import lumina.modelo.EstadoPersona;
import lumina.modelo.Estudiante;
import lumina.modelo.Reserva;
import lumina.servicio.MaterialDisponible;
import lumina.servicio.MaterialService;
import lumina.servicio.PersonaService;
import lumina.servicio.ReservaService;
import lumina.servicio.ServicioException;
import lumina.vista.util.Formatos;
import lumina.vista.util.Formulario;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Component;

/** El bibliotecario registra una reserva a nombre de un estudiante. */
public class NuevaReservaDialog extends DialogoFormulario {

    private static final long serialVersionUID = 1L;

    private final JComboBox<Estudiante> estudiante;
    private final JComboBox<MaterialDisponible> material;
    private Reserva reserva;

    public NuevaReservaDialog(Component padre) {
        super(padre, "Nueva reserva", "Reservar");

        estudiante = Formulario.combo(new PersonaService().listarEstudiantes().stream()
                .filter(e -> e.getEstado() == EstadoPersona.ACTIVO)
                .toList());
        Formulario.mostrarCon(estudiante, Formatos::persona);

        material = Formulario.combo(new MaterialService().catalogo().stream()
                .filter(md -> md.disponibles() > 0)
                .toList());

        formulario.agregar("Estudiante:", estudiante);
        formulario.agregar("Material:", material);
        formulario.agregarCompleto(new JLabel("Solo se listan materiales con unidades disponibles. "
                + "La reserva vence en " + ReservaService.DIAS_VIGENCIA + " días."));
    }

    @Override
    protected void guardar() {
        Estudiante e = (Estudiante) estudiante.getSelectedItem();
        MaterialDisponible md = (MaterialDisponible) material.getSelectedItem();
        if (e == null || md == null) {
            throw new ServicioException("Seleccione el estudiante y el material");
        }
        reserva = new ReservaService().reservar(e.getIdPersona(), md.material().getIdMaterial());
    }

    /** La reserva creada (después de que mostrar() devuelve true). */
    public Reserva getReserva() {
        return reserva;
    }
}
