package italiapizza.dao.impl;

import italiapizza.dao.IPedidoDao;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.ExistenciaInsuficienteException;
import italiapizza.modelo.DetallePedido;
import italiapizza.modelo.Pedido;
import italiapizza.modelo.Producto;
import italiapizza.modelo.Usuario;
import italiapizza.util.Conexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoDaoImpl implements IPedidoDao {

    private static final String SQL_INSERTAR_PEDIDO =
            "INSERT INTO pedido (fecha, total, estatus, id_cliente) VALUES (?,?,?,?)";

    private static final String SQL_INSERTAR_DETALLE =
            "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unit) " +
            "VALUES (?,?,?,?)";

    private static final String SQL_RESTAR_STOCK =
            "UPDATE producto SET cantidad = cantidad - ? WHERE id_producto = ?";

    private static final String SQL_VERIFICAR_STOCK =
            "SELECT cantidad FROM producto WHERE id_producto = ?";

    private static final String SQL_ACTUALIZAR_ESTATUS =
            "UPDATE pedido SET estatus=? WHERE id_pedido=?";

    private static final String SQL_ELIMINAR_DETALLES =
            "DELETE FROM detalle_pedido WHERE id_pedido=?";

    private static final String SQL_ACTUALIZAR_TOTAL =
            "UPDATE pedido SET total=? WHERE id_pedido=?";

    private static final String SQL_BUSCAR_TODOS =
            "SELECT p.*, u.nombre, u.apellidos, u.telefono FROM pedido p " +
            "JOIN usuario u ON p.id_cliente = u.id_usuario ORDER BY p.fecha DESC";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT p.*, u.nombre, u.apellidos, u.telefono FROM pedido p " +
            "JOIN usuario u ON p.id_cliente = u.id_usuario WHERE p.id_pedido=?";

    private static final String SQL_BUSCAR_POR_CLIENTE =
            "SELECT p.*, u.nombre, u.apellidos, u.telefono FROM pedido p " +
            "JOIN usuario u ON p.id_cliente = u.id_usuario " +
            "WHERE p.id_cliente=? ORDER BY p.fecha DESC";

    private static final String SQL_BUSCAR_POR_FECHA =
            "SELECT p.*, u.nombre, u.apellidos, u.telefono FROM pedido p " +
            "JOIN usuario u ON p.id_cliente = u.id_usuario " +
            "WHERE DATE(p.fecha)=? ORDER BY p.fecha DESC";

    private static final String SQL_BUSCAR_POR_ESTATUS =
            "SELECT p.*, u.nombre, u.apellidos, u.telefono FROM pedido p " +
            "JOIN usuario u ON p.id_cliente = u.id_usuario " +
            "WHERE p.estatus=? ORDER BY p.fecha DESC";

    private static final String SQL_BUSCAR_DETALLES =
            "SELECT dp.*, pr.nombre AS nombre_producto, pr.codigo FROM detalle_pedido dp " +
            "JOIN producto pr ON dp.id_producto = pr.id_producto WHERE dp.id_pedido=?";

    @Override
    public void registrar(Pedido pedido) throws DaoException, ExistenciaInsuficienteException {
        try (Connection conexion = Conexion.obtenerConexion()) {
            verificarExistencias(conexion, pedido);
            conexion.setAutoCommit(false);
            try {
                insertarCabeceraPedido(conexion, pedido);
                insertarDetallesPedido(conexion, pedido);
                actualizarStockProductos(conexion, pedido);
                conexion.commit();
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al registrar pedido: " + excepcion.getMessage(),
                    excepcion);
        }
    }

    private void insertarCabeceraPedido(Connection conexion, Pedido pedido) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                SQL_INSERTAR_PEDIDO, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setTimestamp(1, Timestamp.valueOf(pedido.getFecha()));
            sentencia.setDouble(2,   pedido.getTotal());
            sentencia.setString(3,   pedido.getEstatus().name());
            sentencia.setInt(4,      pedido.getIdCliente());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    pedido.setIdPedido(claves.getInt(1));
                }
            }
        }
    }

    private void insertarDetallesPedido(Connection conexion, Pedido pedido) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_DETALLE)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                sentencia.setInt(1,    pedido.getIdPedido());
                sentencia.setInt(2,    detalle.getProducto().getIdProducto());
                sentencia.setInt(3,    detalle.getCantidad());
                sentencia.setDouble(4, detalle.getPrecioUnit());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    private void verificarExistencias(Connection conexion, Pedido pedido)
            throws SQLException, ExistenciaInsuficienteException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_VERIFICAR_STOCK)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                sentencia.setInt(1, detalle.getProducto().getIdProducto());
                try (ResultSet resultado = sentencia.executeQuery()) {
                    if (resultado.next()) {
                        int cantidadDisponible = resultado.getInt("cantidad");
                        if (detalle.getCantidad() > cantidadDisponible) {
                            throw new ExistenciaInsuficienteException(
                                    "Stock insuficiente para \"" +
                                    detalle.getProducto().getNombre() +
                                    "\": disponible " + cantidadDisponible +
                                    ", solicitado " + detalle.getCantidad() + ".");
                        }
                    }
                }
            }
        }
    }

    private void actualizarStockProductos(Connection conexion, Pedido pedido) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_RESTAR_STOCK)) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                sentencia.setInt(1, detalle.getCantidad());
                sentencia.setInt(2, detalle.getProducto().getIdProducto());
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    @Override
    public void actualizarEstatus(int idPedido, Pedido.Estatus nuevoEstatus) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_ESTATUS)) {
            sentencia.setString(1, nuevoEstatus.name());
            sentencia.setInt(2,    idPedido);
            sentencia.executeUpdate();
        } catch (SQLException excepcion) {
            throw new DaoException("Error al actualizar estatus: " + excepcion.getMessage(),
                    excepcion);
        }
    }

    @Override
    public void actualizarDetalles(Pedido pedido) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                eliminarDetallesExistentes(conexion, pedido.getIdPedido());
                insertarDetallesPedido(conexion, pedido);
                actualizarTotalPedido(conexion, pedido);
                conexion.commit();
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al actualizar detalles: " + excepcion.getMessage(),
                    excepcion);
        }
    }

    private void eliminarDetallesExistentes(Connection conexion, int idPedido)
            throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR_DETALLES)) {
            sentencia.setInt(1, idPedido);
            sentencia.executeUpdate();
        }
    }

    private void actualizarTotalPedido(Connection conexion, Pedido pedido) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_TOTAL)) {
            sentencia.setDouble(1, pedido.getTotal());
            sentencia.setInt(2,    pedido.getIdPedido());
            sentencia.executeUpdate();
        }
    }

    @Override
    public Pedido buscarPorId(int idPedido) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idPedido);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    Pedido pedido = mapearPedido(resultado);
                    pedido.setDetalles(buscarDetallesDePedido(conexion, idPedido));
                    return pedido;
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al buscar pedido: " + excepcion.getMessage(), excepcion);
        }
        return null;
    }

    @Override
    public List<Pedido> buscarTodos() throws DaoException {
        return ejecutarConsultaPedidos(SQL_BUSCAR_TODOS, null);
    }

    @Override
    public List<Pedido> buscarPorCliente(int idCliente) throws DaoException {
        return ejecutarConsultaPedidos(SQL_BUSCAR_POR_CLIENTE,
                sentencia -> sentencia.setInt(1, idCliente));
    }

    @Override
    public List<Pedido> buscarPorFecha(LocalDate fecha) throws DaoException {
        return ejecutarConsultaPedidos(SQL_BUSCAR_POR_FECHA,
                sentencia -> sentencia.setDate(1, Date.valueOf(fecha)));
    }

    @Override
    public List<Pedido> buscarPorEstatus(Pedido.Estatus estatus) throws DaoException {
        return ejecutarConsultaPedidos(SQL_BUSCAR_POR_ESTATUS,
                sentencia -> sentencia.setString(1, estatus.name()));
    }

    @FunctionalInterface
    interface AsignadorParametros {
        void asignar(PreparedStatement sentencia) throws SQLException;
    }

    private List<Pedido> ejecutarConsultaPedidos(String consultaSql,
                                                  AsignadorParametros asignador)
            throws DaoException {
        List<Pedido> listaPedidos = new ArrayList<>();
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(consultaSql)) {
            if (asignador != null) {
                asignador.asignar(sentencia);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Pedido pedido = mapearPedido(resultado);
                    pedido.setDetalles(buscarDetallesDePedido(conexion, pedido.getIdPedido()));
                    listaPedidos.add(pedido);
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error en consulta de pedidos: " + excepcion.getMessage(),
                    excepcion);
        }
        return listaPedidos;
    }

    private List<DetallePedido> buscarDetallesDePedido(Connection conexion, int idPedido)
            throws SQLException {
        List<DetallePedido> listaDetalles = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_DETALLES)) {
            sentencia.setInt(1, idPedido);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    DetallePedido detalle = new DetallePedido();
                    Producto producto = new Producto();
                    producto.setIdProducto(resultado.getInt("id_producto"));
                    producto.setNombre(resultado.getString("nombre_producto"));
                    producto.setCodigo(resultado.getString("codigo"));
                    producto.setPrecio(resultado.getDouble("precio_unit"));
                    detalle.setProducto(producto);
                    detalle.setCantidad(resultado.getInt("cantidad"));
                    detalle.setPrecioUnit(resultado.getDouble("precio_unit"));
                    listaDetalles.add(detalle);
                }
            }
        }
        return listaDetalles;
    }

    private Pedido mapearPedido(ResultSet resultado) throws SQLException {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(resultado.getInt("id_pedido"));
        pedido.setFecha(resultado.getTimestamp("fecha").toLocalDateTime());
        pedido.setTotal(resultado.getDouble("total"));
        pedido.setEstatus(Pedido.Estatus.valueOf(resultado.getString("estatus")));
        Usuario cliente = new Usuario();
        cliente.setIdUsuario(resultado.getInt("id_cliente"));
        cliente.setNombre(resultado.getString("nombre"));
        cliente.setApellidos(resultado.getString("apellidos"));
        cliente.setTelefono(resultado.getString("telefono"));
        pedido.setCliente(cliente);
        return pedido;
    }
}
