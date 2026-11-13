package lumina.modelo;

/** Categoría temática de un material (tabla categoria). */
public class Categoria {

    private int idCategoria;
    private String nombre;

    public Categoria() {
    }

    public Categoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public Categoria(int idCategoria, String nombre) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
    }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** Dos objetos son iguales si tienen el mismo id (lo usan los JComboBox). */
    @Override
    public boolean equals(Object o) {
        return o instanceof Categoria otro && otro.idCategoria == idCategoria;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idCategoria);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
