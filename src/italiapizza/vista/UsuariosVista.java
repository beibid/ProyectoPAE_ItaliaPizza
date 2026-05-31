package italiapizza.vista;

import italiapizza.controlador.UsuarioControlador;
import italiapizza.modelo.Usuario;
import italiapizza.util.Sesion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class UsuariosVista {

    @FXML private TextField              campoBusqueda;
    @FXML private TableView<Usuario>     tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> columnaIdentificador;
    @FXML private TableColumn<Usuario, String>  columnaNombre;
    @FXML private TableColumn<Usuario, String>  columnaApellidos;
    @FXML private TableColumn<Usuario, String>  columnaTelefono;
    @FXML private TableColumn<Usuario, String>  columnaTipo;
    @FXML private TableColumn<Usuario, String>  columnaRol;
    @FXML private Button botonEditarUsuario;
    @FXML private Button botonEliminarUsuario;

    private final UsuarioControlador usuarioControlador = new UsuarioControlador();

    @FXML
    public void initialize() {
        columnaIdentificador.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        columnaTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        columnaTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        columnaRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        tablaUsuarios.getSelectionModel().selectedItemProperty()
                .addListener((observador, seleccionAnterior, seleccionActual) -> {
                    boolean haySeleccion = seleccionActual != null;
                    botonEditarUsuario.setDisable(!haySeleccion);
                    botonEliminarUsuario.setDisable(!haySeleccion);
                });

        cargarTodosLosUsuarios();
    }

    @FXML
    public void ejecutarBusqueda() {
        String textoBusqueda = campoBusqueda.getText();
        if (textoBusqueda.isBlank()) {
            cargarTodosLosUsuarios();
            return;
        }
        tablaUsuarios.setItems(FXCollections.observableArrayList(
                usuarioControlador.buscarPorFiltro(textoBusqueda)));
    }

    @FXML
    public void ejecutarLimpiarBusqueda() {
        campoBusqueda.clear();
        cargarTodosLosUsuarios();
    }

    @FXML
    public void abrirFormularioNuevoUsuario() {
        abrirFormularioUsuario(null);
    }

    @FXML
    public void abrirFormularioEditarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado != null) {
            abrirFormularioUsuario(usuarioSeleccionado);
        }
    }

    @FXML
    public void ejecutarEliminarUsuario() {
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar a " + usuarioSeleccionado.getNombre()
                        + " " + usuarioSeleccionado.getApellidos() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                int identificadorSesionActual =
                        Sesion.getInstancia().getUsuarioActual().getIdUsuario();
                String mensajeError = usuarioControlador.eliminar(
                        usuarioSeleccionado, identificadorSesionActual);
                if (mensajeError != null) {
                    new Alert(Alert.AlertType.ERROR, mensajeError, ButtonType.OK).showAndWait();
                } else {
                    cargarTodosLosUsuarios();
                }
            }
        });
    }

    public void cargarTodosLosUsuarios() {
        tablaUsuarios.setItems(FXCollections.observableArrayList(
                usuarioControlador.obtenerTodos()));
    }

    private void abrirFormularioUsuario(Usuario usuarioEditar) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource("/italiapizza/vista/UsuarioForm.fxml"));
            Parent raiz = cargador.load();

            UsuarioFormVista controladorFormulario = cargador.getController();
            controladorFormulario.inicializar(usuarioEditar, this::cargarTodosLosUsuarios);

            Stage ventanaFormulario = new Stage();
            ventanaFormulario.initOwner(tablaUsuarios.getScene().getWindow());
            ventanaFormulario.initModality(Modality.WINDOW_MODAL);
            ventanaFormulario.setTitle(usuarioEditar == null ? "Nuevo Usuario" : "Editar Usuario");
            ventanaFormulario.setScene(new Scene(raiz, 420, 520));
            ventanaFormulario.show();
        } catch (IOException excepcion) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al abrir el formulario de usuario.").showAndWait();
        }
    }
}
