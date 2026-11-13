package lumina.servicio;

import lumina.modelo.Material;

/**
 * Un material junto con cuántas unidades se pueden reservar en este momento
 * (ejemplares disponibles o licencias libres).
 */
public record MaterialDisponible(Material material, int disponibles) {

    @Override
    public String toString() {
        return material.getTitulo() + " [" + material.getTipo() + "] - disponibles: " + disponibles;
    }
}
