package italiapizza.util;

import italiapizza.modelo.DetallePedido;
import italiapizza.modelo.Pedido;
import italiapizza.modelo.Producto;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
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

        try {
            PdfDocument pdfDocumento = new PdfDocument(new PdfWriter(archivoDestino));
            Document documento = new Document(pdfDocumento);

            documento.add(new Paragraph("Reporte de Pedidos - Italia Pizza\n"));

            Table tabla = new Table(new float[]{3, 3, 6, 2, 2});
            tabla.addHeaderCell(new Cell().add(new Paragraph("Fecha")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Cliente")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Productos")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Total")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Estatus")));

            for (Pedido pedido : listaPedidos) {
                tabla.addCell(new Cell().add(
                        new Paragraph(pedido.getFecha().format(FORMATO_FECHA_HORA))));
                tabla.addCell(new Cell().add(
                        new Paragraph(pedido.getCliente().toString())));

                StringBuilder productos = new StringBuilder();
                for (DetallePedido detalle : pedido.getDetalles()) {
                    productos.append(detalle.getProducto().getNombre())
                            .append(" x").append(detalle.getCantidad())
                            .append(" = $").append(String.format("%.2f", detalle.getSubtotal()))
                            .append("\n");
                }
                tabla.addCell(new Cell().add(new Paragraph(productos.toString().trim())));
                tabla.addCell(new Cell().add(
                        new Paragraph(String.format("$%.2f", pedido.getTotal()))));
                tabla.addCell(new Cell().add(
                        new Paragraph(pedido.getEstatus().name())));
            }

            documento.add(tabla);
            documento.close();

            mostrarAlertaInformacion("Exportación exitosa", "Archivo PDF guardado correctamente.");
        } catch (Exception excepcion) {
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

        try {
            PdfDocument pdfDocumento = new PdfDocument(new PdfWriter(archivoDestino));
            Document documento = new Document(pdfDocumento);

            documento.add(new Paragraph("Reporte de Inventario - Italia Pizza\n"));

            Table tabla = new Table(new float[]{2, 5, 2, 2});
            tabla.addHeaderCell(new Cell().add(new Paragraph("Código")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Nombre")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Precio")));
            tabla.addHeaderCell(new Cell().add(new Paragraph("Existencia")));

            for (Producto producto : listaProductos) {
                tabla.addCell(new Cell().add(new Paragraph(producto.getCodigo())));
                tabla.addCell(new Cell().add(new Paragraph(producto.getNombre())));
                tabla.addCell(new Cell().add(
                        new Paragraph(String.format("$%.2f", producto.getPrecio()))));
                tabla.addCell(new Cell().add(
                        new Paragraph(String.valueOf(producto.getCantidad()))));
            }

            documento.add(tabla);
            documento.close();

            mostrarAlertaInformacion("Reporte generado", "Inventario exportado correctamente.");
        } catch (Exception excepcion) {
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
