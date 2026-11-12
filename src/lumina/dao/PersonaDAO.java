package lumina.dao;

import lumina.modelo.EstadoPersona;
import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Persona;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a persona y sus especializaciones (estudiante / funcionario).
 * Cada operación escribe en la tabla padre y en la hija dentro de una transacción.
 * Las personas no se borran: se inactivan con cambiarEstado().
 */
public interface PersonaDAO {

    /** Inserta la persona (Estudiante o Funcionario) y le asigna el id generado. */
    void insertar(Persona persona);

    /** Actualiza datos personales y el grado o rol. No modifica la clave ni el estado. */
    boolean actualizar(Persona persona);

    boolean actualizarClave(int idPersona, String claveHash);

    boolean cambiarEstado(int idPersona, EstadoPersona estado);

    Optional<Persona> buscarPorId(int idPersona);

    /** Útil para el inicio de sesión. */
    Optional<Persona> buscarPorDocumento(int idTipoDocumento, String numeroDocumento);

    List<Persona> listar();

    List<Estudiante> listarEstudiantes();

    List<Funcionario> listarFuncionarios();
}
