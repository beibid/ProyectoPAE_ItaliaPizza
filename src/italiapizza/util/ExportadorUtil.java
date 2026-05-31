package italiapizza.util;

import italiapizza.modelo.DetallePedido;
import italiapizza.modelo.Pedido;
import italiapizza.modelo.Producto;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utilidad para exportar datos del sistema a archivos CSV y PDF (texto estructurado).
 */
public class ExportadorUtil {

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExportadorUtil() {}

    public static void exportarCSV(List<Pedido> listaPedidos, Stage escenarioPropietario) {
        FileChooser selectorArchivo = new FileChooser();
        selectorArchivo.setTitle("Guardar CSV");
        selectorArchivo.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV", "*.csv"));
        selectorArchivo.setInitialFileName("pedidos.csv");

        File archivoDestino = selectorArchivo.showSaveDialog(escenarioPropietario);
        if (archivoDestino == null) {
            return;
        }

        try (PrintWriter escritor = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDestino), "UTF-8"))) {
            escritor.println("ID,Cliente,Fecha,Total,Estatus,Productos");
            for (Pedido pedido : listaPedidos) {
                StringBuilder nombresProductos = new StringBuilder();
                for (DetallePedido detalle : pedido.getDetalles()) {
                    nombresProductos.append(detalle.getProducto().getNombre())
                            .append(" x").append(detalle.getCantidad()).append("; ");
                }
                escritor.printf("%d,\"%s\",%s,%.2f,%s,\"%s\"%n",
                        pedido.getIdPedido(),
                        pedido.getCliente().toString(),
                        pedido.getFecha().format(FORMATO_FECHA_HORA),
                        pedido.getTotal(),
                        pedido.getEstatus().name(),
                        nombresProductos.toString().trim());
            }
            mostrarAlertaInformacion("Exportación exitosa", "Archivo CSV guardado correctamente.");
        } catch (IOException excepcion) {
            mostrarAlertaError("Error al exportar CSV: " + excepcion.getMessage());
        }
    }

    public static void exportarPDF(List<Pedido> listaPedidos, Stage escenarioPropietario) {
        FileChooser selectorArchivo = new FileChooser();
        selectorArchivo.setTitle("Guardar PDF");
        selectorArchivo.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        selectorArchivo.setInitialFileName("pedidos.pdf");

        File archivoDestino = selectorArchivo.showSaveDialog(escenarioPropietario);
        if (archivoDestino == null) {
            return;
        }

        try (PrintWriter escritor = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDestino), "UTF-8"))) {
            escritor.println("=== REPORTE DE PEDIDOS - ITALIA PIZZA ===\n");
            for (Pedido pedido : listaPedidos) {
                escritor.printf("Pedido #%d | %s | Cliente: %s | Total: $%.2f | Estatus: %s%n",
                        pedido.getIdPedido(),
                        pedido.getFecha().format(FORMATO_FECHA_HORA),
                        pedido.getCliente(),
                        pedido.getTotal(),
                        pedido.getEstatus());
                for (DetallePedido detalle : pedido.getDetalles()) {
                    escritor.printf("   - %s x%d @ $%.2f = $%.2f%n",
                            detalle.getProducto().getNombre(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnit(),
                            detalle.getSubtotal());
                }
                escritor.println();
            }
            mostrarAlertaInformacion("Exportación exitosa", "Archivo PDF guardado correctamente.");
        } catch (IOException excepcion) {
            mostrarAlertaError("Error al exportar PDF: " + excepcion.getMessage());
        }
    }

    public static void exportarInventarioPDF(List<Producto> listaProductos,
                                             Stage escenarioPropietario) {
        FileChooser selectorArchivo = new FileChooser();
        selectorArchivo.setTitle("Guardar Reporte de Inventario");
        selectorArchivo.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        selectorArchivo.setInitialFileName("inventario.pdf");

        File archivoDestino = selectorArchivo.showSaveDialog(escenarioPropietario);
        if (archivoDestino == null) {
            return;
        }

        try (PrintWriter escritor = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDestino), "UTF-8"))) {
            escritor.println("=== REPORTE DE INVENTARIO - ITALIA PIZZA ===\n");
            escritor.printf("%-10s %-30s %10s %10s%n",
                    "Código", "Nombre", "Precio", "Existencia");
            escritor.println("-".repeat(65));
            for (Producto producto : listaProductos) {
                escritor.printf("%-10s %-30s %10.2f %10d%n",
                        producto.getCodigo(),
                        producto.getNombre(),
                        producto.getPrecio(),
                        producto.getCantidad());
            }
            mostrarAlertaInformacion("Reporte generado", "Inventario exportado correctamente.");
        } catch (IOException excepcion) {
            mostrarAlertaError("Error al generar reporte: " + excepcion.getMessage());
        }
    }

    private static void mostrarAlertaInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.showAndWait();
    }

    private static void mostrarAlertaError(String mensaje) {
        new Alert(Alert.AlertType.ERROR, mensaje).showAndWait();
    }
}
