package lumina.dao;

import lumina.modelo.Rol;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla rol (catálogo fijo, solo lectura). */
public interface RolDAO {

    List<Rol> listar();

    Optional<Rol> buscarPorId(int idRol);
}
