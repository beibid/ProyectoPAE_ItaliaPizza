package italiapizza.dao.impl;

import italiapizza.dao.IProductoDao;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Producto;
import italiapizza.util.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDaoImpl implements IProductoDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO producto (codigo, nombre, descripcion, precio, restricciones, foto, cantidad) " +
            "VALUES (?,?,?,?,?,?,?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE producto SET nombre=?, descripcion=?, precio=?, restricciones=?, foto=?, " +
            "cantidad=? WHERE id_producto=?";

    private static final String SQL_ELIMINAR =
            "UPDATE producto SET activo=0 WHERE id_producto=?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM producto WHERE id_producto=? AND activo=1";

    private static final String SQL_BUSCAR_TODOS =
            "SELECT * FROM producto WHERE activo=1 ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_FILTRO =
            "SELECT * FROM producto WHERE activo=1 " +
            "AND (LOWER(nombre) LIKE LOWER(?) OR codigo LIKE ?)";

    private static final String SQL_ESTA_EN_PEDIDO =
            "SELECT COUNT(*) FROM detalle_pedido WHERE id_producto=?";

    @Override
    public void registrar(Producto producto) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, producto.getCodigo());
            sentencia.setString(2, producto.getNombre());
            sentencia.setString(3, producto.getDescripcion());
            sentencia.setDouble(4, producto.getPrecio());
            sentencia.setString(5, producto.getRestricciones());
            sentencia.setBytes(6,  producto.getFoto());
            sentencia.setInt(7,    producto.getCantidad());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    producto.setIdProducto(claves.getInt(1));
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al registrar producto: " + excepcion.getMessage(),
                    excepcion);
        }
    }

    @Override
    public void actualizar(Producto producto) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, producto.getNombre());
            sentencia.setString(2, producto.getDescripcion());
            sentencia.setDouble(3, producto.getPrecio());
            sentencia.setString(4, producto.getRestricciones());
            sentencia.setBytes(5,  producto.getFoto());
            sentencia.setInt(6,    producto.getCantidad());
            sentencia.setInt(7,    producto.getIdProducto());
            sentencia.executeUpdate();
        } catch (SQLException excepcion) {
            throw new DaoException("Error al actualizar producto: " + excepcion.getMessage(),
                    excepcion);
        }
    }

    @Override
    public void eliminar(int idProducto) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idProducto);
            sentencia.executeUpdate();
        } catch (SQLException excepcion) {
            throw new DaoException("Error al eliminar producto: " + excepcion.getMessage(),
                    excepcion);
        }
    }

    @Override
    public Producto buscarPorId(int idProducto) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idProducto);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return mapearProducto(resultado);
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al buscar producto: " + excepcion.getMessage(), excepcion);
        }
        return null;
    }

    @Override
    public List<Producto> buscarTodos() throws DaoException {
        List<Producto> listaProductos = new ArrayList<>();
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                listaProductos.add(mapearProducto(resultado));
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al obtener productos: " + excepcion.getMessage(),
                    excepcion);
        }
        return listaProductos;
    }

    @Override
    public List<Producto> buscarPorFiltro(String filtro) throws DaoException {
        List<Producto> listaProductos = new ArrayList<>();
        String patronLike = "%" + filtro + "%";
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_FILTRO)) {
            sentencia.setString(1, patronLike);
            sentencia.setString(2, patronLike);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    listaProductos.add(mapearProducto(resultado));
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al buscar productos: " + excepcion.getMessage(),
                    excepcion);
        }
        return listaProductos;
    }

    @Override
    public boolean estaEnPedido(int idProducto) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ESTA_EN_PEDIDO)) {
            sentencia.setInt(1, idProducto);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt(1) > 0;
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al verificar producto en pedidos: " + excepcion.getMessage(),
                    excepcion);
        }
        return false;
    }

    private Producto mapearProducto(ResultSet resultado) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(resultado.getInt("id_producto"));
        producto.setCodigo(resultado.getString("codigo"));
        producto.setNombre(resultado.getString("nombre"));
        producto.setDescripcion(resultado.getString("descripcion"));
        producto.setPrecio(resultado.getDouble("precio"));
        producto.setRestricciones(resultado.getString("restricciones"));
        producto.setFoto(resultado.getBytes("foto"));
        producto.setCantidad(resultado.getInt("cantidad"));
        producto.setActivo(resultado.getBoolean("activo"));
        return producto;
    }
}
