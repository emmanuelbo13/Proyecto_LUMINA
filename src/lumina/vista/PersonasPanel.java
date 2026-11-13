package lumina.vista;

import lumina.modelo.EstadoPersona;
import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Persona;
import lumina.modelo.TipoPersona;
import lumina.servicio.PersonaService;
import lumina.vista.util.Dialogos;

import java.util.List;

/**
 * Gestión de personas de un tipo: el bibliotecario la usa con ESTUDIANTE
 * y el administrador con FUNCIONARIO.
 */
public class PersonasPanel extends PanelLista<Persona> {

    private static final long serialVersionUID = 1L;

    private final TipoPersona tipo;
    private final Persona usuarioActual;
    private final PersonaService servicio = new PersonaService();

    public PersonasPanel(TipoPersona tipo, Persona usuarioActual) {
        super(tipo == TipoPersona.ESTUDIANTE ? "Estudiantes" : "Funcionarios");
        this.tipo = tipo;
        this.usuarioActual = usuarioActual;

        tabla.columna("ID", Integer.class, Persona::getIdPersona)
                .columna("Documento", p -> p.getTipoDocumento().getCodigo() + " " + p.getNumeroDocumento())
                .columna("Nombres", Persona::getNombres)
                .columna("Apellidos", Persona::getApellidos)
                .columna(tipo == TipoPersona.ESTUDIANTE ? "Grado" : "Rol", PersonasPanel::gradoORol)
                .columna("Estado", EstadoPersona.class, Persona::getEstado);
        tabla.alHacerDobleClic(this::editar);

        boton("Nuevo", this::nuevo);
        boton("Editar", this::editar);
        boton("Activar / Inactivar", this::cambiarEstado);
        boton("Restablecer clave", this::restablecerClave);
        boton("Actualizar", this::recargar);

        recargar();
    }

    @Override
    protected List<? extends Persona> cargar() {
        return tipo == TipoPersona.ESTUDIANTE ? servicio.listarEstudiantes() : servicio.listarFuncionarios();
    }

    private static Object gradoORol(Persona persona) {
        return switch (persona) {
            case Estudiante e -> e.getGrado();
            case Funcionario f -> f.getRol();
        };
    }

    private void nuevo() {
        Dialogos.ejecutar(this, () -> {
            if (new PersonaDialog(this, tipo, null).mostrar()) {
                recargar();
            }
        });
    }

    private void editar() {
        tabla.seleccionada().ifPresent(persona -> Dialogos.ejecutar(this, () -> {
            new PersonaDialog(this, tipo, persona).mostrar();
            recargar();   // también si canceló, para descartar cambios a medias
        }));
    }

    private void cambiarEstado() {
        tabla.seleccionada().ifPresent(persona -> {
            if (persona.getIdPersona() == usuarioActual.getIdPersona()) {
                Dialogos.aviso(this, "No puede inactivar su propia cuenta.");
                return;
            }
            EstadoPersona nuevo = persona.getEstado() == EstadoPersona.ACTIVO
                    ? EstadoPersona.INACTIVO : EstadoPersona.ACTIVO;
            if (Dialogos.confirmar(this, "¿Cambiar el estado de " + persona.getNombreCompleto()
                    + " a " + nuevo + "?")
                    && Dialogos.ejecutar(this, () -> servicio.cambiarEstado(persona.getIdPersona(), nuevo))) {
                recargar();
            }
        });
    }

    private void restablecerClave() {
        tabla.seleccionada().ifPresent(persona -> {
            String regla = tipo == TipoPersona.ESTUDIANTE
                    ? "PIN de 4 dígitos"
                    : "mínimo 8 caracteres, con letras y números";
            String clave = Dialogos.pedirClave(this,
                    "Nueva clave para " + persona.getNombreCompleto() + " (" + regla + "):");
            if (clave != null
                    && Dialogos.ejecutar(this, () -> servicio.restablecerClave(persona.getIdPersona(), clave))) {
                Dialogos.info(this, "La clave se actualizó correctamente.");
            }
        });
    }
}
