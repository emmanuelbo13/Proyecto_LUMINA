package lumina.vista;

import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Persona;
import lumina.modelo.TipoPersona;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.ActionListener;

/**
 * Menú principal. Los botones dependen de quién inició sesión:
 *  - Estudiante:    Catálogo, Mis reservas
 *  - Bibliotecario: Materiales, Estudiantes, Reservas
 *  - Administrador: Funcionarios
 */
public class MenuFrame extends JFrame {

    private Persona usuario;
    private int posicionY = 110;   // altura donde se ubica el siguiente botón

    public MenuFrame(Persona usuario) {
        this.usuario = usuario;
        setTitle("Lumina - Menú principal");
        setLayout(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        iniciarComponentes();
        setSize(420, posicionY + 60);
        setLocationRelativeTo(null);
    }

    private void iniciarComponentes() {
        JLabel lblBienvenida = new JLabel("Bienvenido(a), " + usuario.getNombreCompleto(), SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Arial", Font.BOLD, 16));
        lblBienvenida.setBounds(0, 25, 420, 25);
        add(lblBienvenida);

        JLabel lblPerfil = new JLabel(textoPerfil(), SwingConstants.CENTER);
        lblPerfil.setBounds(0, 55, 420, 20);
        add(lblPerfil);

        if (usuario instanceof Estudiante) {
            Estudiante estudiante = (Estudiante) usuario;
            agregarBoton("Catálogo de materiales", e -> new CatalogoFrame(estudiante).setVisible(true));
            agregarBoton("Mis reservas", e -> new MisReservasFrame(estudiante).setVisible(true));

        } else {
            Funcionario funcionario = (Funcionario) usuario;
            String rol = funcionario.getRol().getNombre();

            if (rol.equals("ADMINISTRADOR")) {
                agregarBoton("Funcionarios",
                        e -> new PersonasFrame(TipoPersona.FUNCIONARIO, usuario).setVisible(true));

            } else if (rol.equals("BIBLIOTECARIO")) {
                agregarBoton("Materiales", e -> new MaterialesFrame().setVisible(true));
                agregarBoton("Estudiantes",
                        e -> new PersonasFrame(TipoPersona.ESTUDIANTE, usuario).setVisible(true));
                agregarBoton("Reservas", e -> new ReservasFrame().setVisible(true));
            }
        }

        // Opciones para todos los usuarios
        agregarBoton("Cambiar clave", e -> new CambiarClaveFrame(usuario).setVisible(true));
        agregarBoton("Cerrar sesión", e -> cerrarSesion());
    }

    /** Crea un botón debajo del anterior. */
    private void agregarBoton(String texto, ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setBounds(100, posicionY, 220, 35);
        boton.addActionListener(accion);
        add(boton);
        posicionY = posicionY + 45;
    }

    private String textoPerfil() {
        if (usuario instanceof Estudiante) {
            return "Estudiante - Grado " + ((Estudiante) usuario).getGrado().getNombre();
        }
        return "Funcionario - " + ((Funcionario) usuario).getRol().getNombre();
    }

    private void cerrarSesion() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
