package lumina;

import lumina.dao.impl.MaterialDAOImpl;
import lumina.modelo.Material;
import lumina.modelo.Persona;
import lumina.modelo.Reserva;
import lumina.seguridad.SeguridadClave;
import lumina.servicio.AutenticacionService;
import lumina.servicio.ReservaService;
import lumina.servicio.ServicioException;

/**
 * Prueba manual de la capa de servicios.
 *
 * ATENCIÓN: a diferencia de PruebaDAO, esta prueba SÍ guarda cambios
 * (crea, cancela y finaliza reservas). Para dejar la base como al inicio,
 * vuelva a ejecutar "01_lumina_DDL 2.sql" y "03_lumina_DML.sql".
 */
public class PruebaServicios {

    private static final int TI = 2;
    private static final int CC = 3;

    public static void main(String[] args) {
        AutenticacionService auth = new AutenticacionService();
        ReservaService reservas = new ReservaService();

        titulo("SeguridadClave");
        System.out.println("SHA-256 de '1234': " + SeguridadClave.cifrar("1234"));

        titulo("Inicio de sesión");
        intentar("Admin con clave correcta", () -> auth.iniciarSesion(CC, "1000000001", "Lumina2026*"));
        intentar("Admin con clave errada", () -> auth.iniciarSesion(CC, "1000000001", "otraClave"));
        intentar("Documento inexistente", () -> auth.iniciarSesion(CC, "999", "1234"));
        Persona juan = auth.iniciarSesion(TI, "1072000101", "1234");
        System.out.println("Estudiante con PIN correcto -> " + juan);
        intentar("Cambiar PIN a 'abc'", () -> {
            auth.cambiarClave(juan.getIdPersona(), "1234", "abc");
            return "cambiada";
        });

        titulo("Vencimiento de reservas");
        System.out.println("Reservas vencidas y liberadas: " + reservas.marcarVencidas());

        titulo("Disponibilidad");
        mostrarDisponibilidad(reservas);

        titulo("Reservas");
        int idJuan = juan.getIdPersona();   // 3
        int idLaura = 4;
        intentar("Juan reserva 'Cien años de soledad'", () -> reservas.reservar(idJuan, 1));

        // Juan cancela las reservas activas que traía de los datos de prueba
        for (Reserva r : reservas.listarPorEstudiante(idJuan)) {
            if (r.getEstado() == lumina.modelo.EstadoReserva.ACTIVA) {
                intentar("Cancelar la reserva #" + r.getIdReserva() + " de Juan", () -> reservas.cancelar(r.getIdReserva()));
                intentar("Cancelarla de nuevo", () -> reservas.cancelar(r.getIdReserva()));
            }
        }
        intentar("Juan reserva 'Cien años de soledad' otra vez", () -> reservas.reservar(idJuan, 1));
        intentar("Juan reserva el ebook 'Matemáticas básicas' (1 licencia)", () -> reservas.reservar(idJuan, 4));
        intentar("Un funcionario intenta reservar", () -> reservas.reservar(1, 2));
        Reserva principito = intentar("Laura reserva 'El principito' (1 ejemplar)", () -> reservas.reservar(idLaura, 2));
        intentar("Juan reserva 'El principito'", () -> reservas.reservar(idJuan, 2));

        titulo("Cierre de reservas");
        if (principito != null) {
            intentar("Finalizar la de Laura con el libro dañado",
                    () -> reservas.finalizar(principito.getIdReserva(), true));
        }

        titulo("Disponibilidad final");
        mostrarDisponibilidad(reservas);

        titulo("Reservas activas");
        reservas.listarActivas().forEach(System.out::println);
    }

    private static void mostrarDisponibilidad(ReservaService reservas) {
        for (Material m : new MaterialDAOImpl().listar()) {
            System.out.printf("  %-25s %-10s disponibles: %d%n",
                    m.getTitulo(), m.getTipo(), reservas.consultarDisponibilidad(m.getIdMaterial()));
        }
    }

    /** Ejecuta una operación y muestra su resultado o el mensaje de la regla de negocio que falló. */
    private static <T> T intentar(String descripcion, java.util.function.Supplier<T> operacion) {
        try {
            T resultado = operacion.get();
            System.out.println("OK    " + descripcion + " -> " + resultado);
            return resultado;
        } catch (ServicioException e) {
            System.out.println("NO    " + descripcion + " -> " + e.getMessage());
            return null;
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}
