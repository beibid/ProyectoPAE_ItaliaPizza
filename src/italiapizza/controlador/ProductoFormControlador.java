package italiapizza.controlador;

import italiapizza.dao.impl.ProductoDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Producto;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ProductoFormControlador {

    @FXML private TextField  campoCodigo;
    @FXML private TextField  campoNombre;
    @FXML private TextArea   campoDescripcion;
    @FXML private TextField  campoPrecio;
    @FXML private TextField  campoRestricciones;
    @FXML private TextField  campoCantidad;
    @FXML private ImageView  imagenProducto;
    @FXML private Label      etiquetaError;

    private final ProductoDaoImpl productoDao = new ProductoDaoImpl();
    private Producto productoEnEdicion;
    private Runnable accionAlGuardar;
    private byte[]   bytesImagenSeleccionada;

    public void inicializar(Producto productoEditar, Runnable accionAlGuardar) {
        this.productoEnEdicion = productoEditar;
        this.accionAlGuardar   = accionAlGuardar;
        if (productoEditar != null) {
            rellenarCamposConProducto(productoEditar);
        }
    }

    private void rellenarCamposConProducto(Producto producto) {
        campoCodigo.setText(producto.getCodigo());
        campoCodigo.setDisable(true);
        campoNombre.setText(producto.getNombre());
        campoDescripcion.setText(producto.getDescripcion());
        campoPrecio.setText(String.valueOf(producto.getPrecio()));
        campoRestricciones.setText(producto.getRestricciones());
        campoCantidad.setText(String.valueOf(producto.getCantidad()));
        bytesImagenSeleccionada = producto.getFoto();
        if (bytesImagenSeleccionada != null) {
            imagenProducto.setImage(
                    new Image(new ByteArrayInputStream(bytesImagenSeleccionada)));
        }
    }

    @FXML
    public void seleccionarFotoProducto() {
        FileChooser selectorArchivo = new FileChooser();
        selectorArchivo.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivoSeleccionado = selectorArchivo.showOpenDialog(
                campoCodigo.getScene().getWindow());
        if (archivoSeleccionado != null) {
            try {
                bytesImagenSeleccionada = Files.readAllBytes(archivoSeleccionado.toPath());
                imagenProducto.setImage(
                        new Image(new ByteArrayInputStream(bytesImagenSeleccionada)));
            } catch (IOException excepcion) {
                etiquetaError.setText("No se pudo cargar la imagen.");
            }
        }
    }

    @FXML
    public void ejecutarGuardarProducto() {
        try {
            Producto producto = productoEnEdicion != null ? productoEnEdicion : new Producto();
            producto.setCodigo(campoCodigo.getText().trim());
            producto.setNombre(campoNombre.getText().trim());
            producto.setDescripcion(campoDescripcion.getText().trim());
            producto.setPrecio(Double.parseDouble(campoPrecio.getText().trim()));
            producto.setRestricciones(campoRestricciones.getText().trim());
            producto.setCantidad(Integer.parseInt(campoCantidad.getText().trim()));
            producto.setFoto(bytesImagenSeleccionada);

            String mensajeError = productoEnEdicion == null
                    ? registrar(producto)
                    : actualizar(producto);

            if (mensajeError != null) {
                etiquetaError.setText(mensajeError);
            } else {
                accionAlGuardar.run();
                cerrarVentana();
            }
        } catch (NumberFormatException excepcion) {
            etiquetaError.setText("Precio y cantidad deben ser valores numéricos válidos.");
        }
    }

    @FXML
    public void ejecutarCancelar() {
        cerrarVentana();
    }

    private String registrar(Producto producto) {
        String mensajeValidacion = validarProducto(producto);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            productoDao.registrar(producto);
            return null;
        } catch (DaoException excepcion) {
            return "Error al guardar: " + excepcion.getMessage();
        }
    }

    private String actualizar(Producto producto) {
        String mensajeValidacion = validarProducto(producto);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            productoDao.actualizar(producto);
            return null;
        } catch (DaoException excepcion) {
            return "Error al actualizar: " + excepcion.getMessage();
        }
    }

    private String validarProducto(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            return "El nombre es requerido.";
        }
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            return "El código es requerido.";
        }
        if (producto.getPrecio() <= 0) {
            return "El precio debe ser mayor a 0.";
        }
        return null;
    }

    private void cerrarVentana() {
        ((Stage) campoCodigo.getScene().getWindow()).close();
    }
}
