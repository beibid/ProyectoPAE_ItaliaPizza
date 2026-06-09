package italiapizza.dao.impl;

import italiapizza.dao.IUsuarioDao;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Usuario;
import italiapizza.util.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDaoImpl implements IUsuarioDao {

    private static final String SQL_INSERTAR =
            "INSERT INTO usuario (nombre, apellidos, telefono, correo, tipo, username, password) " +
            "VALUES (?,?,?,?,?,?,SHA2(?,256))";

    private static final String SQL_INSERTAR_EMPLEADO =
            "INSERT INTO empleado (id_usuario, rol) VALUES (?,?)";

    private static final String SQL_INSERTAR_CLIENTE =
            "INSERT INTO cliente (id_usuario, calle, numero, codigo_postal, ciudad) " +
            "VALUES (?,?,?,?,?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE usuario SET nombre=?, apellidos=?, telefono=?, correo=?, tipo=?, username=? " +
            "WHERE id_usuario=?";

    private static final String SQL_ACTUALIZAR_EMPLEADO =
            "UPDATE empleado SET rol=? WHERE id_usuario=?";

    private static final String SQL_ACTUALIZAR_CLIENTE =
            "UPDATE cliente SET calle=?, numero=?, codigo_postal=?, ciudad=? WHERE id_usuario=?";

    private static final String SQL_ELIMINAR =
            "UPDATE usuario SET activo=0 WHERE id_usuario=?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM usuario WHERE id_usuario=? AND activo=1";

    private static final String SQL_BUSCAR_POR_CREDENCIALES =
            "SELECT u.id_usuario, u.nombre, u.apellidos, u.telefono, u.correo, " +
            "u.username, u.activo, e.rol " +
            "FROM usuario u " +
            "INNER JOIN empleado e ON u.id_usuario = e.id_usuario " +
            "WHERE u.username=? AND u.password=SHA2(?,256) AND u.activo=1";

    private static final String SQL_BUSCAR_TODOS =
            "SELECT u.*, e.rol FROM usuario u " +
            "LEFT JOIN empleado e ON u.id_usuario = e.id_usuario " +
            "WHERE u.activo=1 ORDER BY u.apellidos, u.nombre";

    private static final String SQL_BUSCAR_POR_FILTRO =
            "SELECT u.*, e.rol FROM usuario u " +
            "LEFT JOIN empleado e ON u.id_usuario = e.id_usuario " +
            "WHERE u.activo=1 AND (" +
            "LOWER(u.nombre) LIKE LOWER(?) OR LOWER(u.apellidos) LIKE LOWER(?) OR " +
            "LOWER(CONCAT(u.nombre,' ',u.apellidos)) LIKE LOWER(?) OR u.telefono LIKE ?)";

    private static final String SQL_BUSCAR_CLIENTES =
            "SELECT u.id_usuario, u.nombre, u.apellidos, u.telefono, u.correo, u.activo, " +
            "c.id_cliente, c.calle, c.numero, c.codigo_postal, c.ciudad " +
            "FROM usuario u " +
            "INNER JOIN cliente c ON u.id_usuario = c.id_usuario " +
            "WHERE u.activo = 1";

    private static final String SQL_TIENE_PEDIDOS =
            "SELECT COUNT(*) FROM pedido WHERE id_cliente=?";

    @Override
    public void registrar(Usuario usuario) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(
                        SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                    sentencia.setString(1, usuario.getNombre());
                    sentencia.setString(2, usuario.getApellidos());
                    sentencia.setString(3, usuario.getTelefono());
                    sentencia.setString(4, usuario.getEmail());
                    sentencia.setString(5, usuario.getTipo().name());
                    sentencia.setString(6, usuario.getUsername());
                    sentencia.setString(7, usuario.getPassword());
                    sentencia.executeUpdate();
                    try (ResultSet claves = sentencia.getGeneratedKeys()) {
                        if (claves.next()) {
                            usuario.setIdUsuario(claves.getInt(1));
                        }
                    }
                }
                if (usuario.getTipo() == Usuario.Tipo.EMPLEADO) {
                    try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_EMPLEADO)) {
                        sentencia.setInt(1,    usuario.getIdUsuario());
                        sentencia.setString(2, usuario.getRol().name());
                        sentencia.executeUpdate();
                    }
                } else {
                    try (PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR_CLIENTE)) {
                        sentencia.setInt(1,    usuario.getIdUsuario());
                        sentencia.setString(2, usuario.getCalle());
                        sentencia.setString(3, usuario.getNumero());
                        sentencia.setString(4, usuario.getCodigoPostal());
                        sentencia.setString(5, usuario.getCiudad());
                        sentencia.executeUpdate();
                    }
                }
                conexion.commit();
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al registrar usuario: " + excepcion.getMessage(), excepcion);
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
                    sentencia.setString(1, usuario.getNombre());
                    sentencia.setString(2, usuario.getApellidos());
                    sentencia.setString(3, usuario.getTelefono());
                    sentencia.setString(4, usuario.getEmail());
                    sentencia.setString(5, usuario.getTipo().name());
                    sentencia.setString(6, usuario.getUsername());
                    sentencia.setInt(7,    usuario.getIdUsuario());
                    sentencia.executeUpdate();
                }
                if (usuario.getTipo() == Usuario.Tipo.EMPLEADO) {
                    try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_EMPLEADO)) {
                        sentencia.setString(1, usuario.getRol().name());
                        sentencia.setInt(2,    usuario.getIdUsuario());
                        sentencia.executeUpdate();
                    }
                } else {
                    try (PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_CLIENTE)) {
                        sentencia.setString(1, usuario.getCalle());
                        sentencia.setString(2, usuario.getNumero());
                        sentencia.setString(3, usuario.getCodigoPostal());
                        sentencia.setString(4, usuario.getCiudad());
                        sentencia.setInt(5,    usuario.getIdUsuario());
                        sentencia.executeUpdate();
                    }
                }
                conexion.commit();
            } catch (SQLException excepcion) {
                conexion.rollback();
                throw excepcion;
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al actualizar usuario: " + excepcion.getMessage(), excepcion);
        }
    }

    @Override
    public void eliminar(int idUsuario) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idUsuario);
            sentencia.executeUpdate();
        } catch (SQLException excepcion) {
            throw new DaoException("Error al eliminar usuario: " + excepcion.getMessage(), excepcion);
        }
    }

    @Override
    public Usuario buscarPorId(int idUsuario) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idUsuario);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return mapearUsuario(resultado);
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al buscar usuario: " + excepcion.getMessage(), excepcion);
        }
        return null;
    }

    @Override
    public Usuario buscarPorCredenciales(String nombreUsuario, String contrasena)
            throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_CREDENCIALES)) {
            sentencia.setString(1, nombreUsuario);
            sentencia.setString(2, contrasena);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return mapearEmpleadoParaLogin(resultado);
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al verificar credenciales: " + excepcion.getMessage(),
                    excepcion);
        }
        return null;
    }

    @Override
    public List<Usuario> buscarTodos() throws DaoException {
        List<Usuario> listaUsuarios = new ArrayList<>();
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_TODOS);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                listaUsuarios.add(mapearUsuario(resultado));
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al obtener usuarios: " + excepcion.getMessage(), excepcion);
        }
        return listaUsuarios;
    }

    @Override
    public List<Usuario> buscarPorFiltro(String filtro) throws DaoException {
        List<Usuario> listaUsuarios = new ArrayList<>();
        String patronLike = "%" + filtro + "%";
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_FILTRO)) {
            for (int indice = 1; indice <= 4; indice++) {
                sentencia.setString(indice, patronLike);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    listaUsuarios.add(mapearUsuario(resultado));
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al buscar usuarios: " + excepcion.getMessage(), excepcion);
        }
        return listaUsuarios;
    }

    @Override
    public List<Usuario> buscarClientes() throws DaoException {
        List<Usuario> listaClientes = new ArrayList<>();
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_CLIENTES);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                listaClientes.add(mapearCliente(resultado));
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al obtener clientes: " + excepcion.getMessage(), excepcion);
        }
        return listaClientes;
    }

    @Override
    public boolean tienePedidos(int idUsuario) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_TIENE_PEDIDOS)) {
            sentencia.setInt(1, idUsuario);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt(1) > 0;
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al verificar pedidos de usuario: " + excepcion.getMessage(),
                    excepcion);
        }
        return false;
    }

    private Usuario mapearCliente(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setIdCliente(resultado.getInt("id_cliente"));
        usuario.setNombre(resultado.getString("nombre"));
        usuario.setApellidos(resultado.getString("apellidos"));
        usuario.setTelefono(resultado.getString("telefono"));
        usuario.setEmail(resultado.getString("correo"));
        usuario.setActivo(resultado.getBoolean("activo"));
        usuario.setTipo(Usuario.Tipo.CLIENTE);
        usuario.setCalle(resultado.getString("calle"));
        usuario.setNumero(resultado.getString("numero"));
        usuario.setCodigoPostal(resultado.getString("codigo_postal"));
        usuario.setCiudad(resultado.getString("ciudad"));
        return usuario;
    }

    private Usuario mapearEmpleadoParaLogin(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setNombre(resultado.getString("nombre"));
        usuario.setApellidos(resultado.getString("apellidos"));
        usuario.setTelefono(resultado.getString("telefono"));
        usuario.setEmail(resultado.getString("correo"));
        usuario.setUsername(resultado.getString("username"));
        usuario.setActivo(resultado.getBoolean("activo"));
        usuario.setTipo(Usuario.Tipo.EMPLEADO);
        usuario.setRol(Usuario.Rol.valueOf(resultado.getString("rol")));
        return usuario;
    }

    private Usuario mapearUsuario(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setNombre(resultado.getString("nombre"));
        usuario.setApellidos(resultado.getString("apellidos"));
        usuario.setTelefono(resultado.getString("telefono"));
        usuario.setEmail(resultado.getString("correo"));
        usuario.setTipo(Usuario.Tipo.valueOf(resultado.getString("tipo")));
        usuario.setUsername(resultado.getString("username"));
        usuario.setActivo(resultado.getBoolean("activo"));
        String nombreRol = resultado.getString("rol");
        if (nombreRol != null) {
            usuario.setRol(Usuario.Rol.valueOf(nombreRol));
        }
        return usuario;
    }

}
