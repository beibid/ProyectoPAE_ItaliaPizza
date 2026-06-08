package italiapizza.controlador;

import italiapizza.dao.impl.PedidoDaoImpl;
import italiapizza.dao.impl.ProductoDaoImpl;
import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.excepcion.ExistenciaInsuficienteException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import italiapizza.modelo.DetallePedido;
import italiapizza.modelo.Pedido;
import italiapizza.modelo.Producto;
import italiapizza.modelo.Usuario;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PedidoFormControlador {

    @FXML private ComboBox<Usuario>            comboClientePedido;
    @FXML private ComboBox<Producto>           comboProductoAgregar;
    @FXML private Spinner<Integer>             spinnerCantidad;
    @FXML private TableView<DetallePedido>     tablaDetallesPedido;
    @FXML private TableColumn<DetallePedido, String>  columnaProducto;
    @FXML private TableColumn<DetallePedido, Integer> columnaCantidad;
    @FXML private TableColumn<DetallePedido, Double>  columnaPrecioUnit;
    @FXML private TableColumn<DetallePedido, Double>  columnaSubtotal;
    @FXML private Label etiquetaTotalPedido;
    @FXML private Label etiquetaError;

    private final PedidoDaoImpl   pedidoDao   = new PedidoDaoImpl();
    private final UsuarioDaoImpl  usuarioDao  = new UsuarioDaoImpl();
    private final ProductoDaoImpl productoDao = new ProductoDaoImpl();

    private final ObservableList<DetallePedido> listaDetalles =
            FXCollections.observableArrayList();

    private Pedido   pedidoEnEdicion;
    private Runnable accionAlGuardar;

    @FXML
    public void initialize() {
        comboClientePedido.getItems().addAll(obtenerClientes());
        comboProductoAgregar.getItems().addAll(obtenerTodosLosProductos());

        columnaProducto.setCellValueFactory(
                dato -> new SimpleStringProperty(dato.getValue().getProducto().getNombre()));
        columnaCantidad.setCellValueFactory(
                dato -> new SimpleIntegerProperty(dato.getValue().getCantidad()).asObject());
        columnaPrecioUnit.setCellValueFactory(
                dato -> new SimpleDoubleProperty(dato.getValue().getPrecioUnit()).asObject());
        columnaSubtotal.setCellValueFactory(
                dato -> new SimpleDoubleProperty(dato.getValue().getSubtotal()).asObject());

        tablaDetallesPedido.setItems(listaDetalles);
    }

    public void inicializar(Pedido pedidoEditar, Runnable accionAlGuardar) {
        this.pedidoEnEdicion = pedidoEditar;
        this.accionAlGuardar = accionAlGuardar;
        if (pedidoEditar != null) {
            comboClientePedido.setValue(pedidoEditar.getCliente());
            comboClientePedido.setDisable(true);
            listaDetalles.addAll(pedidoEditar.getDetalles());
            actualizarEtiquetaTotal();
        }
    }

    @FXML
    public void ejecutarAgregarProducto() {
        Producto productoSeleccionado = comboProductoAgregar.getValue();
        if (productoSeleccionado == null) {
            return;
        }
        int cantidadAgregar = spinnerCantidad.getValue();
        listaDetalles.stream()
                .filter(detalle -> detalle.getProducto().getIdProducto()
                        == productoSeleccionado.getIdProducto())
                .findFirst()
                .ifPresentOrElse(
                        detalleExistente -> detalleExistente.setCantidad(
                                detalleExistente.getCantidad() + cantidadAgregar),
                        () -> listaDetalles.add(
                                new DetallePedido(productoSeleccionado, cantidadAgregar))
                );
        tablaDetallesPedido.refresh();
        actualizarEtiquetaTotal();
    }

    @FXML
    public void ejecutarQuitarProducto() {
        DetallePedido detalleSeleccionado =
                tablaDetallesPedido.getSelectionModel().getSelectedItem();
        if (detalleSeleccionado != null) {
            listaDetalles.remove(detalleSeleccionado);
            actualizarEtiquetaTotal();
        }
    }

    @FXML
    public void ejecutarGuardarPedido() {
        Pedido pedido = pedidoEnEdicion != null ? pedidoEnEdicion : new Pedido();
        Usuario clienteSeleccionado = comboClientePedido.getValue();
        pedido.setCliente(clienteSeleccionado);
        pedido.setIdCliente(clienteSeleccionado != null ? clienteSeleccionado.getIdCliente() : 0);
        pedido.setDetalles(new ArrayList<>(listaDetalles));

        String mensajeError = pedidoEnEdicion == null
                ? registrar(pedido)
                : actualizarDetalles(pedido);

        if (mensajeError != null) {
            etiquetaError.setText(mensajeError);
        } else {
            accionAlGuardar.run();
            cerrarVentana();
        }
    }

    @FXML
    public void ejecutarCancelar() {
        cerrarVentana();
    }

    private String registrar(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) {
            return "El pedido debe tener al menos un producto.";
        }
        if (pedido.getCliente() == null) {
            return "Selecciona un cliente.";
        }
        pedido.recalcularTotal();
        try {
            pedidoDao.registrar(pedido);
            return null;
        } catch (ExistenciaInsuficienteException excepcion) {
            new Alert(Alert.AlertType.ERROR, excepcion.getMessage(), ButtonType.OK).showAndWait();
            return null;
        } catch (DaoException excepcion) {
            return "Error al guardar pedido: " + excepcion.getMessage();
        }
    }

    private String actualizarDetalles(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) {
            return "El pedido debe tener al menos un producto.";
        }
        pedido.recalcularTotal();
        try {
            pedidoDao.actualizarDetalles(pedido);
            return null;
        } catch (DaoException excepcion) {
            return "Error al actualizar pedido: " + excepcion.getMessage();
        }
    }

    private List<Usuario> obtenerClientes() {
        try {
            return usuarioDao.buscarClientes();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private List<Producto> obtenerTodosLosProductos() {
        try {
            return productoDao.buscarTodos();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private void actualizarEtiquetaTotal() {
        double totalCalculado = listaDetalles.stream()
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
        etiquetaTotalPedido.setText(String.format("Total: $%.2f", totalCalculado));
    }

    private void cerrarVentana() {
        ((Stage) etiquetaTotalPedido.getScene().getWindow()).close();
    }
}
