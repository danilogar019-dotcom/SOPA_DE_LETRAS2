package modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PalabraDAO {

    // Método para INSERTAR una palabra (CREATE)
    public boolean insertar(String palabra) {
        Conexion conectar = new Conexion();
        Connection con = conectar.getConnection();
        PreparedStatement ps = null;

        // Pasamos la palabra a mayúsculas para que la sopa de letras sea uniforme
        String sql = "INSERT INTO palabras (palabra) VALUES (?)";

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, palabra.toUpperCase().trim());
            ps.execute();
            return true;
        } catch (SQLException e) {
            System.out.println("Error al insertar palabra: " + e.getMessage());
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }

    // Método para ELIMINAR una palabra (DELETE)
    public boolean eliminar(String palabra) {
        Conexion conectar = new Conexion();
        Connection con = conectar.getConnection();
        PreparedStatement ps = null;

        String sql = "DELETE FROM palabras WHERE palabra = ?";

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, palabra.toUpperCase().trim());
            int resultado = ps.executeUpdate();
            return resultado > 0; // Devuelve true si eliminó alguna fila
        } catch (SQLException e) {
            System.out.println("Error al eliminar palabra: " + e.getMessage());
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
    }

    // Método para LISTAR todas las palabras (READ)
    public ArrayList<String> listar() {
        ArrayList<String> lista = new ArrayList<>();
        Conexion conectar = new Conexion();
        Connection con = conectar.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "SELECT palabra FROM palabras ORDER BY palabra ASC";

        try {
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(rs.getString("palabra"));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar palabras: " + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println(e.toString());
            }
        }
        return lista;
    }
}
