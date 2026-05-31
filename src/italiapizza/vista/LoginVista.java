package italiapizza.vista;

import italiapizza.controlador.LoginControlador;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginVista {

    @FXML private TextField     campoNombreUsuario;
    @FXML private PasswordField campoContrasena;
    @FXML private Label         etiquetaError;

    private final LoginControlador loginControlador = new LoginControlador();

    @FXML
    public void initialize() {
        campoContrasena.setOnAction(evento -> manejarIngresoSesion());
    }

    @FXML
    public void manejarIngresoSesion() {
        String nombreUsuario = campoNombreUsuario.getText().trim();
        String contrasena    = campoContrasena.getText();

        String mensajeError = loginControlador.iniciarSesion(nombreUsuario, contrasena);

        if (mensajeError == null) {
            abrirVentanaPrincipal();
        } else {
            etiquetaError.setText(mensajeError);
            etiquetaError.setVisible(true);
            etiquetaError.setManaged(true);
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
                LoginVista.class.getResource("/italiapizza/fxml/Login.fxml"));
        Parent raiz = cargador.load();
        escenario.setScene(new Scene(raiz, 480, 420));
        escenario.setTitle("Italia Pizza - Acceso");
        escenario.setResizable(false);
        escenario.show();
    }
}