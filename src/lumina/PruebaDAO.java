package lumina;

import lumina.conexion.Conexion;
import lumina.dao.CategoriaDAO;
import lumina.dao.DAOException;
import lumina.dao.EjemplarDAO;
import lumina.dao.MaterialDAO;
import lumina.dao.PersonaDAO;
import lumina.dao.ReservaDAO;
import lumina.dao.impl.CategoriaDAOImpl;
import lumina.dao.impl.EjemplarDAOImpl;
import lumina.dao.impl.GradoDAOImpl;
import lumina.dao.impl.MaterialDAOImpl;
import lumina.dao.impl.PersonaDAOImpl;
import lumina.dao.impl.ReservaDAOImpl;
import lumina.dao.impl.RolDAOImpl;
import lumina.dao.impl.TipoDocumentoDAOImpl;
import lumina.modelo.Audiolibro;
import lumina.modelo.Categoria;
import lumina.modelo.Ejemplar;
import lumina.modelo.EstadoEjemplar;
import lumina.modelo.EstadoReserva;
import lumina.modelo.Estudiante;
import lumina.modelo.Grado;
import lumina.modelo.Licenciable;
import lumina.modelo.Material;
import lumina.modelo.Reserva;
import lumina.modelo.TipoDocumento;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Prueba manual de los DAO.
 * 1) Consultas de solo lectura sobre los datos iniciales.
 * 2) Escrituras dentro de una transacción que se deshace al final (rollback),
 *    para no alterar la base de datos.
 */
public class PruebaDAO {

    public static void main(String[] args) {
        probarLecturas();
        probarEscriturasConRollback();
    }

    private static void probarLecturas() {
        titulo("Catálogos");
        System.out.println("Roles:      " + new RolDAOImpl().listar());
        System.out.println("Documentos: " + new TipoDocumentoDAOImpl().listar());
        System.out.println("Grados:     " + new GradoDAOImpl().listar());
        System.out.println("Categorías: " + new CategoriaDAOImpl().listar());

        titulo("Personas (polimorfismo: cada una es Estudiante o Funcionario)");
        new PersonaDAOImpl().listar().forEach(System.out::println);

        titulo("Materiales");
        MaterialDAO materialDAO = new MaterialDAOImpl();
        ReservaDAO reservaDAO = new ReservaDAOImpl();
        for (Material m : materialDAO.listar()) {
            System.out.println(m);
            if (m instanceof Licenciable l) {
                int usadas = reservaDAO.contarActivasPorMaterial(m.getIdMaterial());
                System.out.println("    licencias en uso: " + usadas + "/" + l.getTotalLicencias());
            }
        }

        titulo("Ejemplares de 'Cien años de soledad' (id 1)");
        new EjemplarDAOImpl().listarPorMaterial(1).forEach(System.out::println);

        titulo("Reservas");
        reservaDAO.listar().forEach(System.out::println);

        titulo("Búsquedas");
        System.out.println("Buscar 'garcía': " + materialDAO.buscar("garcía").size() + " resultado(s)");
        System.out.println("Login TI 1072000101: " + new PersonaDAOImpl().buscarPorDocumento(2, "1072000101")
                .map(p -> p.getNombreCompleto()).orElse("no existe"));
    }

    private static void probarEscriturasConRollback() {
        titulo("Escrituras en transacción (se deshacen al final)");
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                // Todos los DAO comparten la misma conexión, por lo tanto la misma transacción
                CategoriaDAO categoriaDAO = new CategoriaDAOImpl(con);
                PersonaDAO personaDAO = new PersonaDAOImpl(con);
                MaterialDAO materialDAO = new MaterialDAOImpl(con);
                EjemplarDAO ejemplarDAO = new EjemplarDAOImpl(con);
                ReservaDAO reservaDAO = new ReservaDAOImpl(con);

                Categoria poesia = new Categoria("Poesía");
                categoriaDAO.insertar(poesia);
                System.out.println("Categoría creada: id " + poesia.getIdCategoria());

                Estudiante nuevo = new Estudiante(new TipoDocumento(2), "1072000999", "Mateo", "Prueba",
                        "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", new Grado(6));
                personaDAO.insertar(nuevo);
                System.out.println("Leído de la BD: " + personaDAO.buscarPorId(nuevo.getIdPersona()).orElseThrow());

                nuevo.setGrado(new Grado(7));
                personaDAO.actualizar(nuevo);
                System.out.println("Tras actualizar: " + personaDAO.buscarPorId(nuevo.getIdPersona()).orElseThrow());

                Audiolibro audio = new Audiolibro("Antología poética", "Varios autores", 80,
                        LocalDate.of(2010, 1, 1), poesia, 120, "Narradora invitada", "MP3", 1);
                materialDAO.insertar(audio);
                System.out.println("Leído de la BD: " + materialDAO.buscarPorId(audio.getIdMaterial()).orElseThrow());

                Ejemplar copia = new Ejemplar(2, LocalDate.now());
                ejemplarDAO.insertar(copia);
                System.out.println("Ejemplar creado: " + copia);

                Reserva reserva = new Reserva(nuevo.getIdPersona(), 2, copia.getNumeroEjemplar(),
                        LocalDate.now().plusDays(3));
                reservaDAO.insertar(reserva);
                ejemplarDAO.cambiarEstado(2, copia.getNumeroEjemplar(), EstadoEjemplar.RESERVADO);
                reservaDAO.cambiarEstado(reserva.getIdReserva(), EstadoReserva.FINALIZADA);
                System.out.println("Reservas del estudiante: " + reservaDAO.listarPorEstudiante(nuevo.getIdPersona()));

                // Una categoría en uso no se puede borrar: la BD lo impide por la llave foránea
                try {
                    categoriaDAO.eliminar(poesia.getIdCategoria());
                } catch (DAOException e) {
                    System.out.println("Eliminar categoría en uso -> " + e.getMessage());
                }
            } finally {
                con.rollback();
                System.out.println("Rollback hecho: la base de datos queda como estaba.");
            }
        } catch (SQLException | DAOException e) {
            System.err.println("Error en la prueba: " + e.getMessage());
        }

        List<Categoria> categorias = new CategoriaDAOImpl().listar();
        System.out.println("Categorías después del rollback: " + categorias.size());
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}
