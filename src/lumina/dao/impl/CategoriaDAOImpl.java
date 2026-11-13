package lumina.dao.impl;

import lumina.dao.CategoriaDAO;
import lumina.modelo.Categoria;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CategoriaDAOImpl extends DAOBase implements CategoriaDAO {

    private static final String SELECT = "SELECT id_categoria, nombre FROM categoria";

    public CategoriaDAOImpl() {
    }

    public CategoriaDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public void insertar(Categoria categoria) {
        int id = ejecutar(con -> insertarConId(con,
                "INSERT INTO categoria (nombre) VALUES (?)", categoria.getNombre()));
        categoria.setIdCategoria(id);
    }

    @Override
    public boolean actualizar(Categoria categoria) {
        return ejecutarUpdate("UPDATE categoria SET nombre = ? WHERE id_categoria = ?",
                categoria.getNombre(), categoria.getIdCategoria()) > 0;
    }

    @Override
    public boolean eliminar(int idCategoria) {
        return ejecutarUpdate("DELETE FROM categoria WHERE id_categoria = ?", idCategoria) > 0;
    }

    @Override
    public Optional<Categoria> buscarPorId(int idCategoria) {
        return consultarUno(SELECT + " WHERE id_categoria = ?", CategoriaDAOImpl::mapear, idCategoria);
    }

    @Override
    public List<Categoria> listar() {
        return consultarLista(SELECT + " ORDER BY nombre", CategoriaDAOImpl::mapear);
    }

    private static Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id_categoria"), rs.getString("nombre"));
    }
}
