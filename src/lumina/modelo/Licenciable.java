package lumina.modelo;

/**
 * Material digital que se presta por licencias (ebook y audiolibro).
 * El número de reservas activas no puede superar el total de licencias.
 */
public interface Licenciable {

    int getTotalLicencias();
}
