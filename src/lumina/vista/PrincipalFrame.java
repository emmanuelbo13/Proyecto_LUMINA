package lumina.vista;

import lumina.modelo.Estudiante;
import lumina.modelo.Funcionario;
import lumina.modelo.Persona;
import lumina.modelo.TipoPersona;
import lumina.vista.util.Dialogos;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Ventana principal. Muestra pestañas distintas según quién inició sesión:
 *  - Estudiante:    Catálogo, Mis reservas
 *  - Bibliotecario: Materiales, Estudiantes, Reservas
 *  - Administrador: Funcionarios
 */
public class PrincipalFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public PrincipalFrame(Persona usuario) {
        super("Lumina - Biblioteca multimedia");

        JTabbedPane pestanas = new JTabbedPane();
        switch (usuario) {
            case Estudiante e -> {
                pestanas.addTab("Catálogo", new CatalogoPanel(e));
                pestanas.addTab("Mis reservas", new MisReservasPanel(e));
            }
            case Funcionario f when f.esAdministrador() ->
                    pestanas.addTab("Funcionarios", new PersonasPanel(TipoPersona.FUNCIONARIO, f));
            case Funcionario f when f.esBibliotecario() -> {
                pestanas.addTab("Materiales", new MaterialesPanel());
                pestanas.addTab("Estudiantes", new PersonasPanel(TipoPersona.ESTUDIANTE, f));
                pestanas.addTab("Reservas", new ReservasPanel());
            }
            case Funcionario f -> pestanas.addTab("Inicio",
                    new JLabel("El rol " + f.getRol() + " no tiene opciones asignadas.", SwingConstants.CENTER));
        }
        // Al cambiar de pestaña se recargan sus datos, para ver los cambios hechos en otras
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() instanceof Recargable panel) {
                panel.recargar();
            }
        });

        setLayout(new BorderLayout());
        add(crearEncabezado(usuario), BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 660);
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado(Persona usuario) {
        String perfil = switch (usuario) {
            case Estudiante e -> "Estudiante · " + e.getGrado();
            case Funcionario f -> "Funcionario · " + f.getRol();
        };

        JLabel marca = new JLabel("Lumina");
        marca.setFont(marca.getFont().deriveFont(Font.BOLD, 22f));
        marca.setForeground(new Color(0x1565C0));

        JLabel nombre = new JLabel(usuario.getNombreCompleto());
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD));
        JLabel detalle = new JLabel(perfil);

        JPanel datos = new JPanel(new BorderLayout());
        datos.add(nombre, BorderLayout.NORTH);
        datos.add(detalle, BorderLayout.SOUTH);

        JButton cambiarClave = new JButton("Cambiar clave");
        cambiarClave.addActionListener(e -> {
            if (new CambiarClaveDialog(this, usuario).mostrar()) {
                Dialogos.info(this, "Su clave se cambió correctamente.");
            }
        });
        JButton salir = new JButton("Cerrar sesión");
        salir.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.add(datos);
        derecha.add(cambiarClave);
        derecha.add(salir);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDDDDDD)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        encabezado.add(marca, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }
}
