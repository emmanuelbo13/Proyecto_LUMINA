package lumina.modelo;

/** Rol de un funcionario (tabla rol). */
public class Rol {

    private int idRol;
    private String nombre;
    private String descripcion;

    public Rol() {
    }

    public Rol(int idRol) {
        this.idRol = idRol;
    }

    public Rol(int idRol, String nombre, String descripcion) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Dos objetos son iguales si tienen el mismo id (lo usan los JComboBox). */
    @Override
    public boolean equals(Object o) {
        return o instanceof Rol otro && otro.idRol == idRol;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idRol);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
