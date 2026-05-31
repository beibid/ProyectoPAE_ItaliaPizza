package italiapizza.dao;

import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Producto;
import java.util.List;

public interface IProductoDao {
    void    registrar(Producto producto)  throws DaoException;
    void    actualizar(Producto producto) throws DaoException;
    void    eliminar(int idProducto)      throws DaoException;
    Producto buscarPorId(int idProducto)  throws DaoException;
    List<Producto> buscarTodos()          throws DaoException;
    List<Producto> buscarPorFiltro(String filtro) throws DaoException;
    boolean estaEnPedido(int idProducto)  throws DaoException;
}
