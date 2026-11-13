package lumina.dao;

import lumina.modelo.Categoria;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla categoria (CRUD completo). */
public interface CategoriaDAO {

    /** Inserta la categoría y le asigna el id generado. */
    void insertar(Categoria categoria);

    boolean actualizar(Categoria categoria);

    /** Falla con DAOException si algún material usa la categoría. */
    boolean eliminar(int idCategoria);

    Optional<Categoria> buscarPorId(int idCategoria);

    List<Categoria> listar();
}
