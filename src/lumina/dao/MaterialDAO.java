package lumina.dao;

import lumina.modelo.EstadoMaterial;
import lumina.modelo.Material;
import lumina.modelo.TipoMaterial;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a material y sus especializaciones (libro físico / ebook / audiolibro).
 * Los materiales no se borran: se retiran con cambiarEstado().
 */
public interface MaterialDAO {

    /** Inserta el material (cualquier subtipo) y le asigna el id generado. */
    void insertar(Material material);

    /** Actualiza los datos generales y los del subtipo. No modifica el estado. */
    boolean actualizar(Material material);

    boolean cambiarEstado(int idMaterial, EstadoMaterial estado);

    Optional<Material> buscarPorId(int idMaterial);

    List<Material> listar();

    List<Material> listarPorTipo(TipoMaterial tipo);

    /** Busca por coincidencia parcial en título o autor. */
    List<Material> buscar(String texto);
}
