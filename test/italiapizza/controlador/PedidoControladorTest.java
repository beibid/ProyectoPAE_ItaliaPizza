package italiapizza.controlador;


import italiapizza.modelo.DetallePedido;
import italiapizza.modelo.Pedido;
import italiapizza.modelo.Producto;
import italiapizza.modelo.Usuario;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


class PedidoControladorTest {
    private final PedidoControlador pedidoControlador = new PedidoControlador();

    private Pedido crearPedidoValido() {
        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombre("Pizza Hawaiana");
        producto.setPrecio(150.0);

        Usuario cliente = new Usuario();
        cliente.setIdUsuario(5);
        cliente.setNombre("Juan");
        cliente.setApellidos("Pérez");

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.getDetalles().add(new DetallePedido(producto, 2));
        return pedido;
    }

    @Test
    void registrar_conPedidoValido_retornaNull() {
        String resultado = pedidoControlador.registrar(crearPedidoValido());
        assertNull(resultado);
    }

    @Test
    void registrar_sinDetalles_retornaMensajeDeError() {
        Pedido pedido = crearPedidoValido();
        pedido.getDetalles().clear();
        String resultado = pedidoControlador.registrar(pedido);
        assertNotNull(resultado);
    }

    @Test
    void registrar_sinCliente_retornaMensajeDeError() {
        Pedido pedido = crearPedidoValido();
        pedido.setCliente(null);
        String resultado = pedidoControlador.registrar(pedido);
        assertNotNull(resultado);
    }

    @Test
    void cambiarEstatus_conDatosValidos_retornaNull() {
        String resultado = pedidoControlador.cambiarEstatus(1, Pedido.Estatus.ENTREGADO);
        assertNull(resultado);
    }

    @Test
    void obtenerTodos_retornaListaNoNula() {
        List<Pedido> resultado = pedidoControlador.obtenerTodos();
        assertNotNull(resultado);
    }

    @Test
    void buscarPorFecha_retornaListaNoNula() {
        List<Pedido> resultado = pedidoControlador.buscarPorFecha(LocalDate.of(2025, 5, 15));
        assertNotNull(resultado);
    }

    @Test
    void actualizarDetalles_sinDetalles_retornaMensajeDeError() {
        Pedido pedido = crearPedidoValido();
        pedido.getDetalles().clear();
        String resultado = pedidoControlador.actualizarDetalles(pedido);
        assertNotNull(resultado);
    }

    @Test
    void actualizarDetalles_conDetallesValidos_retornaNull() {
        String resultado = pedidoControlador.actualizarDetalles(crearPedidoValido());
        assertNull(resultado);
    }
}
