package italiapizza.controlador;

import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.CredencialesInvalidasException;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Usuario;
import italiapizza.util.Sesion;

public class LoginControlador {

    private final UsuarioDaoImpl usuarioDao;

    public LoginControlador() {
        this.usuarioDao = new UsuarioDaoImpl();
    }

    LoginControlador(UsuarioDaoImpl usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    public String iniciarSesion(String nombreUsuario, String contrasena) {
        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            return "Por favor, ingresa usuario y contraseña.";
        }
        try {
            Usuario usuarioEncontrado = usuarioDao.buscarPorCredenciales(nombreUsuario, contrasena);
            if (usuarioEncontrado == null) {
                throw new CredencialesInvalidasException();
            }
            Sesion.getInstancia().setUsuarioActual(usuarioEncontrado);
            return null;
        } catch (CredencialesInvalidasException excepcion) {
            return excepcion.getMessage();
        } catch (DaoException excepcion) {
            return "Error de conexión: " + excepcion.getMessage();
        }
    }
}
