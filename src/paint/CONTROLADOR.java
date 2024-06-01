package paint;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

public class CONTROLADOR {

    private MODELO modelo = new MODELO();
    private PAINT paint;
    private VISTA vista;  // Añadir una referencia a la vista

    public CONTROLADOR(PAINT paint, VISTA vista) {
        this.paint = paint;
        this.vista = vista;  // Inicializar la referencia a la vista
    }

    public void dibujarPunto(int x, int y, Graphics g, Color color) {
        paint.dibujarPunto(x, y, g, color);
    }

    public void dibujarRecta(int x, int y, int x2, int y2, Graphics g, Color color) {
        paint.dibujarRecta(x, y, x2, y2, g, color, paint.isRelleno());
    }

    public void dibujarCircunferencia(int x, int y, int x2, int y2, Graphics g, Color color) {
        paint.dibujarCircunferencia(x, y, x2, y2, g, color, paint.isRelleno());
    }

    public void dibujarPoligonoR(int x, int y, int x2, int y2, int nPoints, Graphics g, Color color) {
        paint.dibujarPoligonoR(x, y, x2, y2, nPoints, g, color, paint.isRelleno());
    }

    public void dibujarPoligonoI(int[] xPoints, int[] yPoints, int nPoints, Graphics g, Color color) {
        paint.dibujarPoligonoI(xPoints, yPoints, nPoints, g, color, paint.isRelleno());
    }

    public void guardarFicheroYPoligonos(String nombre) throws SQLException, IOException {
        int ficheroId = modelo.guardarFichero(nombre);
        List<PAINT.Figura> figuras = paint.obtenerFigurasDibujadas();
        for (PAINT.Figura figura : figuras) {
            int cantidadLados = figura.getCantidadLados();
            int poligonoId = modelo.guardarPoligono(cantidadLados, ficheroId, figura.isRelleno(), figura.getColor());
            for (int i = 0; i < figura.getPuntos().length; i += 2) {
                modelo.guardarPunto(poligonoId, figura.getPuntos()[i], figura.getPuntos()[i + 1]);
            }
        }
        generarSVG(nombre, figuras);
    }

    public void poblarComboBox(JComboBox<String> comboBox) throws SQLException {
        List<String> nombresFicheros = modelo.obtenerNombresFicheros();
        comboBox.removeAllItems(); // Limpiar los elementos existentes
        for (String nombre : nombresFicheros) {
            comboBox.addItem(nombre);
        }
    }

    public void cargarDibujo(String nombreFichero, Graphics g) throws SQLException {
        int ficheroId = modelo.obtenerFicheroIdPorNombre(nombreFichero);
        List<PAINT.Figura> figuras = modelo.obtenerFigurasPorFicheroId(ficheroId);
        for (PAINT.Figura figura : figuras) {
            int[] puntos = figura.getPuntos();
            Color color = Color.decode(figura.getColor());
            boolean relleno = figura.isRelleno();
            int cantidadLados = figura.getCantidadLados();

            if (cantidadLados == 0) {
                paint.dibujarCircunferencia(puntos[0], puntos[1], puntos[2], puntos[3], g, color, relleno);
            } else if (cantidadLados == 1) {
                paint.dibujarPunto(puntos[0], puntos[1], g, color);
            } else if (cantidadLados == 2) {
                paint.dibujarRecta(puntos[0], puntos[1], puntos[2], puntos[3], g, color, relleno);
            } else {
                int[] xPoints = new int[cantidadLados];
                int[] yPoints = new int[cantidadLados];
                for (int i = 0; i < cantidadLados; i++) {
                    xPoints[i] = puntos[2 * i];
                    yPoints[i] = puntos[2 * i + 1];
                }
                paint.dibujarPoligonoI(xPoints, yPoints, cantidadLados, g, color, relleno);
            }
        }
    }

    private void generarSVG(String nombre, List<PAINT.Figura> figuras) throws IOException {
        // Obtener el tamaño del área de dibujo
        int svgWidth = vista.getAreaDibujoWidth();
        int svgHeight = vista.getAreaDibujoHeight();

        StringBuilder svgContent = new StringBuilder();
        svgContent.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"%d\" height=\"%d\">\n", svgWidth, svgHeight));

        for (PAINT.Figura figura : figuras) {
            String color = figura.getColor();
            boolean relleno = figura.isRelleno();
            int[] puntos = figura.getPuntos();
            int cantidadLados = figura.getCantidadLados();

            if (cantidadLados == 1) { // Punto
                svgContent.append(String.format("<circle cx=\"%d\" cy=\"%d\" r=\"2\" fill=\"%s\" />\n", puntos[0], puntos[1], color));
            } else if (cantidadLados == 2) { // Recta
                svgContent.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" />\n", puntos[0], puntos[1], puntos[2], puntos[3], color));
            } else if (cantidadLados == 0) { // Circunferencia
                int R = (int) Math.sqrt(Math.pow(puntos[2] - puntos[0], 2) + Math.pow(puntos[3] - puntos[1], 2));
                int x = puntos[2] - R;
                int y = puntos[3] - R;
                if (relleno) {
                    svgContent.append(String.format("<circle cx=\"%d\" cy=\"%d\" r=\"%d\" fill=\"%s\" />\n", x + R, y + R, R, color));
                } else {
                    svgContent.append(String.format("<circle cx=\"%d\" cy=\"%d\" r=\"%d\" stroke=\"%s\" fill=\"none\" />\n", x + R, y + R, R, color));
                }
            } else { // Polígono
                svgContent.append("<polygon points=\"");
                for (int i = 0; i < puntos.length; i += 2) {
                    svgContent.append(puntos[i]).append(",").append(puntos[i + 1]).append(" ");
                }
                if (relleno) {
                    svgContent.append(String.format("\" fill=\"%s\" />\n", color));
                } else {
                    svgContent.append(String.format("\" stroke=\"%s\" fill=\"none\" />\n", color));
                }
            }
        }

        svgContent.append("</svg>");

        // Mostrar cuadro de diálogo para seleccionar la ubicación de guardado
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(nombre + ".svg"));
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(svgContent.toString());
            }
        }
    }

    public void eliminarFichero(String nombre) throws SQLException {
        int ficheroId = modelo.obtenerFicheroIdPorNombre(nombre);
        modelo.eliminarFichero(ficheroId);
    }
}
