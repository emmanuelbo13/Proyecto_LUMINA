package lumina.modelo;

import java.time.LocalDate;

/** Audiolibro (tablas material + audiolibro). */
public final class Audiolibro extends Material implements Licenciable {

    private int duracionMin;
    private String narrador;
    private String formatoAudio;
    private int totalLicencias;

    public Audiolibro() {
    }

    public Audiolibro(String titulo, String autor, int numPaginas, LocalDate fechaPublicacion,
                      Categoria categoria, int duracionMin, String narrador, String formatoAudio,
                      int totalLicencias) {
        super(titulo, autor, numPaginas, fechaPublicacion, categoria);
        this.duracionMin = duracionMin;
        this.narrador = narrador;
        this.formatoAudio = formatoAudio;
        this.totalLicencias = totalLicencias;
    }

    @Override
    public TipoMaterial getTipo() {
        return TipoMaterial.AUDIOLIBRO;
    }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }

    public String getNarrador() { return narrador; }
    public void setNarrador(String narrador) { this.narrador = narrador; }

    public String getFormatoAudio() { return formatoAudio; }
    public void setFormatoAudio(String formatoAudio) { this.formatoAudio = formatoAudio; }

    @Override
    public int getTotalLicencias() { return totalLicencias; }
    public void setTotalLicencias(int totalLicencias) { this.totalLicencias = totalLicencias; }

    @Override
    public String toString() {
        return super.toString() + " | " + duracionMin + " min, narra " + narrador
                + " | Licencias: " + totalLicencias;
    }
}
