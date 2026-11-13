package lumina.servicio;

import lumina.conexion.Transaccion;
import lumina.dao.EjemplarDAO;
import lumina.dao.MaterialDAO;
import lumina.dao.impl.EjemplarDAOImpl;
import lumina.dao.impl.MaterialDAOImpl;
import lumina.dao.impl.ReservaDAOImpl;
import lumina.modelo.Audiolibro;
import lumina.modelo.Ebook;
import lumina.modelo.Ejemplar;
import lumina.modelo.EstadoEjemplar;
import lumina.modelo.EstadoMaterial;
import lumina.modelo.LibroFisico;
import lumina.modelo.Licenciable;
import lumina.modelo.Material;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Registro y mantenimiento de materiales y ejemplares. */
public class MaterialService {

    private final ReservaService reservaService = new ReservaService();

    /**
     * Registra un material. Si es un libro físico, crea también la cantidad
     * de ejemplares indicada, todo en una sola transacción.
     */
    public void registrar(Material material, int cantidadEjemplares) {
        validar(material);
        if (material instanceof LibroFisico && cantidadEjemplares < 1) {
            throw new ServicioException("Un libro físico debe registrarse con al menos un ejemplar");
        }
        Transaccion.ejecutar(con -> {
            new MaterialDAOImpl(con).insertar(material);
            if (material instanceof LibroFisico) {
                EjemplarDAO ejemplarDAO = new EjemplarDAOImpl(con);
                for (int i = 0; i < cantidadEjemplares; i++) {
                    ejemplarDAO.insertar(new Ejemplar(material.getIdMaterial(), LocalDate.now()));
                }
            }
            return null;
        });
    }

    public void actualizar(Material material) {
        validar(material);
        if (material instanceof Licenciable l) {
            int enUso = new ReservaDAOImpl().contarActivasPorMaterial(material.getIdMaterial());
            if (l.getTotalLicencias() < enUso) {
                throw new ServicioException("Hay " + enUso + " licencias en uso; el total no puede ser menor");
            }
        }
        if (!new MaterialDAOImpl().actualizar(material)) {
            throw new ServicioException("El material que intenta actualizar ya no existe");
        }
    }

    /** Retira o reactiva un material. No se puede retirar si tiene reservas activas. */
    public void cambiarEstado(int idMaterial, EstadoMaterial estado) {
        if (estado == EstadoMaterial.RETIRADO
                && new ReservaDAOImpl().contarActivasPorMaterial(idMaterial) > 0) {
            throw new ServicioException("No se puede retirar: el material tiene reservas activas");
        }
        if (!new MaterialDAOImpl().cambiarEstado(idMaterial, estado)) {
            throw new ServicioException("El material no existe");
        }
    }

    public List<Material> listar() {
        return new MaterialDAOImpl().listar();
    }

    /** Todos los materiales con su disponibilidad (vista del bibliotecario). */
    public List<MaterialDisponible> listarConDisponibilidad() {
        return listar().stream()
                .map(m -> new MaterialDisponible(m, reservaService.consultarDisponibilidad(m.getIdMaterial())))
                .toList();
    }

    /** Solo materiales activos, con su disponibilidad (catálogo del estudiante). */
    public List<MaterialDisponible> catalogo() {
        return listarConDisponibilidad().stream()
                .filter(md -> md.material().getEstado() == EstadoMaterial.ACTIVO)
                .toList();
    }

    // ------------------------------------------------------------------
    // Ejemplares
    // ------------------------------------------------------------------

    public List<Ejemplar> listarEjemplares(int idMaterial) {
        return new EjemplarDAOImpl().listarPorMaterial(idMaterial);
    }

    public Ejemplar agregarEjemplar(int idMaterial) {
        MaterialDAO materialDAO = new MaterialDAOImpl();
        Material material = materialDAO.buscarPorId(idMaterial)
                .orElseThrow(() -> new ServicioException("El material no existe"));
        if (!(material instanceof LibroFisico)) {
            throw new ServicioException("Solo los libros físicos tienen ejemplares");
        }
        Ejemplar ejemplar = new Ejemplar(idMaterial, LocalDate.now());
        new EjemplarDAOImpl().insertar(ejemplar);
        return ejemplar;
    }

    /**
     * Marca un ejemplar como DANADO o lo devuelve a DISPONIBLE (reparado).
     * El estado RESERVADO lo manejan solo las reservas.
     */
    public void cambiarEstadoEjemplar(int idMaterial, int numeroEjemplar, EstadoEjemplar nuevoEstado) {
        if (nuevoEstado == EstadoEjemplar.RESERVADO) {
            throw new ServicioException("Un ejemplar solo pasa a RESERVADO mediante una reserva");
        }
        EjemplarDAO ejemplarDAO = new EjemplarDAOImpl();
        Ejemplar ejemplar = ejemplarDAO.buscar(idMaterial, numeroEjemplar)
                .orElseThrow(() -> new ServicioException("El ejemplar no existe"));
        if (ejemplar.getEstado() == EstadoEjemplar.RESERVADO) {
            throw new ServicioException("El ejemplar está reservado; cierre primero la reserva");
        }
        ejemplarDAO.cambiarEstado(idMaterial, numeroEjemplar, ejemplar.getEstado(), nuevoEstado);
    }

    // ------------------------------------------------------------------
    // Validaciones
    // ------------------------------------------------------------------

    private static void validar(Material m) {
        m.setTitulo(Validar.texto(m.getTitulo(), "título", 200));
        m.setAutor(Validar.texto(m.getAutor(), "autor", 150));
        Validar.positivo(m.getNumPaginas(), "número de páginas");
        if (m.getCategoria() == null) {
            throw new ServicioException("Seleccione la categoría");
        }
        if (m.getFechaPublicacion() != null && m.getFechaPublicacion().isAfter(LocalDate.now())) {
            throw new ServicioException("La fecha de publicación no puede ser futura");
        }

        switch (m) {
            case LibroFisico l -> {
                l.setIsbn(Validar.textoOpcional(l.getIsbn(), "ISBN", 17));
                l.setUbicacionEstante(Validar.texto(l.getUbicacionEstante(), "ubicación en estante", 30));
            }
            case Ebook e -> {
                e.setFormatoArchivo(Validar.texto(e.getFormatoArchivo(), "formato de archivo", 10));
                if (e.getTamanoMb() == null || e.getTamanoMb().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ServicioException("El tamaño en MB debe ser mayor que cero");
                }
                Validar.positivo(e.getTotalLicencias(), "total de licencias");
            }
            case Audiolibro a -> {
                Validar.positivo(a.getDuracionMin(), "duración");
                a.setNarrador(Validar.texto(a.getNarrador(), "narrador", 120));
                a.setFormatoAudio(Validar.texto(a.getFormatoAudio(), "formato de audio", 10));
                Validar.positivo(a.getTotalLicencias(), "total de licencias");
            }
        }
    }
}
