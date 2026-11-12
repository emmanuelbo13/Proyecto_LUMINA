package lumina.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Material de la biblioteca (tabla material).
 * Abstracta y sellada: solo puede ser LibroFisico, Ebook o Audiolibro.
 */
public abstract sealed class Material permits LibroFisico, Ebook, Audiolibro {

    private int idMaterial;
    private String titulo;
    private String autor;
    private int numPaginas;
    private LocalDate fechaPublicacion;
    private Categoria categoria;
    private EstadoMaterial estado = EstadoMaterial.ACTIVO;
    private LocalDateTime fechaRegistro;

    protected Material() {
    }

    protected Material(String titulo, String autor, int numPaginas,
                       LocalDate fechaPublicacion, Categoria categoria) {
        this.titulo = titulo;
        this.autor = autor;
        this.numPaginas = numPaginas;
        this.fechaPublicacion = fechaPublicacion;
        this.categoria = categoria;
    }

    /** Valor que se guarda en la columna tipo_material. */
    public abstract TipoMaterial getTipo();

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int idMaterial) { this.idMaterial = idMaterial; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public int getNumPaginas() { return numPaginas; }
    public void setNumPaginas(int numPaginas) { this.numPaginas = numPaginas; }

    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public EstadoMaterial getEstado() { return estado; }
    public void setEstado(EstadoMaterial estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        return getTipo() + " #" + idMaterial + " | " + titulo + " - " + autor
                + " | " + categoria + " | " + estado;
    }
}
