package lumina.dao.impl;

import lumina.dao.RolDAO;
import lumina.modelo.Rol;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RolDAOImpl extends DAOBase implements RolDAO {

    private static final String SELECT = "SELECT id_rol, nombre, descripcion FROM rol";

    public RolDAOImpl() {
    }

    public RolDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public List<Rol> listar() {
        return consultarLista(SELECT + " ORDER BY id_rol", RolDAOImpl::mapear);
    }

    @Override
    public Optional<Rol> buscarPorId(int idRol) {
        return consultarUno(SELECT + " WHERE id_rol = ?", RolDAOImpl::mapear, idRol);
    }

    private static Rol mapear(ResultSet rs) throws SQLException {
        return new Rol(rs.getInt("id_rol"), rs.getString("nombre"), rs.getString("descripcion"));
    }
}
