package lumina.modelo;

/** Tipo de documento de identidad (tabla tipo_documento). */
public class TipoDocumento {

    private int idTipoDocumento;
    private String codigo;
    private String nombre;

    public TipoDocumento() {
    }

    public TipoDocumento(int idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public TipoDocumento(int idTipoDocumento, String codigo, String nombre) {
        this.idTipoDocumento = idTipoDocumento;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public int getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(int idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    /** Dos objetos son iguales si tienen el mismo id (lo usan los JComboBox). */
    @Override
    public boolean equals(Object o) {
        return o instanceof TipoDocumento otro && otro.idTipoDocumento == idTipoDocumento;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(idTipoDocumento);
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
