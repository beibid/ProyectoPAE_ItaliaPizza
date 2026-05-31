package italiapizza.util;

import italiapizza.modelo.Usuario;

/**
 * Singleton que almacena la sesión del empleado actualmente autenticado.
 */
public class Sesion {

    private static Sesion instancia;
    private Usuario usuarioActual;

    private Sesion() {}

    public static Sesion getInstancia() {
        if (instancia == null) {
            instancia = new Sesion();
        }
        return instancia;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioAutenticado) {
        this.usuarioActual = usuarioAutenticado;
    }

    public boolean esAdministrador() {
        return usuarioActual != null
                && usuarioActual.getRol() == Usuario.Rol.ADMINISTRADOR;
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }
}
