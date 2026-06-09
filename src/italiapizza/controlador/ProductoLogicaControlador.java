package italiapizza.controlador;

import italiapizza.dao.impl.ProductoDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.ProductoNoEliminableException;
import italiapizza.modelo.Producto;

import java.util.Collections;
import java.util.List;

public class ProductoLogicaControlador {

    private final ProductoDaoImpl productoDao = new ProductoDaoImpl();

    public String registrar(Producto producto) {
        String mensajeValidacion = validarProducto(producto);
        if (mensajeValidacion != null) return mensajeValidacion;
        try {
            productoDao.registrar(producto);
            return null;
        } catch (DaoException e) {
            return "Error al registrar producto: " + e.getMessage();
        }
    }

    public String actualizar(Producto producto) {
        String mensajeValidacion = validarProducto(producto);
        if (mensajeValidacion != null) return mensajeValidacion;
        try {
            productoDao.actualizar(producto);
            return null;
        } catch (DaoException e) {
            return "Error al actualizar producto: " + e.getMessage();
        }
    }

    public String eliminar(Producto producto) {
        try {
            if (productoDao.estaEnPedido(producto.getIdProducto())) {
                return new ProductoNoEliminableException(
                        "El producto ya fue utilizado en un pedido.").getMessage();
            }
            productoDao.eliminar(producto.getIdProducto());
            return null;
        } catch (DaoException e) {
            return "Error al eliminar producto: " + e.getMessage();
        }
    }

    public List<Producto> obtenerTodos() {
        try {
            return productoDao.buscarTodos();
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }

    public List<Producto> buscarPorFiltro(String filtro) {
        try {
            return productoDao.buscarPorFiltro(filtro);
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }

    private String validarProducto(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().isBlank())
            return "El código es obligatorio.";
        if (producto.getNombre() == null || producto.getNombre().isBlank())
            return "El nombre es obligatorio.";
        if (producto.getPrecio() <= 0)
            return "El precio debe ser mayor a cero.";
        return null;
    }
}