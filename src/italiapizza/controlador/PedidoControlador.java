package italiapizza.controlador;

import italiapizza.dao.impl.PedidoDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Pedido;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PedidoControlador {

    private final PedidoDaoImpl pedidoDao;

    public PedidoControlador() {
        this.pedidoDao = new PedidoDaoImpl();
    }

    PedidoControlador(PedidoDaoImpl pedidoDao) {
        this.pedidoDao = pedidoDao;
    }

    public String registrar(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) {
            return "El pedido debe tener al menos un producto.";
        }
        if (pedido.getCliente() == null) {
            return "Selecciona un cliente.";
        }
        pedido.recalcularTotal();
        try {
            pedidoDao.registrar(pedido);
            return null;
        } catch (DaoException excepcion) {
            return "Error al guardar pedido: " + excepcion.getMessage();
        }
    }

    public String actualizarDetalles(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) {
            return "El pedido debe tener al menos un producto.";
        }
        pedido.recalcularTotal();
        try {
            pedidoDao.actualizarDetalles(pedido);
            return null;
        } catch (DaoException excepcion) {
            return "Error al actualizar pedido: " + excepcion.getMessage();
        }
    }

    public String cambiarEstatus(int idPedido, Pedido.Estatus nuevoEstatus) {
        try {
            pedidoDao.actualizarEstatus(idPedido, nuevoEstatus);
            return null;
        } catch (DaoException excepcion) {
            return "Error al cambiar estatus: " + excepcion.getMessage();
        }
    }

    public List<Pedido> obtenerTodos() {
        try {
            return pedidoDao.buscarTodos();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    public List<Pedido> buscarPorCliente(int idCliente) {
        try {
            return pedidoDao.buscarPorCliente(idCliente);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    public List<Pedido> buscarPorFecha(LocalDate fecha) {
        try {
            return pedidoDao.buscarPorFecha(fecha);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    public List<Pedido> buscarPorEstatus(Pedido.Estatus estatus) {
        try {
            return pedidoDao.buscarPorEstatus(estatus);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }
}
