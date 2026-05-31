package italiapizza.controlador;


import italiapizza.dao.impl.UsuarioDaoImpl;
import italiapizza.excepcion.DaoException;
import italiapizza.modelo.Usuario;
import italiapizza.util.Sesion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


    class LoginControladorTest {

        private LoginControlador loginControlador = new LoginControlador();

        @Test
        void iniciarSesion_conCamposVacios_retornaMensajeDeError() {
            String resultado = loginControlador.iniciarSesion("", "");
            assertNotNull(resultado);
        }

        @Test
        void iniciarSesion_conNombreUsuarioVacio_retornaMensajeDeError() {
            String resultado = loginControlador.iniciarSesion("", "password123");
            assertNotNull(resultado);
        }

        @Test
        void iniciarSesion_conContrasenaVacia_retornaMensajeDeError() {
            String resultado = loginControlador.iniciarSesion("admin", "");
            assertNotNull(resultado);
        }

        @Test
        void iniciarSesion_conCredencialesIncorrectas_retornaMensajeDeError() {
            String resultado = loginControlador.iniciarSesion("admin", "equivocada");
            assertNotNull(resultado);
        }

        @Test
        void iniciarSesion_conCredencialesIncorrectas_noGuardaUsuarioEnSesion() {
            loginControlador.iniciarSesion("admin", "equivocada");
            assertNull(Sesion.getInstancia().getUsuarioActual());
        }

    }
