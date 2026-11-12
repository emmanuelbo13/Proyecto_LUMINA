package lumina.modelo;

/** Grado escolar de un estudiante (tabla grado). */
public class Grado {

    private int idGrado;
    private String nombre;

    public Grado() {
    }

    public Grado(int idGrado) {
        this.idGrado = idGrado;
    }

    public Grado(int idGrado, String nombre) {
        this.idGrado = idGrado;
        this.nombre = nombre;
    }

    public int getIdGrado() { return idGrado; }
    public void setIdGrado(int idGrado) { this.idGrado = idGrado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}
