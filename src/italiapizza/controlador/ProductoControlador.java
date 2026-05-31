package italiapizza.controlador;

import italiapizza.dao.impl.ProductoDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.ProductoNoEliminableException;
import italiapizza.modelo.Producto;

import java.util.Collections;
import java.util.List;

public class ProductoControlador {

    private final ProductoDaoImpl productoDao;

    public ProductoControlador() {
        this.productoDao = new ProductoDaoImpl();
    }

    ProductoControlador(ProductoDaoImpl productoDao) {
        this.productoDao = productoDao;
    }

    public String registrar(Producto producto) {
        String mensajeValidacion = validarProducto(producto);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            productoDao.registrar(producto);
            return null;
        } catch (DaoException excepcion) {
            return "Error al guardar: " + excepcion.getMessage();
        }
    }

    public String actualizar(Producto producto) {
        String mensajeValidacion = validarProducto(producto);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            productoDao.actualizar(producto);
            return null;
        } catch (DaoException excepcion) {
            return "Error al actualizar: " + excepcion.getMessage();
        }
    }

    public String eliminar(Producto producto) {
        try {
            if (productoDao.estaEnPedido(producto.getIdProducto())) {
                throw new ProductoNoEliminableException(
                        "El producto ya fue utilizado en pedidos y no puede eliminarse.");
            }
            productoDao.eliminar(producto.getIdProducto());
            return null;
        } catch (ProductoNoEliminableException excepcion) {
            return excepcion.getMessage();
        } catch (DaoException excepcion) {
            return "Error al eliminar: " + excepcion.getMessage();
        }
    }

    public List<Producto> obtenerTodos() {
        try {
            return productoDao.buscarTodos();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    public List<Producto> buscarPorFiltro(String filtro) {
        try {
            return productoDao.buscarPorFiltro(filtro);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private String validarProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            return "El nombre es requerido.";
        }
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            return "El código es requerido.";
        }
        if (producto.getPrecio() <= 0) {
            return "El precio debe ser mayor a 0.";
        }
        return null;
    }
}
