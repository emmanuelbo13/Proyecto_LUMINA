package lumina.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reserva de un material por parte de un estudiante (tabla reserva).
 * numeroEjemplar solo aplica a libros físicos; en ebook y audiolibro es null.
 */
public class Reserva {

    private int idReserva;
    private int idEstudiante;
    private int idMaterial;
    private Integer numeroEjemplar;
    private LocalDateTime fechaReserva;
    private LocalDate fechaVencimiento;
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    // Datos descriptivos de solo lectura, llenados por el DAO al consultar
    private String nombreEstudiante;
    private String tituloMaterial;

    public Reserva() {
    }

    public Reserva(int idEstudiante, int idMaterial, Integer numeroEjemplar, LocalDate fechaVencimiento) {
        this.idEstudiante = idEstudiante;
        this.idMaterial = idMaterial;
        this.numeroEjemplar = numeroEjemplar;
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }

    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int idMaterial) { this.idMaterial = idMaterial; }

    public Integer getNumeroEjemplar() { return numeroEjemplar; }
    public void setNumeroEjemplar(Integer numeroEjemplar) { this.numeroEjemplar = numeroEjemplar; }

    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }

    public String getTituloMaterial() { return tituloMaterial; }
    public void setTituloMaterial(String tituloMaterial) { this.tituloMaterial = tituloMaterial; }

    @Override
    public String toString() {
        String ejemplar = numeroEjemplar != null ? " (ejemplar " + numeroEjemplar + ")" : "";
        return "Reserva #" + idReserva + " | " + nombreEstudiante + " -> " + tituloMaterial + ejemplar
                + " | vence " + fechaVencimiento + " | " + estado;
    }
}
