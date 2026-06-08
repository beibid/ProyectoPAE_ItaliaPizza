package italiapizza.controlador;

import italiapizza.dao.impl.ProductoDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.ProductoNoEliminableException;
import italiapizza.modelo.Producto;
import italiapizza.util.ExportadorUtil;
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

public class ProductosControlador {

    @FXML private TextField               campoBusqueda;
    @FXML private TableView<Producto>     tablaProductos;
    @FXML private TableColumn<Producto, String>  columnaCodigo;
    @FXML private TableColumn<Producto, String>  columnaNombre;
    @FXML private TableColumn<Producto, Double>  columnaPrecio;
    @FXML private TableColumn<Producto, Integer> columnaExistencia;
    @FXML private Button botonEditarProducto;
    @FXML private Button botonEliminarProducto;

    private final ProductoDaoImpl productoDao = new ProductoDaoImpl();

    @FXML
    public void initialize() {
        columnaCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        columnaNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnaExistencia.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        tablaProductos.getSelectionModel().selectedItemProperty()
                .addListener((observador, seleccionAnterior, seleccionActual) -> {
                    boolean haySeleccion = seleccionActual != null;
                    botonEditarProducto.setDisable(!haySeleccion);
                    botonEliminarProducto.setDisable(!haySeleccion);
                });

        cargarTodosLosProductos();
    }

    @FXML
    public void ejecutarBusqueda() {
        String textoBusqueda = campoBusqueda.getText();
        if (textoBusqueda.isBlank()) {
            cargarTodosLosProductos();
            return;
        }
        tablaProductos.setItems(FXCollections.observableArrayList(
                buscarPorFiltro(textoBusqueda)));
    }

    @FXML
    public void ejecutarLimpiarBusqueda() {
        campoBusqueda.clear();
        cargarTodosLosProductos();
    }

    @FXML
    public void abrirFormularioNuevoProducto() {
        abrirFormularioProducto(null);
    }

    @FXML
    public void abrirFormularioEditarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            abrirFormularioProducto(productoSeleccionado);
        }
    }

    @FXML
    public void ejecutarEliminarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto " + productoSeleccionado.getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                String mensajeError = eliminar(productoSeleccionado);
                if (mensajeError != null) {
                    new Alert(Alert.AlertType.ERROR, mensajeError).showAndWait();
                } else {
                    cargarTodosLosProductos();
                }
            }
        });
    }

    @FXML
    public void generarReporteInventario() {
        Stage escenario = (Stage) tablaProductos.getScene().getWindow();
        ExportadorUtil.exportarInventarioPDF(obtenerTodos(), escenario);
    }

    public void cargarTodosLosProductos() {
        tablaProductos.setItems(FXCollections.observableArrayList(obtenerTodos()));
    }

    private String eliminar(Producto producto) {
        try {
            if (productoDao.estaEnPedido(producto.getIdProducto())) {
                throw new ProductoNoEliminableException(
                        "El producto ya fue utilizado en pedidos y no puede eliminarse.");
            }
            productoDao.eliminar(producto.getIdProducto());
            return null;
        } catch (ProductoNoEliminableException excepcion) {
            return excepcion.getMessage();
        } catch (DaoException excepcion) {
            return "Error al eliminar: " + excepcion.getMessage();
        }
    }

    private List<Producto> obtenerTodos() {
        try {
            return productoDao.buscarTodos();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private List<Producto> buscarPorFiltro(String filtro) {
        try {
            return productoDao.buscarPorFiltro(filtro);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private void abrirFormularioProducto(Producto productoEditar) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource("/italiapizza/fxml/ProductoForm.fxml"));
            Parent raiz = cargador.load();

            ProductoFormControlador controladorFormulario = cargador.getController();
            controladorFormulario.inicializar(productoEditar, this::cargarTodosLosProductos);

            Stage ventanaFormulario = new Stage();
            ventanaFormulario.initOwner(tablaProductos.getScene().getWindow());
            ventanaFormulario.initModality(Modality.WINDOW_MODAL);
            ventanaFormulario.setTitle(
                    productoEditar == null ? "Nuevo Producto" : "Editar Producto");
            ventanaFormulario.setScene(new Scene(raiz, 440, 480));
            ventanaFormulario.show();
        } catch (IOException excepcion) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al abrir el formulario de producto.").showAndWait();
        }
    }
}
