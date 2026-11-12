package lumina.modelo;

import java.time.LocalDateTime;

/**
 * Persona registrada en Lumina (tabla persona).
 * Es abstracta y sellada: solo puede ser un Estudiante o un Funcionario,
 * igual que la especialización del modelo relacional.
 */
public abstract sealed class Persona permits Estudiante, Funcionario {

    private int idPersona;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String claveHash;
    private EstadoPersona estado = EstadoPersona.ACTIVO;
    private LocalDateTime fechaRegistro;

    protected Persona() {
    }

    protected Persona(TipoDocumento tipoDocumento, String numeroDocumento,
                      String nombres, String apellidos, String claveHash) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.claveHash = claveHash;
    }

    /** Valor que se guarda en la columna tipo_persona. */
    public abstract TipoPersona getTipo();

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public int getIdPersona() { return idPersona; }
    public void setIdPersona(int idPersona) { this.idPersona = idPersona; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }

    public EstadoPersona getEstado() { return estado; }
    public void setEstado(EstadoPersona estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    @Override
    public String toString() {
        String doc = (tipoDocumento != null ? tipoDocumento.getCodigo() : "?") + " " + numeroDocumento;
        return getTipo() + " #" + idPersona + " | " + doc + " | " + getNombreCompleto() + " | " + estado;
    }
}
