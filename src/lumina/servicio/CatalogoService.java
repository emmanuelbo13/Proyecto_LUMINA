package lumina.servicio;

import lumina.dao.impl.CategoriaDAOImpl;
import lumina.dao.impl.GradoDAOImpl;
import lumina.dao.impl.RolDAOImpl;
import lumina.dao.impl.TipoDocumentoDAOImpl;
import lumina.modelo.Categoria;
import lumina.modelo.Grado;
import lumina.modelo.Rol;
import lumina.modelo.TipoDocumento;

import java.util.List;

/** Listas de las tablas de apoyo, usadas para llenar los combos de la interfaz. */
public class CatalogoService {

    public List<TipoDocumento> tiposDocumento() {
        return new TipoDocumentoDAOImpl().listar();
    }

    public List<Grado> grados() {
        return new GradoDAOImpl().listar();
    }

    public List<Rol> roles() {
        return new RolDAOImpl().listar();
    }

    public List<Categoria> categorias() {
        return new CategoriaDAOImpl().listar();
    }
}
