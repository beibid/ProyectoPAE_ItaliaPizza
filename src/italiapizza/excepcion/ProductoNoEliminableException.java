package italiapizza.excepcion;

/**
 * Se lanza cuando se intenta eliminar un producto que ya fue utilizado en algún pedido.
 */
public class ProductoNoEliminableException extends ItaliaPizzaException {
    public ProductoNoEliminableException(String mensaje) {
        super(mensaje);
    }
}
