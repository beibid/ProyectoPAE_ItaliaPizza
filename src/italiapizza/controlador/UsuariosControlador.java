package italiapizza.controlador;

import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.UsuarioNoEliminableException;
import italiapizza.modelo.Usuario;
import italiapizza.util.Sesion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class UsuariosControlador {

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

    private final UsuarioDaoImpl usuarioDao = new UsuarioDaoImpl();

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
    }

    @FXML
    public void ejecutarBusqueda() {
        String textoBusqueda = campoBusqueda.getText();
        if (textoBusqueda.isBlank()) {
            cargarTodosLosUsuarios();
            return;
        }
        tablaUsuarios.setItems(FXCollections.observableArrayList(
                buscarPorFiltro(textoBusqueda)));
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
                String mensajeError = eliminar(usuarioSeleccionado, identificadorSesionActual);
                if (mensajeError != null) {
                    new Alert(Alert.AlertType.ERROR, mensajeError, ButtonType.OK).showAndWait();
                } else {
                    cargarTodosLosUsuarios();
                }
            }
        });
    }

    public void cargarTodosLosUsuarios() {
        tablaUsuarios.setItems(FXCollections.observableArrayList(obtenerTodos()));
    }

    private String eliminar(Usuario usuario, int idUsuarioEnSesion) {
        try {
            if (usuario.getIdUsuario() == idUsuarioEnSesion) {
                throw new UsuarioNoEliminableException("No puedes eliminar tu propia cuenta.");
            }
            if (usuario.getTipo() == Usuario.Tipo.CLIENTE
                    && usuarioDao.tienePedidos(usuario.getIdUsuario())) {
                throw new UsuarioNoEliminableException(
                        "El cliente tiene pedidos registrados y no puede eliminarse.");
            }
            usuarioDao.eliminar(usuario.getIdUsuario());
            return null;
        } catch (UsuarioNoEliminableException excepcion) {
            return excepcion.getMessage();
        } catch (DaoException excepcion) {
            return "Error al eliminar: " + excepcion.getMessage();
        }
    }

    private List<Usuario> obtenerTodos() {
        try {
            return usuarioDao.buscarTodos();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private List<Usuario> buscarPorFiltro(String filtro) {
        try {
            return usuarioDao.buscarPorFiltro(filtro);
        } catch (DaoException excepcion) {
            new Alert(Alert.AlertType.ERROR, excepcion.getMessage(), ButtonType.OK).showAndWait();
            return Collections.emptyList();
        }
    }

    private void abrirFormularioUsuario(Usuario usuarioEditar) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource("/italiapizza/fxml/UsuarioForm.fxml"));
            Parent raiz = cargador.load();

            UsuarioFormControlador controladorFormulario = cargador.getController();
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
