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

    /** Nombre del rol en la tabla rol para los administradores. */
    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    /** Nombre del rol en la tabla rol para los bibliotecarios. */
    public static final String ROL_BIBLIOTECARIO = "BIBLIOTECARIO";

    public boolean esAdministrador() {
        return rol != null && ROL_ADMINISTRADOR.equals(rol.getNombre());
    }

    public boolean esBibliotecario() {
        return rol != null && ROL_BIBLIOTECARIO.equals(rol.getNombre());
    }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    @Override
    public String toString() {
        return super.toString() + " | Rol: " + rol;
    }
}
