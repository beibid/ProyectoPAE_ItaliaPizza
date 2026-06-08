package italiapizza.modelo;

/**
 * Modelo que representa a un usuario (cliente o empleado) del sistema.
 */
public class Usuario {

    public enum Tipo { CLIENTE, EMPLEADO }
    public enum Rol  { ADMINISTRADOR, CAJERO }

    private int    idUsuario;
    private int    idCliente;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private String calle;
    private String numero;
    private String codigoPostal;
    private String ciudad;
    private Tipo   tipo;
    private String username;
    private String password;
    private Rol    rol;
    private boolean activo;

    public Usuario() {}

    public Usuario(int idUsuario, String nombre, String apellidos, String telefono,
                   String email, String calle, String numero, String codigoPostal,
                   String ciudad, Tipo tipo, String username, String password,
                   Rol rol, boolean activo) {
        this.idUsuario   = idUsuario;
        this.nombre      = nombre;
        this.apellidos   = apellidos;
        this.telefono    = telefono;
        this.email       = email;
        this.calle       = calle;
        this.numero      = numero;
        this.codigoPostal= codigoPostal;
        this.ciudad      = ciudad;
        this.tipo        = tipo;
        this.username    = username;
        this.password    = password;
        this.rol         = rol;
        this.activo      = activo;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────────
    public int     getIdUsuario()    { return idUsuario; }
    public void    setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int     getIdCliente()    { return idCliente; }
    public void    setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String  getNombre()       { return nombre; }
    public void    setNombre(String nombre) { this.nombre = nombre; }

    public String  getApellidos()    { return apellidos; }
    public void    setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String  getTelefono()     { return telefono; }
    public void    setTelefono(String telefono) { this.telefono = telefono; }

    public String  getEmail()        { return email; }
    public void    setEmail(String email) { this.email = email; }

    public String  getCalle()        { return calle; }
    public void    setCalle(String calle) { this.calle = calle; }

    public String  getNumero()       { return numero; }
    public void    setNumero(String numero) { this.numero = numero; }

    public String  getCodigoPostal() { return codigoPostal; }
    public void    setCodigoPostal(String codigoPostal) { this.codigoPostal = codigoPostal; }

    public String  getCiudad()       { return ciudad; }
    public void    setCiudad(String ciudad) { this.ciudad = ciudad; }

    public Tipo    getTipo()         { return tipo; }
    public void    setTipo(Tipo tipo) { this.tipo = tipo; }

    public String  getUsername()     { return username; }
    public void    setUsername(String username) { this.username = username; }

    public String  getPassword()     { return password; }
    public void    setPassword(String password) { this.password = password; }

    public Rol     getRol()          { return rol; }
    public void    setRol(Rol rol)   { this.rol = rol; }

    public boolean isActivo()        { return activo; }
    public void    setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nombre + " " + apellidos;
    }
}
