package italiapizza.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa un pedido realizado por un cliente.
 */
public class Pedido {

    public enum Estatus { EN_PROCESO, ENTREGADO, CANCELADO }

    private int           idPedido;
    private LocalDateTime fecha;
    private double        total;
    private Estatus       estatus;
    private Usuario       cliente;
    private List<DetallePedido> detalles;

    public Pedido() {
        this.detalles = new ArrayList<>();
        this.estatus  = Estatus.EN_PROCESO;
        this.fecha    = LocalDateTime.now();
    }

    public int           getIdPedido()  { return idPedido; }
    public void          setIdPedido(int idPedido) { this.idPedido = idPedido; }

    public LocalDateTime getFecha()     { return fecha; }
    public void          setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public double        getTotal()     { return total; }
    public void          setTotal(double total) { this.total = total; }

    public Estatus       getEstatus()   { return estatus; }
    public void          setEstatus(Estatus estatus) { this.estatus = estatus; }

    public Usuario       getCliente()   { return cliente; }
    public void          setCliente(Usuario cliente) { this.cliente = cliente; }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }

    /** Recalcula el total sumando precio*cantidad de cada detalle. */
    public void recalcularTotal() {
        this.total = detalles.stream()
                .mapToDouble(d -> d.getPrecioUnit() * d.getCantidad())
                .sum();
    }
}
