package italiapizza.modelo;

/**
 * Representa una línea de detalle dentro de un pedido.
 */
public class DetallePedido {

    private int     idDetalle;
    private Pedido  pedido;
    private Producto producto;
    private int     cantidad;
    private double  precioUnit;

    public DetallePedido() {}

    public DetallePedido(Producto producto, int cantidad) {
        this.producto  = producto;
        this.cantidad  = cantidad;
        this.precioUnit= producto.getPrecio();
    }

    public int      getIdDetalle()  { return idDetalle; }
    public void     setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    public Pedido   getPedido()     { return pedido; }
    public void     setPedido(Pedido pedido) { this.pedido = pedido; }

    public Producto getProducto()   { return producto; }
    public void     setProducto(Producto producto) { this.producto = producto; }

    public int      getCantidad()   { return cantidad; }
    public void     setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double   getPrecioUnit() { return precioUnit; }
    public void     setPrecioUnit(double precioUnit) { this.precioUnit = precioUnit; }

    public double   getSubtotal()   { return cantidad * precioUnit; }
}
