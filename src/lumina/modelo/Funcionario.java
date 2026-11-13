package lumina.modelo;

/** Funcionario de la biblioteca (tablas persona + funcionario). */
public final class Funcionario extends Persona {

    private Rol rol;

    public Funcionario() {
    }

    public Funcionario(TipoDocumento tipoDocumento, String numeroDocumento, String nombres,
                       String apellidos, String claveHash, Rol rol) {
        super(tipoDocumento, numeroDocumento, nombres, apellidos, claveHash);
        this.rol = rol;
    }

    @Override
    public TipoPersona getTipo() {
        return TipoPersona.FUNCIONARIO;
    }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    @Override
    public String toString() {
        return super.toString() + " | Rol: " + rol;
    }
}
