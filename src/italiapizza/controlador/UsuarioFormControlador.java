package italiapizza.controlador;

import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UsuarioFormControlador {

    @FXML private TextField        campoNombre;
    @FXML private TextField        campoApellidos;
    @FXML private TextField        campoTelefono;
    @FXML private TextField        campoEmail;
    @FXML private TextField        campoCalle;
    @FXML private TextField        campoNumero;
    @FXML private TextField        campoCodigoPostal;
    @FXML private TextField        campoCiudad;
    @FXML private ComboBox<String> comboTipoUsuario;
    @FXML private VBox             panelDatosEmpleado;
    @FXML private TextField        campoNombreUsuarioSistema;
    @FXML private PasswordField    campoContrasena;
    @FXML private ComboBox<String> comboRolEmpleado;
    @FXML private Label            etiquetaError;

    private final UsuarioDaoImpl usuarioDao = new UsuarioDaoImpl();
    private Usuario  usuarioEnEdicion;
    private Runnable accionAlGuardar;

    @FXML
    public void initialize() {
        comboTipoUsuario.getItems().addAll("CLIENTE", "EMPLEADO");
        comboTipoUsuario.setValue("CLIENTE");
        comboRolEmpleado.getItems().addAll("ADMINISTRADOR", "CAJERO");
    }

    public void inicializar(Usuario usuarioEditar, Runnable accionAlGuardar) {
        this.usuarioEnEdicion = usuarioEditar;
        this.accionAlGuardar  = accionAlGuardar;
        if (usuarioEditar != null) {
            rellenarCamposConUsuario(usuarioEditar);
        }
    }

    private void rellenarCamposConUsuario(Usuario usuario) {
        campoNombre.setText(usuario.getNombre());
        campoApellidos.setText(usuario.getApellidos());
        campoTelefono.setText(usuario.getTelefono());
        campoEmail.setText(usuario.getEmail());
        campoCalle.setText(usuario.getCalle());
        campoNumero.setText(usuario.getNumero());
        campoCodigoPostal.setText(usuario.getCodigoPostal());
        campoCiudad.setText(usuario.getCiudad());
        comboTipoUsuario.setValue(usuario.getTipo().name());
        if (usuario.getTipo() == Usuario.Tipo.EMPLEADO) {
            campoNombreUsuarioSistema.setText(usuario.getUsername());
            if (usuario.getRol() != null) {
                comboRolEmpleado.setValue(usuario.getRol().name());
            }
            panelDatosEmpleado.setVisible(true);
            panelDatosEmpleado.setManaged(true);
        }
    }

    @FXML
    public void actualizarVisibilidadPanelEmpleado() {
        boolean esEmpleado = "EMPLEADO".equals(comboTipoUsuario.getValue());
        panelDatosEmpleado.setVisible(esEmpleado);
        panelDatosEmpleado.setManaged(esEmpleado);
        ((Stage) campoNombre.getScene().getWindow()).sizeToScene();
    }

    @FXML
    public void ejecutarGuardarUsuario() {
        Usuario usuario = usuarioEnEdicion != null ? usuarioEnEdicion : new Usuario();
        usuario.setNombre(campoNombre.getText().trim());
        usuario.setApellidos(campoApellidos.getText().trim());
        usuario.setTelefono(campoTelefono.getText().trim());
        usuario.setEmail(campoEmail.getText().trim());
        usuario.setCalle(campoCalle.getText().trim());
        usuario.setNumero(campoNumero.getText().trim());
        usuario.setCodigoPostal(campoCodigoPostal.getText().trim());
        usuario.setCiudad(campoCiudad.getText().trim());
        usuario.setTipo(Usuario.Tipo.valueOf(comboTipoUsuario.getValue()));
        if (usuario.getTipo() == Usuario.Tipo.EMPLEADO) {
            usuario.setUsername(campoNombreUsuarioSistema.getText().trim());
            usuario.setPassword(campoContrasena.getText());
            if (comboRolEmpleado.getValue() != null) {
                usuario.setRol(Usuario.Rol.valueOf(comboRolEmpleado.getValue()));
            }
        }
        String mensajeError = usuarioEnEdicion == null
                ? registrar(usuario)
                : actualizar(usuario);
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

    private String registrar(Usuario usuario) {
        String mensajeValidacion = validarUsuario(usuario);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            usuarioDao.registrar(usuario);
            return null;
        } catch (DaoException excepcion) {
            return "Error al guardar: " + excepcion.getMessage();
        }
    }

    private String actualizar(Usuario usuario) {
        String mensajeValidacion = validarUsuario(usuario);
        if (mensajeValidacion != null) {
            return mensajeValidacion;
        }
        try {
            usuarioDao.actualizar(usuario);
            return null;
        } catch (DaoException excepcion) {
            return "Error al actualizar: " + excepcion.getMessage();
        }
    }

    private String validarUsuario(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
            return "El nombre es requerido.";
        }
        if (usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            return "Los apellidos son requeridos.";
        }
        if (usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
            return "El teléfono es requerido.";
        }
        if (usuario.getTipo() == null) {
            return "Selecciona el tipo de usuario.";
        }
        if (usuario.getTipo() == Usuario.Tipo.EMPLEADO) {
            if (usuario.getUsername() == null || usuario.getUsername().isBlank()) {
                return "El nombre de usuario es requerido para empleados.";
            }
            if (usuario.getIdUsuario() == 0
                    && (usuario.getPassword() == null || usuario.getPassword().isBlank())) {
                return "La contraseña es requerida para empleados.";
            }
            if (usuario.getRol() == null) {
                return "Selecciona el rol del empleado.";
            }
        }
        return null;
    }

    private void cerrarVentana() {
        ((Stage) campoNombre.getScene().getWindow()).close();
    }
}
