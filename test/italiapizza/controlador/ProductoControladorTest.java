package italiapizza.controlador;

import italiapizza.modelo.Producto;
import italiapizza.util.Conexion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoControladorTest {

    private final ProductoLogicaControlador productoControlador = new ProductoLogicaControlador();

    @BeforeEach
    void limpiarAntes() throws SQLException {
        limpiar();
    }

    @AfterEach
    void limpiarDespues() throws SQLException {
        limpiar();
    }

    private void limpiar() throws SQLException {
        try (Connection con = Conexion.obtenerConexion();
             Statement st = con.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            st.execute("DELETE FROM detalle_pedido WHERE id_producto NOT IN (1)");
            st.execute("DELETE FROM producto WHERE codigo = 'PIZZA-001' AND id_producto NOT IN (1)");
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

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
        try (Connection con = Conexion.obtenerConexion();
             Statement st = con.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            st.execute("DELETE FROM producto WHERE codigo = 'PIZZA-001'");
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            fail("Error al preparar test: " + e.getMessage());
        }
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
        try (Connection con = Conexion.obtenerConexion();
             Statement st = con.createStatement()) {
            st.execute("INSERT INTO producto (codigo, nombre, descripcion, precio, cantidad, activo) " +
                    "VALUES ('TEST-TEMP', 'Producto Temporal', 'Para prueba', 50.00, 5, 1)");
        } catch (SQLException e) {
            fail("Error al preparar test: " + e.getMessage());
        }

        Producto productoTemporal = new Producto();
        try (Connection con = Conexion.obtenerConexion();
             var st = con.prepareStatement(
                     "SELECT id_producto FROM producto WHERE codigo = 'TEST-TEMP'")) {
            var rs = st.executeQuery();
            if (rs.next()) {
                productoTemporal.setIdProducto(rs.getInt("id_producto"));
                productoTemporal.setCodigo("TEST-TEMP");
                productoTemporal.setNombre("Producto Temporal");
                productoTemporal.setPrecio(50.00);
            }
        } catch (SQLException e) {
            fail("Error al obtener producto temporal: " + e.getMessage());
        }

        String resultado = productoControlador.eliminar(productoTemporal);
        assertNull(resultado);
    }

    @Test
    void obtenerTodos_retornaListaNoNula() {
        List<Producto> resultado = productoControlador.obtenerTodos();
        assertNotNull(resultado);
    }
}