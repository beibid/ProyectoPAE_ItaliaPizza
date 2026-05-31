package italiapizza.excepcion;

/**
 * Se lanza cuando ocurre un error en la capa de acceso a datos (DAO).
 */
public class DaoException extends ItaliaPizzaException {
    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
