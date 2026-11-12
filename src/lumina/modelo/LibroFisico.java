package lumina.modelo;

import java.time.LocalDate;

/** Libro impreso (tablas material + libro_fisico). Sus copias son ejemplares. */
public final class LibroFisico extends Material {

    private String isbn;
    private String ubicacionEstante;

    public LibroFisico() {
    }

    public LibroFisico(String titulo, String autor, int numPaginas, LocalDate fechaPublicacion,
                       Categoria categoria, String isbn, String ubicacionEstante) {
        super(titulo, autor, numPaginas, fechaPublicacion, categoria);
        this.isbn = isbn;
        this.ubicacionEstante = ubicacionEstante;
    }

    @Override
    public TipoMaterial getTipo() {
        return TipoMaterial.LIBRO;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getUbicacionEstante() { return ubicacionEstante; }
    public void setUbicacionEstante(String ubicacionEstante) { this.ubicacionEstante = ubicacionEstante; }

    @Override
    public String toString() {
        return super.toString() + " | ISBN: " + isbn + " | Estante: " + ubicacionEstante;
    }
}
