package lumina.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Cifrado de claves con SHA-256, en el mismo formato que usan los datos
 * de prueba del script DML (64 caracteres hexadecimales en minúscula).
 */
public final class SeguridadClave {

    private SeguridadClave() {
    }

    /** Devuelve el hash SHA-256 de la clave en hexadecimal. */
    public static String cifrar(String clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(clave.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Todo JDK incluye SHA-256, así que esto no debería pasar
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }

    /** Compara una clave escrita por el usuario con el hash guardado. */
    public static boolean verificar(String clave, String hashGuardado) {
        if (clave == null || hashGuardado == null) {
            return false;
        }
        byte[] calculado = cifrar(clave).getBytes(StandardCharsets.UTF_8);
        byte[] guardado = hashGuardado.toLowerCase().getBytes(StandardCharsets.UTF_8);
        // isEqual tarda lo mismo sin importar dónde difieran (evita ataques de tiempo)
        return MessageDigest.isEqual(calculado, guardado);
    }
}
