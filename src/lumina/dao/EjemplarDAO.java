package lumina.dao;

import lumina.modelo.Ejemplar;
import lumina.modelo.EstadoEjemplar;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla ejemplar (copias de libros físicos). */
public interface EjemplarDAO {

    /** Inserta el ejemplar asignándole el siguiente número disponible para su libro. */
    void insertar(Ejemplar ejemplar);

    boolean cambiarEstado(int idMaterial, int numeroEjemplar, EstadoEjemplar estado);

    Optional<Ejemplar> buscar(int idMaterial, int numeroEjemplar);

    List<Ejemplar> listarPorMaterial(int idMaterial);

    List<Ejemplar> listarDisponibles(int idMaterial);
}
