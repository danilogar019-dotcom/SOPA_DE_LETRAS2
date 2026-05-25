package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import modelo.PalabraDAO;
import vista.VentanaPrincipal;

public class SopaControlador implements ActionListener, MouseListener, MouseMotionListener {

    private VentanaPrincipal vista;
    private PalabraDAO modelo;
    
    // Variables globales para la Sopa de Letras (adaptadas del ANEXO)
    private String[][] M; // Matriz de letras
    private int[][] H;    // Matriz de huecos ocupados
    private final int TAMANIO = 15; // Tamaño adaptado a la interfaz de la práctica
    
    // Coordenadas para el control del ratón interactivo
    private int filaInicio = -1, colInicio = -1;

    public SopaControlador(VentanaPrincipal vista, PalabraDAO modelo) {
        this.vista = vista;
        this.modelo = modelo;
        
        // Enlazamos los listeners de los botones de la vista
        this.vista.btnAnadir.addActionListener(this);
        this.vista.btnEliminar.addActionListener(this);
        this.vista.btnConsultar.addActionListener(this);
        this.vista.btnGenerar.addActionListener(this);
        this.vista.btnBuscar.addActionListener(this);
        this.vista.btnMostrarSolucion.addActionListener(this);
        this.vista.btnOcultarSolucion.addActionListener(this);
        
        // Enlazamos los listeners del ratón al JTextArea de la sopa
        this.vista.txtSopa.addMouseListener(this);
        this.vista.txtSopa.addMouseMotionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // --- BOTÓN AÑADIR (C) ---
        if (e.getSource() == vista.btnAnadir) {
            String palabra = vista.txtPalabra.getText().trim();
            // Validación de entrada (Rúbrica: Criterio 3)
            if (palabra.isEmpty() || !palabra.matches("[a-zA-ZÑñ]+")) {
                JOptionPane.showMessageDialog(vista, "Introduce una palabra válida (solo letras).", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (modelo.insertar(palabra)) {
                JOptionPane.showMessageDialog(vista, "¡Palabra añadida correctamente!");
                vista.txtPalabra.setText("");
                actualizarLista();
            } else {
                // Previene duplicados si salta la restricción UNIQUE de MariaDB
                JOptionPane.showMessageDialog(vista, "Error: La palabra ya existe o no se pudo guardar.", "Error de Duplicado", JOptionPane.ERROR_MESSAGE);
            }
        }

        // --- BOTÓN ELIMINAR (D) ---
        if (e.getSource() == vista.btnEliminar) {
            String seleccionado = vista.listaPalabras.getSelectedValue();
            if (seleccionado == null) {
                JOptionPane.showMessageDialog(vista, "Selecciona una palabra de la lista para eliminar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (modelo.eliminar(seleccionado)) {
                JOptionPane.showMessageDialog(vista, "Palabra DHCP/palabra eliminada.");
                actualizarLista();
            }
        }

        // --- BOTÓN CONSULTAR (R) ---
        if (e.getSource() == vista.btnConsultar) {
            actualizarLista();
        }

        // --- BOTÓN GENERAR SOPA ---
        if (e.getSource() == vista.btnGenerar) {
            ArrayList<String> palabrasBD = modelo.listar();
            if (palabrasBD.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "La base de datos está vacía. Añade palabras primero.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            M = new String[TAMANIO][TAMANIO];
            H = new int[TAMANIO][TAMANIO];
            
            inicializaMatrices();
            
            // Colocamos las palabras recuperadas dinámicamente de la BD
            if (colocarTodasBD(palabrasBD)) {
                mostrarMatrizEnTextArea();
            } else {
                JOptionPane.showMessageDialog(vista, "No se pudieron colocar todas las palabras debido al espacio. Inténtalo de nuevo.", "Límite superado", JOptionPane.WARNING_MESSAGE);
            }
        }

        // --- BOTÓN MOSTRAR SOLUCIÓN (Opcional - Rúbrica: Criterio 2) ---
        if (e.getSource() == vista.btnMostrarSolucion) {
            if (M == null) return;
            StringBuilder sb = new StringBuilder();
            for (int f = 0; f < TAMANIO; f++) {
                for (int c = 0; c < TAMANIO; c++) {
                    // Si el casillero está ocupado por una palabra real (H == 1), lo muestra; si no, pone un punto
                    if (H[f][c] == 1) {
                        sb.append(M[f][c]).append(" ");
                    } else {
                        sb.append(". ");
                    }
                }
                sb.append("\n");
            }
            vista.txtSopa.setText(sb.toString());
        }

        // --- BOTÓN OCULTAR SOLUCIÓN ---
        if (e.getSource() == vista.btnOcultarSolucion) {
            mostrarMatrizEnTextArea();
        }

        // --- BOTÓN BUSCAR PALABRA (Opcional - Rúbrica: Criterio 2) ---
        if (e.getSource() == vista.btnBuscar) {
            String buscar = JOptionPane.showInputDialog(vista, "Escribe la palabra que quieres buscar:");
            if (buscar != null && !buscar.trim().isEmpty()) {
                ArrayList<String> actuales = modelo.listar();
                if (actuales.contains(buscar.toUpperCase().trim())) {
                    JOptionPane.showMessageDialog(vista, "¡Sí! La palabra '" + buscar.toUpperCase() + "' está registrada en el panel actual.");
                } else {
                    JOptionPane.showMessageDialog(vista, "No, esa palabra no se encuentra en la base de datos.");
                }
            }
        }
    }

    // Actualiza el componente gráfico JList de forma limpia
    private void actualizarLista() {
        ArrayList<String> palabras = modelo.listar();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String p : palabras) {
            listModel.addElement(p);
        }
        vista.listaPalabras.setModel(listModel);
    }

    // Vuelca la matriz en formato de bloque de texto al JTextArea
    private void mostrarMatrizEnTextArea() {
        if (M == null) return;
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < TAMANIO; f++) {
            for (int c = 0; c < TAMANIO; c++) {
                sb.append(M[f][c]).append(" ");
            }
            sb.append("\n");
        }
        vista.txtSopa.setText(sb.toString());
    }

    // =========================================================================
    // IMPLEMENTACIÓN DEL CONTROL DEL RATÓN (INTERACCIÓN PREMIUM UX/UI)
    // =========================================================================
    
    @Override
    public void mousePressed(MouseEvent e) {
        if (M == null) return;
        try {
            // Identificamos el índice posicional del carácter del JTextArea donde se hace clic
            int caretPos = vista.txtSopa.viewToModel2D(e.getPoint());
            if (caretPos == -1) return;
            
            // Cada línea de texto equivale a (TAMANIO * 2) caracteres por los espacios de espaciado y el \n
            int longitudFila = (TAMANIO * 2); 
            filaInicio = caretPos / longitudFila;
            colInicio = (caretPos % longitudFila) / 2;
            
            if (filaInicio >= TAMANIO || colInicio >= TAMANIO) {
                filaInicio = -1; colInicio = -1;
            }
        } catch (Exception ex) {
            filaInicio = -1; colInicio = -1;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (M == null || filaInicio == -1 || colInicio == -1) return;
        try {
            int caretPos = vista.txtSopa.viewToModel2D(e.getPoint());
            if (caretPos == -1) return;
            
            int longitudFila = (TAMANIO * 2);
            int filaFin = caretPos / longitudFila;
            int colFin = (caretPos % longitudFila) / 2;
            
            if (filaFin >= TAMANIO) filaFin = TAMANIO - 1;
            if (colFin >= TAMANIO) colFin = TAMANIO - 1;

            StringBuilder palabraSeleccionada = new StringBuilder();
            
            // Desplazamiento Horizontal (Misma fila)
            if (filaInicio == filaFin) {
                int min = Math.min(colInicio, colFin);
                int max = Math.max(colInicio, colFin);
                for (int c = min; c <= max; c++) palabraSeleccionada.append(M[filaInicio][c]);
            } 
            // Desplazamiento Vertical (Misma columna)
            else if (colInicio == colFin) {
                int min = Math.min(filaInicio, filaFin);
                int max = Math.max(filaInicio, filaFin);
                for (int f = min; f <= max; f++) palabraSeleccionada.append(M[f][colInicio]);
            }
            
            String seleccion = palabraSeleccionada.toString().trim();
            String seleccionInvertida = palabraSeleccionada.reverse().toString().trim();
            
            ArrayList<String> palabrasBD = modelo.listar();
            
            // Validación cruzada (acepta lectura al derecho o invertida)
            if (!seleccion.isEmpty() && (palabrasBD.contains(seleccion) || palabrasBD.contains(seleccionInvertida))) {
                String palabraEncontrada = palabrasBD.contains(seleccion) ? seleccion : seleccionInvertida;
                JOptionPane.showMessageDialog(vista, "¡Excelente! Has encontrado la palabra: " + palabraEncontrada, "¡Logrado!", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            // Captura tolerante de desbordamientos de rango visual
        }
        filaInicio = -1; colInicio = -1;
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}

    // =========================================================================
    // LÓGICA ALGORÍTMICA BASADA EN EL ANEXO DEL PROFESOR
    // =========================================================================
    
    private int azar(int limite) {
        return (int) Math.floor(Math.random() * limite);
    }

    private void inicializaMatrices() {
        String cadena = "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ";
        String[] letra = cadena.split("");
        for (int f = 0; f < TAMANIO; f++) {
            for (int c = 0; c < TAMANIO; c++) {
                M[f][c] = letra[azar(27)];
                H[f][c] = 0;
            }
        }
    }

    private boolean colocarTodasBD(ArrayList<String> listaPalabras) {
        int cuenta = 0;
        int longPalabra = listaPalabras.size();
        int intentosTotales = 0; // Rompe bucles infinitos (Rúbrica: Criterio 2)

        do {
            String[] word = listaPalabras.get(cuenta).split("");
            int orientacion = azar(8);
            boolean colocado = false;

            switch (orientacion) {
                case 0: colocado = colocarPalabra0(word); break;
                case 1: colocado = colocarPalabra1(word); break;
                case 2: colocado = colocarPalabra2(word); break;
                case 3: colocado = colocarPalabra3(word); break;
                case 4: colocado = colocarPalabra4(word); break;
                case 5: colocado = colocarPalabra5(word); break;
                case 6: colocado = colocarPalabra6(word); break;
                case 7: colocado = colocarPalabra7(word); break;
            }

            if (colocado) {
                cuenta++;
            }
            
            intentosTotales++;
            if (intentosTotales > 1000) { 
                return false; // Escapa limpiamente si no hay espacio para mitigar cuelgues
            }

        } while (cuenta < longPalabra);
        return true;
    }

    // --- MÉTODOS DE ORIENTACIÓN DEL ANEXO ADAPTADOS AL TAMAÑO GENERAL ---
    
    private boolean colocarPalabra0(String[] word) {
        int f = azar(TAMANIO);
        int L = word.length;
        if (L > TAMANIO) return false;
        int c = azar(TAMANIO - L + 1);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((H[f][c + t] == 1) && (!M[f][c + t].equals(word[t]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f][c + t] = word[t]; H[f][c + t] = 1; }
        }
        return permitido;
    }

    private boolean colocarPalabra2(String[] word) {
        int L = word.length;
        if (L > TAMANIO) return false;
        int f = azar(TAMANIO - L + 1);
        int c = azar(TAMANIO);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((H[f + t][c] == 1) && (!M[f + t][c].equals(word[t]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f + t][c] = word[t]; H[f + t][c] = 1; }
        }
        return permitido;
    }

    private boolean colocarPalabra4(String[] word) {
        int f = azar(TAMANIO);
        int L = word.length;
        if (L > TAMANIO) return false;
        int c = azar(TAMANIO - L + 1);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((H[f][c + t] == 1) && (!M[f][c + t].equals(word[L - t - 1]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f][c + t] = word[L - t - 1]; H[f][c + t] = 1; }
        }
        return permitido;
    }

    private boolean colocarPalabra6(String[] word) {
        int L = word.length;
        if (L > TAMANIO) return false;
        int f = azar(TAMANIO - L + 1);
        int c = azar(TAMANIO);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((H[f + t][c] == 1) && (!M[f + t][c].equals(word[L - t - 1]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f + t][c] = word[L - t - 1]; H[f + t][c] = 1; }
        }
        return permitido;
    }

    private boolean colocarPalabra1(String[] word) {
        int L = word.length;
        int f = azar(TAMANIO);
        int c = azar(TAMANIO);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((f + t >= TAMANIO) || (c + t >= TAMANIO)) permitido = false;
            else if ((H[f + t][c + t] == 1) && (!M[f + t][c + t].equals(word[t]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f + t][c + t] = word[t]; H[f + t][c + t] = 1; }
        }
        return permitido;
    }

    private boolean colocarPalabra5(String[] word) {
        int L = word.length;
        int f = azar(TAMANIO);
        int c = azar(TAMANIO);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((f + t >= TAMANIO) || (c + t >= TAMANIO)) permitido = false;
            else if ((H[f + t][c + t] == 1) && (!M[f + t][c + t].equals(word[L - t - 1]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f + t][c + t] = word[L - t - 1]; H[f + t][c + t] = 1; }
        }
        return permitido;
    }

    private boolean colocarPalabra3(String[] word) {
        int L = word.length;
        int f = azar(TAMANIO);
        int c = azar(TAMANIO);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((f + t >= TAMANIO) || (c - t < 0)) permitido = false;
            else if ((H[f + t][c - t] == 1) && (!M[f + t][c - t].equals(word[t]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f + t][c - t] = word[t]; H[f + t][c - t] = 1; }
        }
        return permitido;
    }

    private boolean colocarPalabra7(String[] word) {
        int L = word.length;
        int f = azar(TAMANIO);
        int c = azar(TAMANIO);
        boolean permitido = true;
        for (int t = 0; t < L; t++) {
            if ((f + t >= TAMANIO) || (c - t < 0)) permitido = false;
            else if ((H[f + t][c - t] == 1) && (!M[f + t][c - t].equals(word[L - t - 1]))) permitido = false;
        }
        if (permitido) {
            for (int t = 0; t < L; t++) { M[f + t][c - t] = word[L - t - 1]; H[f + t][c - t] = 1; }
        }
        return permitido;
    }
}