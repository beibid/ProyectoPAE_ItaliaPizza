package italiapizza;

import italiapizza.vista.LoginVista;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicación Italia Pizza.
 */
public class Main extends Application {

    @Override
    public void start(Stage escenarioPrincipal) throws IOException {
        LoginVista.mostrarEn(escenarioPrincipal);
    }

    public static void main(String[] argumentos) {
        launch(argumentos);
    }
}
