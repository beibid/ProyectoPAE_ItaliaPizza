package italiapizza.vista;

import italiapizza.controlador.ProductoControlador;
import italiapizza.modelo.Producto;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidacionInventarioVista {

    @FXML private ComboBox<Producto>                        comboProductoValidar;
    @FXML private Spinner<Integer>                          spinnerCantidadFisica;
    @FXML private TableView<Map<String, Object>>            tablaValidacion;
    @FXML private TableColumn<Map<String, Object>, String>  columnaNombreProducto;
    @FXML private TableColumn<Map<String, Object>, Integer> columnaExistenciaSistema;
    @FXML private TableColumn<Map<String, Object>, Integer> columnaExistenciaFisica;
    @FXML private TableColumn<Map<String, Object>, String>  columnaDiferencia;

    private final ProductoControlador productoControlador = new ProductoControlador();

    @FXML
    public void initialize() {
        List<Producto> listaProductos = productoControlador.obtenerTodos();
        comboProductoValidar.setItems(FXCollections.observableArrayList(listaProductos));

        columnaNombreProducto.setCellValueFactory(dato ->
                new javafx.beans.property.SimpleStringProperty(
                        (String) dato.getValue().get("nombre")));
        columnaExistenciaSistema.setCellValueFactory(dato ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        (Integer) dato.getValue().get("sistema")));
        columnaExistenciaFisica.setCellValueFactory(dato ->
                new javafx.beans.property.SimpleObjectProperty<>(
                        (Integer) dato.getValue().get("fisico")));
        columnaDiferencia.setCellValueFactory(dato ->
                new javafx.beans.property.SimpleStringProperty(
                        (String) dato.getValue().get("diferencia")));

        columnaDiferencia.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String valorDiferencia, boolean estaVacio) {
                super.updateItem(valorDiferencia, estaVacio);
                if (estaVacio || valorDiferencia == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                    return;
                }
                setText(valorDiferencia);
                if (valorDiferencia.startsWith("-")) {
                    setTextFill(Color.RED);
                } else if (valorDiferencia.startsWith("+")) {
                    setTextFill(Color.GREEN);
                } else {
                    setTextFill(Color.GRAY);
                }
            }
        });

        listaProductos.forEach(producto -> {
            Map<String, Object> filaProducto = new HashMap<>();
            filaProducto.put("id",         producto.getIdProducto());
            filaProducto.put("nombre",     producto.getNombre());
            filaProducto.put("sistema",    producto.getCantidad());
            filaProducto.put("fisico",     producto.getCantidad());
            filaProducto.put("diferencia", "---");
            tablaValidacion.getItems().add(filaProducto);
        });
    }

    @FXML
    public void aplicarValidacion() {
        Producto productoSeleccionado = comboProductoValidar.getValue();
        if (productoSeleccionado == null) {
            return;
        }
        int cantidadFisicaIngresada = spinnerCantidadFisica.getValue();

        tablaValidacion.getItems().stream()
                .filter(fila -> fila.get("id").equals(productoSeleccionado.getIdProducto()))
                .findFirst()
                .ifPresent(fila -> {
                    int cantidadSistema = (Integer) fila.get("sistema");
                    int diferencia      = cantidadFisicaIngresada - cantidadSistema;
                    fila.put("fisico",     cantidadFisicaIngresada);
                    fila.put("diferencia", diferencia == 0
                            ? "OK"
                            : (diferencia > 0
                                    ? "+" + diferencia
                                    : String.valueOf(diferencia)));
                    tablaValidacion.refresh();
                });
    }
}
