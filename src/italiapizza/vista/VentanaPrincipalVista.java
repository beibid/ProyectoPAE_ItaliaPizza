package italiapizza.vista;

import italiapizza.util.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class VentanaPrincipalVista {

    @FXML private Label etiquetaBienvenida;
    @FXML private Label etiquetaEstado;
    @FXML private Menu  menuAdministracion;
    @FXML private Menu  menuInventarios;

    @FXML
    public void initialize() {
        boolean esAdministrador = Sesion.getInstancia().esAdministrador();
        String  nombreUsuario   = Sesion.getInstancia().getUsuarioActual().getNombre();

        etiquetaBienvenida.setText("¡Bienvenido, " + nombreUsuario + "!");
        etiquetaEstado.setText("  Sesión: " + nombreUsuario
                + " | Rol: " + Sesion.getInstancia().getUsuarioActual().getRol());

        menuAdministracion.setDisable(!esAdministrador);
        menuInventarios.setDisable(!esAdministrador);
    }

    @FXML
    public void abrirGestionUsuarios() {
        abrirVentanaModal("/italiapizza/vista/Usuarios.fxml", "Gestión de Usuarios", 780, 500);
    }

    @FXML
    public void abrirGestionProductos() {
        abrirVentanaModal("/italiapizza/vista/Productos.fxml", "Gestión de Productos", 760, 480);
    }

    @FXML
    public void abrirValidacionInventario() {
        abrirVentanaModal("/italiapizza/vista/ValidacionInventario.fxml",
                "Validación de Inventario", 720, 460);
    }

    @FXML
    public void abrirGestionPedidos() {
        abrirVentanaModal("/italiapizza/vista/Pedidos.fxml", "Gestión de Pedidos", 900, 520);
    }

    @FXML
    public void ejecutarCierreSesion() {
        Sesion.getInstancia().cerrarSesion();
        try {
            Stage escenario = (Stage) etiquetaEstado.getScene().getWindow();
            LoginVista.mostrarEn(escenario);
        } catch (IOException excepcion) {
            excepcion.printStackTrace();
        }
    }

    private void abrirVentanaModal(String rutaFxml, String titulo, double ancho, double alto) {
        try {
            FXMLLoader cargador = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent raiz = cargador.load();
            Stage ventanaModal = new Stage();
            ventanaModal.initOwner(etiquetaEstado.getScene().getWindow());
            ventanaModal.initModality(Modality.WINDOW_MODAL);
            ventanaModal.setTitle(titulo);
            ventanaModal.setScene(new Scene(raiz, ancho, alto));
            ventanaModal.show();
        } catch (IOException excepcion) {
            excepcion.printStackTrace();
        }
    }
}
