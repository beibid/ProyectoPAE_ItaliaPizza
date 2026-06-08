package italiapizza.excepcion;


public class ItaliaPizzaException extends Exception {
    public ItaliaPizzaException(String mensaje) {
        super(mensaje);
    }
    public ItaliaPizzaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
