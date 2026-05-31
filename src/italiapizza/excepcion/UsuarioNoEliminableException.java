package italiapizza.excepcion;

/**
 * Se lanza cuando se intenta eliminar un usuario que tiene pedidos activos
 * o cuando un empleado intenta eliminarse a sí mismo.
 */
public class UsuarioNoEliminableException extends ItaliaPizzaException {
    public UsuarioNoEliminableException(String mensaje) {
        super(mensaje);
    }
}
