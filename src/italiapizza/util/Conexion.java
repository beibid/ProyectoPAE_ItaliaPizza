package italiapizza.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexion {

    private static final String URL_BASE_DE_DATOS =
            "jdbc:mysql://localhost:3306/italiapizza?useSSL=false&serverTimezone=UTC";
    private static final String NOMBRE_USUARIO_BD = "root";
    private static final String CONTRASENA_BD     = "coca";

    private Conexion() {}

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_BASE_DE_DATOS, NOMBRE_USUARIO_BD, CONTRASENA_BD);
    }
}
