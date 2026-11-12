package lumina.dao;

import lumina.modelo.TipoDocumento;

import java.util.List;
import java.util.Optional;

/** Acceso a la tabla tipo_documento (catálogo fijo, solo lectura). */
public interface TipoDocumentoDAO {

    List<TipoDocumento> listar();

    Optional<TipoDocumento> buscarPorId(int idTipoDocumento);

    Optional<TipoDocumento> buscarPorCodigo(String codigo);
}
