package lumina.modelo;

/** Estudiante de la institución (tablas persona + estudiante). */
public final class Estudiante extends Persona {

    private Grado grado;

    public Estudiante() {
    }

    public Estudiante(TipoDocumento tipoDocumento, String numeroDocumento, String nombres,
                      String apellidos, String claveHash, Grado grado) {
        super(tipoDocumento, numeroDocumento, nombres, apellidos, claveHash);
        this.grado = grado;
    }

    @Override
    public TipoPersona getTipo() {
        return TipoPersona.ESTUDIANTE;
    }

    public Grado getGrado() { return grado; }
    public void setGrado(Grado grado) { this.grado = grado; }

    @Override
    public String toString() {
        return super.toString() + " | Grado: " + grado;
    }
}
