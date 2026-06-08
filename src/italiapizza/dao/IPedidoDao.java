package italiapizza.dao;

import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.ExistenciaInsuficienteException;
import italiapizza.modelo.Pedido;
import java.time.LocalDate;
import java.util.List;

public interface IPedidoDao {
    void   registrar(Pedido pedido)       throws DaoException, ExistenciaInsuficienteException;
    void   actualizarEstatus(int idPedido, Pedido.Estatus estatus) throws DaoException;
    void   actualizarDetalles(Pedido pedido) throws DaoException;
    Pedido buscarPorId(int idPedido)      throws DaoException;
    List<Pedido> buscarTodos()            throws DaoException;
    List<Pedido> buscarPorCliente(int idCliente) throws DaoException;
    List<Pedido> buscarPorFecha(LocalDate fecha)  throws DaoException;
    List<Pedido> buscarPorEstatus(Pedido.Estatus estatus) throws DaoException;
}
