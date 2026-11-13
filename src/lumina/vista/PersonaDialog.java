package lumina.vista;

import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Grado;
import lumina.modelo.Persona;
import lumina.modelo.Rol;
import lumina.modelo.TipoDocumento;
import lumina.modelo.TipoPersona;
import lumina.servicio.CatalogoService;
import lumina.servicio.PersonaService;
import lumina.servicio.ServicioException;
import lumina.vista.util.Formulario;

import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Component;

/** Formulario para crear o editar un estudiante o un funcionario. */
public class PersonaDialog extends DialogoFormulario {

    private static final long serialVersionUID = 1L;

    private final TipoPersona tipo;
    private final Persona existente;
    private final PersonaService servicio = new PersonaService();

    private final JComboBox<TipoDocumento> tipoDocumento;
    private final JTextField numero = new JTextField(22);
    private final JTextField nombres = new JTextField(22);
    private final JTextField apellidos = new JTextField(22);
    private JComboBox<Grado> grado;
    private JComboBox<Rol> rol;
    private JPasswordField clave;
    private JPasswordField confirmacion;

    /** @param existente persona a editar, o null para registrar una nueva */
    public PersonaDialog(Component padre, TipoPersona tipo, Persona existente) {
        super(padre, (existente == null ? "Nuevo " : "Editar ")
                + (tipo == TipoPersona.ESTUDIANTE ? "estudiante" : "funcionario"), "Guardar");
        this.tipo = tipo;
        this.existente = existente;

        CatalogoService catalogos = new CatalogoService();
        tipoDocumento = formulario.agregar("Tipo de documento:", Formulario.combo(catalogos.tiposDocumento()));
        formulario.agregar("Número de documento:", numero);
        formulario.agregar("Nombres:", nombres);
        formulario.agregar("Apellidos:", apellidos);

        if (tipo == TipoPersona.ESTUDIANTE) {
            grado = formulario.agregar("Grado:", Formulario.combo(catalogos.grados()));
        } else {
            rol = formulario.agregar("Rol:", Formulario.combo(catalogos.roles()));
        }

        if (existente == null) {
            // Documento sugerido: TI para estudiantes, CC para funcionarios
            String codigoSugerido = tipo == TipoPersona.ESTUDIANTE ? "TI" : "CC";
            Formulario.seleccionar(tipoDocumento, td -> codigoSugerido.equals(td.getCodigo()));

            String etiqueta = tipo == TipoPersona.ESTUDIANTE ? "PIN (4 dígitos):" : "Clave (mín. 8, letras y números):";
            clave = formulario.agregar(etiqueta, new JPasswordField(22));
            confirmacion = formulario.agregar("Confirmar:", new JPasswordField(22));
        } else {
            mostrarDatos(existente);
        }
    }

    private void mostrarDatos(Persona p) {
        tipoDocumento.setSelectedItem(p.getTipoDocumento());
        numero.setText(p.getNumeroDocumento());
        nombres.setText(p.getNombres());
        apellidos.setText(p.getApellidos());
        switch (p) {
            case Estudiante e -> grado.setSelectedItem(e.getGrado());
            case Funcionario f -> rol.setSelectedItem(f.getRol());
        }
    }

    @Override
    protected void guardar() {
        Persona persona = existente != null ? existente
                : tipo == TipoPersona.ESTUDIANTE ? new Estudiante() : new Funcionario();

        persona.setTipoDocumento((TipoDocumento) tipoDocumento.getSelectedItem());
        persona.setNumeroDocumento(numero.getText());
        persona.setNombres(nombres.getText());
        persona.setApellidos(apellidos.getText());
        switch (persona) {
            case Estudiante e -> e.setGrado((Grado) grado.getSelectedItem());
            case Funcionario f -> f.setRol((Rol) rol.getSelectedItem());
        }

        if (existente == null) {
            String texto = new String(clave.getPassword());
            if (!texto.equals(new String(confirmacion.getPassword()))) {
                throw new ServicioException("La clave y su confirmación no coinciden");
            }
            servicio.registrar(persona, texto);
        } else {
            servicio.actualizar(persona);
        }
    }
}
