package lumina.servicio;

import lumina.dao.PersonaDAO;
import lumina.dao.impl.PersonaDAOImpl;
import lumina.modelo.EstadoPersona;
import lumina.modelo.Persona;
import lumina.modelo.TipoPersona;
import lumina.seguridad.SeguridadClave;

/** Inicio de sesión y cambio de clave. */
public class AutenticacionService {

    private static final String CREDENCIALES_INVALIDAS = "Documento o clave incorrectos";

    private final PersonaDAO personaDAO;

    public AutenticacionService() {
        this(new PersonaDAOImpl());
    }

    public AutenticacionService(PersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    /**
     * Valida las credenciales y devuelve la persona autenticada
     * (un Estudiante o un Funcionario).
     *
     * @throws ServicioException si las credenciales no son válidas o el usuario está inactivo
     */
    public Persona iniciarSesion(int idTipoDocumento, String numeroDocumento, String clave) {
        // Mismo mensaje si el documento no existe o la clave falla: no revela qué dato estaba mal
        Persona persona = personaDAO.buscarPorDocumento(idTipoDocumento, numeroDocumento.trim())
                .orElseThrow(() -> new ServicioException(CREDENCIALES_INVALIDAS));

        if (!SeguridadClave.verificar(clave, persona.getClaveHash())) {
            throw new ServicioException(CREDENCIALES_INVALIDAS);
        }
        if (persona.getEstado() != EstadoPersona.ACTIVO) {
            throw new ServicioException("El usuario está inactivo. Comuníquese con la biblioteca");
        }
        return persona;
    }

    public void cambiarClave(int idPersona, String claveActual, String claveNueva) {
        Persona persona = personaDAO.buscarPorId(idPersona)
                .orElseThrow(() -> new ServicioException("La persona no existe"));

        if (!SeguridadClave.verificar(claveActual, persona.getClaveHash())) {
            throw new ServicioException("La clave actual no es correcta");
        }
        validarFormatoClave(persona.getTipo(), claveNueva);
        personaDAO.actualizarClave(idPersona, SeguridadClave.cifrar(claveNueva));
    }

    /**
     * Reglas de formato:
     *  - Estudiantes: PIN de 4 dígitos.
     *  - Funcionarios: mínimo 8 caracteres, con al menos una letra y un número.
     */
    public static void validarFormatoClave(TipoPersona tipo, String clave) {
        if (clave == null) {
            throw new ServicioException("La clave es obligatoria");
        }
        switch (tipo) {
            case ESTUDIANTE -> {
                if (!clave.matches("\\d{4}")) {
                    throw new ServicioException("El PIN debe tener exactamente 4 dígitos");
                }
            }
            case FUNCIONARIO -> {
                if (clave.length() < 8 || !clave.matches(".*[A-Za-z].*") || !clave.matches(".*\\d.*")) {
                    throw new ServicioException(
                            "La clave debe tener mínimo 8 caracteres, con al menos una letra y un número");
                }
            }
        }
    }
}
