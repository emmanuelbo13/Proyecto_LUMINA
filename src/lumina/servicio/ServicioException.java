package lumina.servicio;

/**
 * Se lanza cuando una operación viola una regla de negocio
 * (por ejemplo: no hay ejemplares disponibles). El mensaje está pensado
 * para mostrarse directamente al usuario.
 */
public class ServicioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServicioException(String mensaje) {
        super(mensaje);
    }
}
