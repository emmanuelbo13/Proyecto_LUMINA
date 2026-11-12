package lumina.dao;

import lumina.modelo.Grado;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla grado (catálogo fijo, solo lectura). */
public interface GradoDAO {

    List<Grado> listar();

    Optional<Grado> buscarPorId(int idGrado);
}
