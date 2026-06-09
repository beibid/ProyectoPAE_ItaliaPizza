package italiapizza.controlador;

import italiapizza.dao.impl.PedidoDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.ExistenciaInsuficienteException;
import italiapizza.modelo.Pedido;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PedidoLogicaControlador {

    private final PedidoDaoImpl pedidoDao = new PedidoDaoImpl();

    public String registrar(Pedido pedido) {
        if (pedido.getCliente() == null)
            return "Debe seleccionar un cliente.";

        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty())
            return "El pedido debe tener al menos un producto.";

        pedido.setIdCliente(pedido.getCliente().getIdCliente());

        try {
            pedido.recalcularTotal();
            pedidoDao.registrar(pedido);
            return null;

        } catch (ExistenciaInsuficienteException e) {
            return "Existencia insuficiente: " + e.getMessage();
        } catch (DaoException e) {
            return "Error al registrar pedido: " + e.getMessage();
        }
    }

    public String actualizarDetalles(Pedido pedido) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty())
            return "El pedido debe tener al menos un producto.";
        try {
            pedido.recalcularTotal();
            pedidoDao.actualizarDetalles(pedido);
            return null;
        } catch (DaoException e) {
            return "Error al actualizar pedido: " + e.getMessage();
        }
    }

    public String cambiarEstatus(int idPedido, Pedido.Estatus nuevoEstatus) {
        try {
            pedidoDao.actualizarEstatus(idPedido, nuevoEstatus);
            return null;
        } catch (DaoException e) {
            return "Error al cambiar estatus: " + e.getMessage();
        }
    }

    public List<Pedido> obtenerTodos() {
        try {
            return pedidoDao.buscarTodos();
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }

    public List<Pedido> buscarPorFecha(LocalDate fecha) {
        try {
            return pedidoDao.buscarPorFecha(fecha);
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }

    public List<Pedido> buscarPorEstatus(Pedido.Estatus estatus) {
        try {
            return pedidoDao.buscarPorEstatus(estatus);
        } catch (DaoException e) {
            return Collections.emptyList();
        }
    }
}