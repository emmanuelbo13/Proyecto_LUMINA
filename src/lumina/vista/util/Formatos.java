package lumina.vista.util;

import lumina.modelo.Persona;
import lumina.servicio.ServicioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Conversión de fechas y textos para mostrar en pantalla. */
public final class Formatos {

    // Formato AAAA-MM-DD: además de ser claro, ordena bien en las tablas
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Formatos() {
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FECHA);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : fecha.format(FECHA_HORA);
    }

    /** Convierte un texto AAAA-MM-DD en fecha. Un texto vacío devuelve null. */
    public static LocalDate leerFecha(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (DateTimeParseException e) {
            throw new ServicioException("La " + campo + " debe tener el formato AAAA-MM-DD, por ejemplo 2024-03-15");
        }
    }

    /** "Juan Pablo Gómez Ruiz (TI 1072000101)" */
    public static String persona(Persona p) {
        return p == null ? "" : p.getNombreCompleto() + " (" + p.getTipoDocumento().getCodigo()
                + " " + p.getNumeroDocumento() + ")";
    }
}
