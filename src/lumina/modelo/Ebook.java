package lumina.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Libro electrónico (tablas material + ebook). */
public final class Ebook extends Material implements Licenciable {

    private String formatoArchivo;
    private BigDecimal tamanoMb;
    private int totalLicencias;

    public Ebook() {
    }

    public Ebook(String titulo, String autor, int numPaginas, LocalDate fechaPublicacion,
                 Categoria categoria, String formatoArchivo, BigDecimal tamanoMb, int totalLicencias) {
        super(titulo, autor, numPaginas, fechaPublicacion, categoria);
        this.formatoArchivo = formatoArchivo;
        this.tamanoMb = tamanoMb;
        this.totalLicencias = totalLicencias;
    }

    @Override
    public TipoMaterial getTipo() {
        return TipoMaterial.EBOOK;
    }

    public String getFormatoArchivo() { return formatoArchivo; }
    public void setFormatoArchivo(String formatoArchivo) { this.formatoArchivo = formatoArchivo; }

    public BigDecimal getTamanoMb() { return tamanoMb; }
    public void setTamanoMb(BigDecimal tamanoMb) { this.tamanoMb = tamanoMb; }

    @Override
    public int getTotalLicencias() { return totalLicencias; }
    public void setTotalLicencias(int totalLicencias) { this.totalLicencias = totalLicencias; }

    @Override
    public String toString() {
        return super.toString() + " | " + formatoArchivo + " " + tamanoMb + " MB | Licencias: " + totalLicencias;
    }
}
