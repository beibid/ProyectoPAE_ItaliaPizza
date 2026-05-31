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
            "INSERT INTO usuario (nombre, apellidos, telefono, email, calle, numero, " +
            "codigo_postal, ciudad, tipo, username, password, rol) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,SHA2(?,256),?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE usuario SET nombre=?, apellidos=?, telefono=?, email=?, calle=?, numero=?, " +
            "codigo_postal=?, ciudad=?, tipo=?, username=?, rol=? WHERE id_usuario=?";

    private static final String SQL_ELIMINAR =
            "UPDATE usuario SET activo=0 WHERE id_usuario=?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT * FROM usuario WHERE id_usuario=? AND activo=1";

    private static final String SQL_BUSCAR_POR_CREDENCIALES =
            "SELECT * FROM usuario WHERE username=? AND password=SHA2(?,256) " +
            "AND activo=1 AND tipo='EMPLEADO'";

    private static final String SQL_BUSCAR_TODOS =
            "SELECT * FROM usuario WHERE activo=1 ORDER BY apellidos, nombre";

    private static final String SQL_BUSCAR_POR_FILTRO =
            "SELECT * FROM usuario WHERE activo=1 AND (" +
            "LOWER(nombre) LIKE LOWER(?) OR LOWER(apellidos) LIKE LOWER(?) OR " +
            "LOWER(CONCAT(nombre,' ',apellidos)) LIKE LOWER(?) OR " +
            "LOWER(calle) LIKE LOWER(?) OR telefono LIKE ?)";

    private static final String SQL_BUSCAR_CLIENTES =
            "SELECT * FROM usuario WHERE tipo='CLIENTE' AND activo=1 ORDER BY apellidos, nombre";

    private static final String SQL_TIENE_PEDIDOS =
            "SELECT COUNT(*) FROM pedido WHERE id_cliente=?";

    @Override
    public void registrar(Usuario usuario) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(
                     SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1,  usuario.getNombre());
            sentencia.setString(2,  usuario.getApellidos());
            sentencia.setString(3,  usuario.getTelefono());
            sentencia.setString(4,  usuario.getEmail());
            sentencia.setString(5,  usuario.getCalle());
            sentencia.setString(6,  usuario.getNumero());
            sentencia.setString(7,  usuario.getCodigoPostal());
            sentencia.setString(8,  usuario.getCiudad());
            sentencia.setString(9,  usuario.getTipo().name());
            sentencia.setString(10, usuario.getUsername());
            sentencia.setString(11, usuario.getPassword());
            sentencia.setString(12, usuario.getRol() != null ? usuario.getRol().name() : null);
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    usuario.setIdUsuario(claves.getInt(1));
                }
            }
        } catch (SQLException excepcion) {
            throw new DaoException("Error al registrar usuario: " + excepcion.getMessage(), excepcion);
        }
    }

    @Override
    public void actualizar(Usuario usuario) throws DaoException {
        try (Connection conexion = Conexion.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1,  usuario.getNombre());
            sentencia.setString(2,  usuario.getApellidos());
            sentencia.setString(3,  usuario.getTelefono());
            sentencia.setString(4,  usuario.getEmail());
            sentencia.setString(5,  usuario.getCalle());
            sentencia.setString(6,  usuario.getNumero());
            sentencia.setString(7,  usuario.getCodigoPostal());
            sentencia.setString(8,  usuario.getCiudad());
            sentencia.setString(9,  usuario.getTipo().name());
            sentencia.setString(10, usuario.getUsername());
            sentencia.setString(11, usuario.getRol() != null ? usuario.getRol().name() : null);
            sentencia.setInt(12,    usuario.getIdUsuario());
            sentencia.executeUpdate();
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
                    return mapearUsuario(resultado);
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
            for (int indice = 1; indice <= 5; indice++) {
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
                listaClientes.add(mapearUsuario(resultado));
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

    private Usuario mapearUsuario(ResultSet resultado) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setNombre(resultado.getString("nombre"));
        usuario.setApellidos(resultado.getString("apellidos"));
        usuario.setTelefono(resultado.getString("telefono"));
        usuario.setEmail(resultado.getString("email"));
        usuario.setCalle(resultado.getString("calle"));
        usuario.setNumero(resultado.getString("numero"));
        usuario.setCodigoPostal(resultado.getString("codigo_postal"));
        usuario.setCiudad(resultado.getString("ciudad"));
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
