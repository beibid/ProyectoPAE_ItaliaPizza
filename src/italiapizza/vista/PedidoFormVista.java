package italiapizza.vista;

import italiapizza.controlador.PedidoControlador;
import italiapizza.controlador.ProductoControlador;
import italiapizza.controlador.UsuarioControlador;
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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;

public class PedidoFormVista {

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

    private final PedidoControlador   pedidoControlador   = new PedidoControlador();
    private final UsuarioControlador  usuarioControlador  = new UsuarioControlador();
    private final ProductoControlador productoControlador = new ProductoControlador();

    private final ObservableList<DetallePedido> listaDetalles =
            FXCollections.observableArrayList();

    private Pedido   pedidoEnEdicion;
    private Runnable accionAlGuardar;

    @FXML
    public void initialize() {
        comboClientePedido.getItems().addAll(usuarioControlador.obtenerClientes());
        comboProductoAgregar.getItems().addAll(productoControlador.obtenerTodos());

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
        pedido.setCliente(comboClientePedido.getValue());
        pedido.setDetalles(new ArrayList<>(listaDetalles));

        String mensajeError = pedidoEnEdicion == null
                ? pedidoControlador.registrar(pedido)
                : pedidoControlador.actualizarDetalles(pedido);

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
