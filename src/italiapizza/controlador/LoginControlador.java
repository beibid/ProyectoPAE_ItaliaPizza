package italiapizza.controlador;

import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.CredencialesInvalidasException;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Usuario;
import italiapizza.util.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginControlador {

    @FXML private TextField     campoNombreUsuario;
    @FXML private PasswordField campoContrasena;
    @FXML private Label         etiquetaError;

    private final UsuarioDaoImpl usuarioDao = new UsuarioDaoImpl();

    @FXML
    public void initialize() {
        campoContrasena.setOnAction(evento -> manejarIngresoSesion());
    }

    @FXML
    public void manejarIngresoSesion() {
        String nombreUsuario = campoNombreUsuario.getText().trim();
        String contrasena    = campoContrasena.getText();

        String mensajeError = iniciarSesion(nombreUsuario, contrasena);

        if (mensajeError == null) {
            abrirVentanaPrincipal();
        } else {
            etiquetaError.setText(mensajeError);
            etiquetaError.setVisible(true);
            etiquetaError.setManaged(true);
        }
    }

    String iniciarSesion(String nombreUsuario, String contrasena) {
        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            return "Por favor, ingresa usuario y contraseña.";
        }
        try {
            Usuario usuarioEncontrado = usuarioDao.buscarPorCredenciales(nombreUsuario, contrasena);
            if (usuarioEncontrado == null) {
                throw new CredencialesInvalidasException();
            }
            Sesion.getInstancia().setUsuarioActual(usuarioEncontrado);
            return null;
        } catch (CredencialesInvalidasException excepcion) {
            return excepcion.getMessage();
        } catch (DaoException excepcion) {
            Throwable causa = excepcion.getCause();
            return "Error BD: " + (causa != null ? causa.getMessage() : excepcion.getMessage());
        }
    }

    private void abrirVentanaPrincipal() {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource("/italiapizza/fxml/VentanaPrincipal.fxml"));
            Parent raiz = cargador.load();
            Stage escenario = (Stage) campoNombreUsuario.getScene().getWindow();
            escenario.setScene(new Scene(raiz, 900, 600));
            escenario.setTitle("Italia Pizza - Sistema de Administración");
            escenario.setResizable(true);
        } catch (IOException excepcion) {
            etiquetaError.setText("Error al cargar la ventana principal.");
            etiquetaError.setVisible(true);
            etiquetaError.setManaged(true);
        }
    }

    public static void mostrarEn(Stage escenario) throws IOException {
        FXMLLoader cargador = new FXMLLoader(
                LoginControlador.class.getResource("/italiapizza/fxml/Login.fxml"));
        Parent raiz = cargador.load();
        escenario.setScene(new Scene(raiz, 480, 420));
        escenario.setTitle("Italia Pizza - Acceso");
        escenario.setResizable(false);
        escenario.show();
    }
}
