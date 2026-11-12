package lumina.dao.impl;

import lumina.dao.PersonaDAO;
import lumina.modelo.EstadoPersona;
import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Grado;
import lumina.modelo.Persona;
import lumina.modelo.Rol;
import lumina.modelo.TipoDocumento;
import lumina.modelo.TipoPersona;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PersonaDAOImpl extends DAOBase implements PersonaDAO {

    /** Une persona con su tabla hija; solo una de las dos ramas LEFT JOIN tendrá datos. */
    private static final String SELECT = """
            SELECT p.id_persona, p.numero_documento, p.nombres, p.apellidos, p.clave_hash,
                   p.tipo_persona, p.estado, p.fecha_registro,
                   td.id_tipo_documento, td.codigo AS td_codigo, td.nombre AS td_nombre,
                   g.id_grado, g.nombre AS grado_nombre,
                   r.id_rol, r.nombre AS rol_nombre, r.descripcion AS rol_descripcion
            FROM persona p
            JOIN tipo_documento td ON td.id_tipo_documento = p.id_tipo_documento
            LEFT JOIN estudiante e  ON e.id_persona = p.id_persona
            LEFT JOIN grado g       ON g.id_grado = e.id_grado
            LEFT JOIN funcionario f ON f.id_persona = p.id_persona
            LEFT JOIN rol r         ON r.id_rol = f.id_rol
            """;

    private static final String ORDEN = " ORDER BY p.apellidos, p.nombres";

    public PersonaDAOImpl() {
    }

    public PersonaDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public void insertar(Persona persona) {
        int id = ejecutarEnTransaccion(con -> {
            int nuevoId = insertarConId(con, """
                    INSERT INTO persona (id_tipo_documento, numero_documento, nombres, apellidos,
                                         clave_hash, tipo_persona, estado)
                    VALUES (?, ?, ?, ?, ?, ?, ?)""",
                    persona.getTipoDocumento().getIdTipoDocumento(), persona.getNumeroDocumento(),
                    persona.getNombres(), persona.getApellidos(), persona.getClaveHash(),
                    persona.getTipo(), persona.getEstado());

            switch (persona) {
                case Estudiante e -> update(con,
                        "INSERT INTO estudiante (id_persona, id_grado) VALUES (?, ?)",
                        nuevoId, e.getGrado().getIdGrado());
                case Funcionario f -> update(con,
                        "INSERT INTO funcionario (id_persona, id_rol) VALUES (?, ?)",
                        nuevoId, f.getRol().getIdRol());
            }
            return nuevoId;
        });
        persona.setIdPersona(id);
    }

    @Override
    public boolean actualizar(Persona persona) {
        return ejecutarEnTransaccion(con -> {
            int filas = update(con, """
                    UPDATE persona
                    SET id_tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?
                    WHERE id_persona = ? AND tipo_persona = ?""",
                    persona.getTipoDocumento().getIdTipoDocumento(), persona.getNumeroDocumento(),
                    persona.getNombres(), persona.getApellidos(),
                    persona.getIdPersona(), persona.getTipo());
            if (filas == 0) {
                return false;
            }
            switch (persona) {
                case Estudiante e -> update(con, "UPDATE estudiante SET id_grado = ? WHERE id_persona = ?",
                        e.getGrado().getIdGrado(), e.getIdPersona());
                case Funcionario f -> update(con, "UPDATE funcionario SET id_rol = ? WHERE id_persona = ?",
                        f.getRol().getIdRol(), f.getIdPersona());
            }
            return true;
        });
    }

    @Override
    public boolean actualizarClave(int idPersona, String claveHash) {
        return ejecutarUpdate("UPDATE persona SET clave_hash = ? WHERE id_persona = ?",
                claveHash, idPersona) > 0;
    }

    @Override
    public boolean cambiarEstado(int idPersona, EstadoPersona estado) {
        return ejecutarUpdate("UPDATE persona SET estado = ? WHERE id_persona = ?",
                estado, idPersona) > 0;
    }

    @Override
    public Optional<Persona> buscarPorId(int idPersona) {
        return consultarUno(SELECT + " WHERE p.id_persona = ?", PersonaDAOImpl::mapear, idPersona);
    }

    @Override
    public Optional<Persona> buscarPorDocumento(int idTipoDocumento, String numeroDocumento) {
        return consultarUno(SELECT + " WHERE p.id_tipo_documento = ? AND p.numero_documento = ?",
                PersonaDAOImpl::mapear, idTipoDocumento, numeroDocumento);
    }

    @Override
    public List<Persona> listar() {
        return consultarLista(SELECT + ORDEN, PersonaDAOImpl::mapear);
    }

    @Override
    public List<Estudiante> listarEstudiantes() {
        return consultarLista(SELECT + " WHERE p.tipo_persona = ?" + ORDEN, PersonaDAOImpl::mapear,
                TipoPersona.ESTUDIANTE)
                .stream().map(Estudiante.class::cast).toList();
    }

    @Override
    public List<Funcionario> listarFuncionarios() {
        return consultarLista(SELECT + " WHERE p.tipo_persona = ?" + ORDEN, PersonaDAOImpl::mapear,
                TipoPersona.FUNCIONARIO)
                .stream().map(Funcionario.class::cast).toList();
    }

    /** Crea la subclase correcta según la columna tipo_persona. */
    private static Persona mapear(ResultSet rs) throws SQLException {
        Persona persona = switch (TipoPersona.valueOf(rs.getString("tipo_persona"))) {
            case ESTUDIANTE -> {
                Estudiante e = new Estudiante();
                e.setGrado(new Grado(rs.getInt("id_grado"), rs.getString("grado_nombre")));
                yield e;
            }
            case FUNCIONARIO -> {
                Funcionario f = new Funcionario();
                f.setRol(new Rol(rs.getInt("id_rol"), rs.getString("rol_nombre"),
                        rs.getString("rol_descripcion")));
                yield f;
            }
        };
        persona.setIdPersona(rs.getInt("id_persona"));
        persona.setTipoDocumento(new TipoDocumento(rs.getInt("id_tipo_documento"),
                rs.getString("td_codigo"), rs.getString("td_nombre")));
        persona.setNumeroDocumento(rs.getString("numero_documento"));
        persona.setNombres(rs.getString("nombres"));
        persona.setApellidos(rs.getString("apellidos"));
        persona.setClaveHash(rs.getString("clave_hash"));
        persona.setEstado(EstadoPersona.valueOf(rs.getString("estado")));
        persona.setFechaRegistro(rs.getObject("fecha_registro", LocalDateTime.class));
        return persona;
    }
}
