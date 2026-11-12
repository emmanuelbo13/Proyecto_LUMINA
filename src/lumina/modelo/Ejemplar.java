package lumina.modelo;

import java.time.LocalDate;

/**
 * Copia física de un libro (tabla ejemplar).
 * Entidad débil: se identifica por (idMaterial, numeroEjemplar).
 */
public class Ejemplar {

    private int idMaterial;
    private int numeroEjemplar;
    private EstadoEjemplar estado = EstadoEjemplar.DISPONIBLE;
    private LocalDate fechaIngreso;

    public Ejemplar() {
    }

    /** Ejemplar nuevo: el número lo asigna el DAO al insertarlo. */
    public Ejemplar(int idMaterial, LocalDate fechaIngreso) {
        this.idMaterial = idMaterial;
        this.fechaIngreso = fechaIngreso;
    }

    public Ejemplar(int idMaterial, int numeroEjemplar, EstadoEjemplar estado, LocalDate fechaIngreso) {
        this.idMaterial = idMaterial;
        this.numeroEjemplar = numeroEjemplar;
        this.estado = estado;
        this.fechaIngreso = fechaIngreso;
    }

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int idMaterial) { this.idMaterial = idMaterial; }

    public int getNumeroEjemplar() { return numeroEjemplar; }
    public void setNumeroEjemplar(int numeroEjemplar) { this.numeroEjemplar = numeroEjemplar; }

    public EstadoEjemplar getEstado() { return estado; }
    public void setEstado(EstadoEjemplar estado) { this.estado = estado; }

    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    @Override
    public String toString() {
        return "Ejemplar " + idMaterial + "-" + numeroEjemplar + " | " + estado + " | ingreso " + fechaIngreso;
    }
}
