package lumina.dao.impl;

import lumina.dao.TipoDocumentoDAO;
import lumina.modelo.TipoDocumento;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TipoDocumentoDAOImpl extends DAOBase implements TipoDocumentoDAO {

    private static final String SELECT = "SELECT id_tipo_documento, codigo, nombre FROM tipo_documento";

    public TipoDocumentoDAOImpl() {
    }

    public TipoDocumentoDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public List<TipoDocumento> listar() {
        return consultarLista(SELECT + " ORDER BY id_tipo_documento", TipoDocumentoDAOImpl::mapear);
    }

    @Override
    public Optional<TipoDocumento> buscarPorId(int idTipoDocumento) {
        return consultarUno(SELECT + " WHERE id_tipo_documento = ?", TipoDocumentoDAOImpl::mapear, idTipoDocumento);
    }

    @Override
    public Optional<TipoDocumento> buscarPorCodigo(String codigo) {
        return consultarUno(SELECT + " WHERE codigo = ?", TipoDocumentoDAOImpl::mapear, codigo);
    }

    private static TipoDocumento mapear(ResultSet rs) throws SQLException {
        return new TipoDocumento(rs.getInt("id_tipo_documento"), rs.getString("codigo"), rs.getString("nombre"));
    }
}
