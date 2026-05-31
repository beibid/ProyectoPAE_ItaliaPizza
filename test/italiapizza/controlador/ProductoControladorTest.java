package italiapizza.controlador;


import italiapizza.modelo.Producto;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


class ProductoControladorTest {

    private final ProductoControlador productoControlador = new ProductoControlador();

    private Producto crearProductoValido() {
        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setCodigo("PIZZA-001");
        producto.setNombre("Pizza Margarita");
        producto.setPrecio(120.00);
        producto.setCantidad(10);
        return producto;
    }

    @Test
    void registrar_conProductoValido_retornaNull() {
        String resultado = productoControlador.registrar(crearProductoValido());
        assertNull(resultado);
    }

    @Test
    void registrar_conNombreVacio_retornaMensajeDeError() {
        Producto producto = crearProductoValido();
        producto.setNombre("");
        String resultado = productoControlador.registrar(producto);
        assertNotNull(resultado);
    }

    @Test
    void registrar_conCodigoVacio_retornaMensajeDeError() {
        Producto producto = crearProductoValido();
        producto.setCodigo("");
        String resultado = productoControlador.registrar(producto);
        assertNotNull(resultado);
    }

    @Test
    void registrar_conPrecioCero_retornaMensajeDeError() {
        Producto producto = crearProductoValido();
        producto.setPrecio(0.0);
        String resultado = productoControlador.registrar(producto);
        assertNotNull(resultado);
    }

    @Test
    void registrar_conPrecioNegativo_retornaMensajeDeError() {
        Producto producto = crearProductoValido();
        producto.setPrecio(-50.0);
        String resultado = productoControlador.registrar(producto);
        assertNotNull(resultado);
    }

    @Test
    void actualizar_conProductoValido_retornaNull() {
        String resultado = productoControlador.actualizar(crearProductoValido());
        assertNull(resultado);
    }

    @Test
    void actualizar_conNombreNulo_retornaMensajeDeError() {
        Producto producto = crearProductoValido();
        producto.setNombre(null);
        String resultado = productoControlador.actualizar(producto);
        assertNotNull(resultado);
    }

    @Test
    void eliminar_productoSinPedidos_retornaNull() {
        String resultado = productoControlador.eliminar(crearProductoValido());
        assertNull(resultado);
    }

    @Test
    void obtenerTodos_retornaListaNoNula() {
        List<Producto> resultado = productoControlador.obtenerTodos();
        assertNotNull(resultado);
    }
}
