package italiapizza.excepcion;

/**
 * Excepción base del sistema Italia Pizza.
 */
public class ItaliaPizzaException extends Exception {
    public ItaliaPizzaException(String mensaje) {
        super(mensaje);
    }
    public ItaliaPizzaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
