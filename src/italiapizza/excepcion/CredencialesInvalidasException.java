package italiapizza.excepcion;

/**
 * Se lanza cuando las credenciales de inicio de sesión son incorrectas.
 */
public class CredencialesInvalidasException extends ItaliaPizzaException {
    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.");
    }
}
