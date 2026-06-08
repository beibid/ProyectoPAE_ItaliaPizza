package italiapizza.controlador;

import italiapizza.dao.impl.PedidoDaoImpl;
import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Pedido;
import italiapizza.modelo.Usuario;
import italiapizza.util.ExportadorUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class PedidosControlador {

    @FXML private ComboBox<String>         comboTipoFiltro;
    @FXML private ComboBox<Usuario>        comboClienteFiltro;
    @FXML private DatePicker               selectorFecha;
    @FXML private ComboBox<Pedido.Estatus> comboEstatusFiltro;
    @FXML private TableView<Pedido>        tablaPedidos;
    @FXML private TableColumn<Pedido, Integer> columnaIdentificador;
    @FXML private TableColumn<Pedido, String>  columnaCliente;
    @FXML private TableColumn<Pedido, String>  columnaFecha;
    @FXML private TableColumn<Pedido, Double>  columnaTotal;
    @FXML private TableColumn<Pedido, String>  columnaEstatus;
    @FXML private Button botonEditarPedido;
    @FXML private Button botonCambiarEstatus;

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PedidoDaoImpl  pedidoDao  = new PedidoDaoImpl();
    private final UsuarioDaoImpl usuarioDao = new UsuarioDaoImpl();
    private List<Pedido> pedidosCargados;

    @FXML
    public void initialize() {
        comboTipoFiltro.getItems().addAll("Todos", "Por Cliente", "Por Fecha", "Por Estatus");
        comboTipoFiltro.setValue("Todos");
        comboClienteFiltro.getItems().addAll(obtenerClientes());
        comboEstatusFiltro.getItems().addAll(Pedido.Estatus.values());

        columnaIdentificador.setCellValueFactory(
                dato -> new SimpleObjectProperty<>(dato.getValue().getIdPedido()));
        columnaCliente.setCellValueFactory(
                dato -> new SimpleStringProperty(dato.getValue().getCliente().toString()));
        columnaFecha.setCellValueFactory(
                dato -> new SimpleStringProperty(
                        dato.getValue().getFecha().format(FORMATO_FECHA_HORA)));
        columnaTotal.setCellValueFactory(
                dato -> new SimpleObjectProperty<>(dato.getValue().getTotal()));
        columnaEstatus.setCellValueFactory(
                dato -> new SimpleStringProperty(dato.getValue().getEstatus().name()));

        tablaPedidos.getSelectionModel().selectedItemProperty()
                .addListener((observador, seleccionAnterior, seleccionActual) -> {
                    boolean haySeleccion  = seleccionActual != null;
                    boolean estaEnProceso = haySeleccion
                            && seleccionActual.getEstatus() == Pedido.Estatus.EN_PROCESO;
                    botonEditarPedido.setDisable(!estaEnProceso);
                    botonCambiarEstatus.setDisable(!haySeleccion);
                });

        cargarTodosLosPedidos();
    }

    @FXML
    public void actualizarVisibilidadFiltros() {
        String tipoSeleccionado = comboTipoFiltro.getValue();
        comboClienteFiltro.setVisible("Por Cliente".equals(tipoSeleccionado));
        comboClienteFiltro.setManaged("Por Cliente".equals(tipoSeleccionado));
        selectorFecha.setVisible("Por Fecha".equals(tipoSeleccionado));
        selectorFecha.setManaged("Por Fecha".equals(tipoSeleccionado));
        comboEstatusFiltro.setVisible("Por Estatus".equals(tipoSeleccionado));
        comboEstatusFiltro.setManaged("Por Estatus".equals(tipoSeleccionado));
    }

    @FXML
    public void ejecutarBusqueda() {
        String         tipoFiltro    = comboTipoFiltro.getValue();
        Usuario        clienteFiltro = comboClienteFiltro.getValue();
        LocalDate      fechaFiltro   = selectorFecha.getValue();
        Pedido.Estatus estatusFiltro = comboEstatusFiltro.getValue();

        List<Pedido> resultadoBusqueda = switch (tipoFiltro) {
            case "Por Cliente" -> clienteFiltro != null
                    ? buscarPorCliente(clienteFiltro.getIdUsuario())
                    : obtenerTodos();
            case "Por Fecha"   -> fechaFiltro != null
                    ? buscarPorFecha(fechaFiltro)
                    : obtenerTodos();
            case "Por Estatus" -> estatusFiltro != null
                    ? buscarPorEstatus(estatusFiltro)
                    : obtenerTodos();
            default            -> obtenerTodos();
        };

        pedidosCargados = resultadoBusqueda;
        tablaPedidos.setItems(FXCollections.observableArrayList(resultadoBusqueda));
    }

    @FXML
    public void abrirFormularioNuevoPedido() {
        abrirFormularioPedido(null);
    }

    @FXML
    public void abrirFormularioEditarPedido() {
        Pedido pedidoSeleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
        if (pedidoSeleccionado != null) {
            abrirFormularioPedido(pedidoSeleccionado);
        }
    }

    @FXML
    public void ejecutarCambioEstatus() {
        Pedido pedidoSeleccionado = tablaPedidos.getSelectionModel().getSelectedItem();
        if (pedidoSeleccionado == null) {
            return;
        }
        ChoiceDialog<Pedido.Estatus> dialogoEleccion = new ChoiceDialog<>(
                pedidoSeleccionado.getEstatus(), Pedido.Estatus.values());
        dialogoEleccion.setTitle("Cambiar Estatus");
        dialogoEleccion.setHeaderText("Selecciona el nuevo estatus para el pedido #"
                + pedidoSeleccionado.getIdPedido());
        dialogoEleccion.showAndWait().ifPresent(nuevoEstatus -> {
            String mensajeError = cambiarEstatus(pedidoSeleccionado.getIdPedido(), nuevoEstatus);
            if (mensajeError != null) {
                new Alert(Alert.AlertType.ERROR, mensajeError).showAndWait();
            } else {
                cargarTodosLosPedidos();
            }
        });
    }

    @FXML
    public void exportarCSV() {
        if (pedidosCargados != null) {
            ExportadorUtil.exportarCSV(pedidosCargados,
                    (Stage) tablaPedidos.getScene().getWindow());
        }
    }

    @FXML
    public void exportarPDF() {
        if (pedidosCargados != null) {
            ExportadorUtil.exportarPDF(pedidosCargados,
                    (Stage) tablaPedidos.getScene().getWindow());
        }
    }

    public void cargarTodosLosPedidos() {
        pedidosCargados = obtenerTodos();
        tablaPedidos.setItems(FXCollections.observableArrayList(pedidosCargados));
    }

    private String cambiarEstatus(int idPedido, Pedido.Estatus nuevoEstatus) {
        try {
            pedidoDao.actualizarEstatus(idPedido, nuevoEstatus);
            return null;
        } catch (DaoException excepcion) {
            return "Error al cambiar estatus: " + excepcion.getMessage();
        }
    }

    private List<Pedido> obtenerTodos() {
        try {
            return pedidoDao.buscarTodos();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private List<Pedido> buscarPorCliente(int idCliente) {
        try {
            return pedidoDao.buscarPorCliente(idCliente);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private List<Pedido> buscarPorFecha(LocalDate fecha) {
        try {
            return pedidoDao.buscarPorFecha(fecha);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private List<Pedido> buscarPorEstatus(Pedido.Estatus estatus) {
        try {
            return pedidoDao.buscarPorEstatus(estatus);
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private List<Usuario> obtenerClientes() {
        try {
            return usuarioDao.buscarClientes();
        } catch (DaoException excepcion) {
            return Collections.emptyList();
        }
    }

    private void abrirFormularioPedido(Pedido pedidoEditar) {
        try {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource("/italiapizza/fxml/PedidoForm.fxml"));
            Parent raiz = cargador.load();

            PedidoFormControlador controladorFormulario = cargador.getController();
            controladorFormulario.inicializar(pedidoEditar, this::cargarTodosLosPedidos);

            Stage ventanaFormulario = new Stage();
            ventanaFormulario.initOwner(tablaPedidos.getScene().getWindow());
            ventanaFormulario.initModality(Modality.WINDOW_MODAL);
            ventanaFormulario.setTitle(pedidoEditar == null
                    ? "Nuevo Pedido"
                    : "Editar Pedido #" + pedidoEditar.getIdPedido());
            ventanaFormulario.setScene(new Scene(raiz, 580, 540));
            ventanaFormulario.show();
        } catch (IOException excepcion) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al abrir el formulario de pedido.").showAndWait();
        }
    }
}
