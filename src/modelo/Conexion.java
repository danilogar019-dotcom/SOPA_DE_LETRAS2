package modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // Nombre de la base de datos que creamos en phpMyAdmin
    private final String baseDatos = "db_sopa";
    private final String url = "jdbc:mysql://localhost:3306/" + baseDatos; // Revisa si tu puerto es 3306 o 3080
    private final String usuario = "root";
    private final String password = ""; // En XAMPP suele estar vacío por defecto
    private Connection con = null;

    public Connection getConnection() {
        try {
            // Cargamos el driver de MySQL/MariaDB
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establecemos la conexión
            con = DriverManager.getConnection(url, usuario, password);
        } catch (ClassNotFoundException e) {
            System.out.println("Error al cargar el driver: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de conexión a la BD: " + e.getMessage());
        }
        return con;
    }
}
