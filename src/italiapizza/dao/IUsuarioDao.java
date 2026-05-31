package italiapizza.dao;

import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Usuario;
import java.util.List;

/**
 * Interfaz para operaciones CRUD de Usuario.
 */
public interface IUsuarioDao {
    void   registrar(Usuario usuario) throws DaoException;
    void   actualizar(Usuario usuario) throws DaoException;
    void   eliminar(int idUsuario)     throws DaoException;
    Usuario buscarPorId(int idUsuario) throws DaoException;
    Usuario buscarPorCredenciales(String username, String password) throws DaoException;
    List<Usuario> buscarTodos()        throws DaoException;
    List<Usuario> buscarPorFiltro(String filtro) throws DaoException;
    List<Usuario> buscarClientes()     throws DaoException;
    boolean tienePedidos(int idUsuario) throws DaoException;
}
