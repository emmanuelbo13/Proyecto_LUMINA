package lumina.dao.impl;

import lumina.dao.MaterialDAO;
import lumina.modelo.Audiolibro;
import lumina.modelo.Categoria;
import lumina.modelo.Ebook;
import lumina.modelo.EstadoMaterial;
import lumina.modelo.LibroFisico;
import lumina.modelo.Material;
import lumina.modelo.TipoMaterial;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MaterialDAOImpl extends DAOBase implements MaterialDAO {

    /** Une material con sus tres posibles tablas hijas; solo una tendrá datos. */
    private static final String SELECT = """
            SELECT m.id_material, m.titulo, m.autor, m.num_paginas, m.fecha_publicacion,
                   m.tipo_material, m.estado, m.fecha_registro,
                   c.id_categoria, c.nombre AS categoria_nombre,
                   lf.isbn, lf.ubicacion_estante,
                   eb.formato_archivo, eb.tamano_mb, eb.total_licencias AS eb_licencias,
                   au.duracion_min, au.narrador, au.formato_audio, au.total_licencias AS au_licencias
            FROM material m
            JOIN categoria c         ON c.id_categoria = m.id_categoria
            LEFT JOIN libro_fisico lf ON lf.id_material = m.id_material
            LEFT JOIN ebook eb        ON eb.id_material = m.id_material
            LEFT JOIN audiolibro au   ON au.id_material = m.id_material
            """;

    private static final String ORDEN = " ORDER BY m.titulo";

    public MaterialDAOImpl() {
    }

    public MaterialDAOImpl(Connection conexion) {
        super(conexion);
    }

    @Override
    public void insertar(Material material) {
        int id = ejecutarEnTransaccion(con -> {
            int nuevoId = insertarConId(con, """
                    INSERT INTO material (titulo, autor, num_paginas, fecha_publicacion,
                                          id_categoria, tipo_material, estado)
                    VALUES (?, ?, ?, ?, ?, ?, ?)""",
                    material.getTitulo(), material.getAutor(), material.getNumPaginas(),
                    material.getFechaPublicacion(), material.getCategoria().getIdCategoria(),
                    material.getTipo(), material.getEstado());

            switch (material) {
                case LibroFisico l -> update(con, """
                        INSERT INTO libro_fisico (id_material, isbn, ubicacion_estante)
                        VALUES (?, ?, ?)""",
                        nuevoId, l.getIsbn(), l.getUbicacionEstante());
                case Ebook e -> update(con, """
                        INSERT INTO ebook (id_material, formato_archivo, tamano_mb, total_licencias)
                        VALUES (?, ?, ?, ?)""",
                        nuevoId, e.getFormatoArchivo(), e.getTamanoMb(), e.getTotalLicencias());
                case Audiolibro a -> update(con, """
                        INSERT INTO audiolibro (id_material, duracion_min, narrador, formato_audio, total_licencias)
                        VALUES (?, ?, ?, ?, ?)""",
                        nuevoId, a.getDuracionMin(), a.getNarrador(), a.getFormatoAudio(), a.getTotalLicencias());
            }
            return nuevoId;
        });
        material.setIdMaterial(id);
    }

    @Override
    public boolean actualizar(Material material) {
        return ejecutarEnTransaccion(con -> {
            int filas = update(con, """
                    UPDATE material
                    SET titulo = ?, autor = ?, num_paginas = ?, fecha_publicacion = ?, id_categoria = ?
                    WHERE id_material = ? AND tipo_material = ?""",
                    material.getTitulo(), material.getAutor(), material.getNumPaginas(),
                    material.getFechaPublicacion(), material.getCategoria().getIdCategoria(),
                    material.getIdMaterial(), material.getTipo());
            if (filas == 0) {
                return false;
            }
            switch (material) {
                case LibroFisico l -> update(con,
                        "UPDATE libro_fisico SET isbn = ?, ubicacion_estante = ? WHERE id_material = ?",
                        l.getIsbn(), l.getUbicacionEstante(), l.getIdMaterial());
                case Ebook e -> update(con,
                        "UPDATE ebook SET formato_archivo = ?, tamano_mb = ?, total_licencias = ? WHERE id_material = ?",
                        e.getFormatoArchivo(), e.getTamanoMb(), e.getTotalLicencias(), e.getIdMaterial());
                case Audiolibro a -> update(con, """
                        UPDATE audiolibro SET duracion_min = ?, narrador = ?, formato_audio = ?, total_licencias = ?
                        WHERE id_material = ?""",
                        a.getDuracionMin(), a.getNarrador(), a.getFormatoAudio(), a.getTotalLicencias(),
                        a.getIdMaterial());
            }
            return true;
        });
    }

    @Override
    public boolean cambiarEstado(int idMaterial, EstadoMaterial estado) {
        return ejecutarUpdate("UPDATE material SET estado = ? WHERE id_material = ?",
                estado, idMaterial) > 0;
    }

    @Override
    public Optional<Material> buscarPorId(int idMaterial) {
        return consultarUno(SELECT + " WHERE m.id_material = ?", MaterialDAOImpl::mapear, idMaterial);
    }

    @Override
    public List<Material> listar() {
        return consultarLista(SELECT + ORDEN, MaterialDAOImpl::mapear);
    }

    @Override
    public List<Material> listarPorTipo(TipoMaterial tipo) {
        return consultarLista(SELECT + " WHERE m.tipo_material = ?" + ORDEN, MaterialDAOImpl::mapear, tipo);
    }

    @Override
    public List<Material> buscar(String texto) {
        String patron = "%" + texto + "%";
        return consultarLista(SELECT + " WHERE m.titulo LIKE ? OR m.autor LIKE ?" + ORDEN,
                MaterialDAOImpl::mapear, patron, patron);
    }

    /** Crea la subclase correcta según la columna tipo_material. */
    private static Material mapear(ResultSet rs) throws SQLException {
        Material material = switch (TipoMaterial.valueOf(rs.getString("tipo_material"))) {
            case LIBRO -> {
                LibroFisico l = new LibroFisico();
                l.setIsbn(rs.getString("isbn"));
                l.setUbicacionEstante(rs.getString("ubicacion_estante"));
                yield l;
            }
            case EBOOK -> {
                Ebook e = new Ebook();
                e.setFormatoArchivo(rs.getString("formato_archivo"));
                e.setTamanoMb(rs.getBigDecimal("tamano_mb"));
                e.setTotalLicencias(rs.getInt("eb_licencias"));
                yield e;
            }
            case AUDIOLIBRO -> {
                Audiolibro a = new Audiolibro();
                a.setDuracionMin(rs.getInt("duracion_min"));
                a.setNarrador(rs.getString("narrador"));
                a.setFormatoAudio(rs.getString("formato_audio"));
                a.setTotalLicencias(rs.getInt("au_licencias"));
                yield a;
            }
        };
        material.setIdMaterial(rs.getInt("id_material"));
        material.setTitulo(rs.getString("titulo"));
        material.setAutor(rs.getString("autor"));
        material.setNumPaginas(rs.getInt("num_paginas"));
        material.setFechaPublicacion(rs.getObject("fecha_publicacion", LocalDate.class));
        material.setCategoria(new Categoria(rs.getInt("id_categoria"), rs.getString("categoria_nombre")));
        material.setEstado(EstadoMaterial.valueOf(rs.getString("estado")));
        material.setFechaRegistro(rs.getObject("fecha_registro", LocalDateTime.class));
        return material;
    }
}
