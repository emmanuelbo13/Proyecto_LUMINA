package lumina.dao.impl;

import lumina.dao.GradoDAO;
import lumina.modelo.Grado;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GradoDAOImpl extends DAOBase implements GradoDAO {

    private static final String SELECT = "SELECT id_grado, nombre FROM grado";

    public GradoDAOImpl() {
    }

    public GradoDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public List<Grado> listar() {
        return consultarLista(SELECT + " ORDER BY id_grado", GradoDAOImpl::mapear);
    }

    @Override
    public Optional<Grado> buscarPorId(int idGrado) {
        return consultarUno(SELECT + " WHERE id_grado = ?", GradoDAOImpl::mapear, idGrado);
    }

    private static Grado mapear(ResultSet rs) throws SQLException {
        return new Grado(rs.getInt("id_grado"), rs.getString("nombre"));
    }
}
