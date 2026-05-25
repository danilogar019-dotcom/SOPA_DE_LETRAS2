
package javaapplication26;

import controlador.SopaControlador;
import modelo.PalabraDAO;
import vista.VentanaPrincipal;

public class JavaApplication26 {

    public static void main(String[] args) {
        // Instanciamos las 3 capas del MVC
        VentanaPrincipal vista = new VentanaPrincipal();
        PalabraDAO modelo = new PalabraDAO();
        SopaControlador controlador = new SopaControlador(vista, modelo);

        // Hacemos visible la ventana en la pantalla
        vista.setLocationRelativeTo(null); // Centra la ventana
        vista.setVisible(true);
    }
}
