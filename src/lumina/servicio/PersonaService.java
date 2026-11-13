package lumina.servicio;

import lumina.dao.PersonaDAO;
import lumina.dao.impl.PersonaDAOImpl;
import lumina.modelo.EstadoPersona;
import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Persona;
import lumina.seguridad.SeguridadClave;

import java.util.List;

/** Registro y mantenimiento de estudiantes y funcionarios. */
public class PersonaService {

    private final PersonaDAO personaDAO;

    public PersonaService() {
        this(new PersonaDAOImpl());
    }

    public PersonaService(PersonaDAO personaDAO) {
        this.personaDAO = personaDAO;
    }

    /** Registra un estudiante o funcionario nuevo; la clave llega en texto plano y se guarda cifrada. */
    public void registrar(Persona persona, String clave) {
        validarDatos(persona);
        AutenticacionService.validarFormatoClave(persona.getTipo(), clave);
        validarDocumentoUnico(persona);
        persona.setClaveHash(SeguridadClave.cifrar(clave));
        personaDAO.insertar(persona);
    }

    public void actualizar(Persona persona) {
        validarDatos(persona);
        validarDocumentoUnico(persona);
        if (!personaDAO.actualizar(persona)) {
            throw new ServicioException("La persona que intenta actualizar ya no existe");
        }
    }

    public void cambiarEstado(int idPersona, EstadoPersona estado) {
        if (!personaDAO.cambiarEstado(idPersona, estado)) {
            throw new ServicioException("La persona no existe");
        }
    }

    /** Asigna una clave nueva sin pedir la anterior (lo hace un funcionario). */
    public void restablecerClave(int idPersona, String claveNueva) {
        Persona persona = personaDAO.buscarPorId(idPersona)
                .orElseThrow(() -> new ServicioException("La persona no existe"));
        AutenticacionService.validarFormatoClave(persona.getTipo(), claveNueva);
        personaDAO.actualizarClave(idPersona, SeguridadClave.cifrar(claveNueva));
    }

    public List<Estudiante> listarEstudiantes() {
        return personaDAO.listarEstudiantes();
    }

    public List<Funcionario> listarFuncionarios() {
        return personaDAO.listarFuncionarios();
    }

    private void validarDocumentoUnico(Persona persona) {
        personaDAO.buscarPorDocumento(persona.getTipoDocumento().getIdTipoDocumento(), persona.getNumeroDocumento())
                .filter(otra -> otra.getIdPersona() != persona.getIdPersona())
                .ifPresent(otra -> {
                    throw new ServicioException("Ya existe una persona registrada con ese documento: "
                            + otra.getNombreCompleto());
                });
    }

    private static void validarDatos(Persona persona) {
        if (persona.getTipoDocumento() == null) {
            throw new ServicioException("Seleccione el tipo de documento");
        }
        persona.setNumeroDocumento(Validar.texto(persona.getNumeroDocumento(), "número de documento", 20));
        if (!persona.getNumeroDocumento().matches("[A-Za-z0-9]+")) {
            throw new ServicioException("El número de documento solo puede tener letras y números");
        }
        persona.setNombres(Validar.texto(persona.getNombres(), "nombres", 60));
        persona.setApellidos(Validar.texto(persona.getApellidos(), "apellidos", 60));

        switch (persona) {
            case Estudiante e when e.getGrado() == null -> throw new ServicioException("Seleccione el grado");
            case Funcionario f when f.getRol() == null -> throw new ServicioException("Seleccione el rol");
            default -> { }
        }
    }
}
