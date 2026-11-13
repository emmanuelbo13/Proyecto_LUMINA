package lumina.servicio;

/** Validaciones comunes de los datos que llegan desde la interfaz. */
final class Validar {

    private Validar() {
    }

    /** Exige un texto no vacío de máximo 'maximo' caracteres y lo devuelve sin espacios sobrantes. */
    static String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new ServicioException("El campo " + campo + " es obligatorio");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ServicioException("El campo " + campo + " admite máximo " + maximo + " caracteres");
        }
        return limpio;
    }

    /** Igual que texto() pero permite vacío; en ese caso devuelve null. */
    static String textoOpcional(String valor, String campo, int maximo) {
        return valor == null || valor.isBlank() ? null : texto(valor, campo, maximo);
    }

    static void positivo(int valor, String campo) {
        if (valor <= 0) {
            throw new ServicioException("El campo " + campo + " debe ser mayor que cero");
        }
    }
}
