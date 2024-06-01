package paint;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class PAINT {

    public static class Figura {
        private int[] puntos;
        private boolean relleno;
        private String color;
        private int cantidadLados;

        public Figura(int[] puntos, boolean relleno, String color, int cantidadLados) {
            this.puntos = puntos;
            this.relleno = relleno;
            this.color = color;
            this.cantidadLados = cantidadLados;
        }

        public int[] getPuntos() {
            return puntos;
        }

        public boolean isRelleno() {
            return relleno;
        }

        public String getColor() {
            return color;
        }

        public int getCantidadLados() {
            return cantidadLados;
        }
    }

    private List<Figura> figurasDibujadas = new ArrayList<>();
    private boolean relleno = false;
    private Color color = Color.BLACK;

    public PAINT() {
    }

    public void setRelleno(boolean relleno) {
        this.relleno = relleno;
    }

    public boolean isRelleno() {
        return relleno;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getColorHex() {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void dibujarPunto(int x, int y, Graphics g, Color color) {
        g.setColor(color);
        g.fillOval(x, y, 5, 5);
        figurasDibujadas.add(new Figura(new int[]{x, y}, relleno, getColorHex(), 1));
    }

    public void dibujarRecta(int x, int y, int x2, int y2, Graphics g, Color color, boolean fill) {
        g.setColor(color);
        g.drawLine(x, y, x2, y2);
        figurasDibujadas.add(new Figura(new int[]{x, y, x2, y2}, fill, getColorHex(), 2));
    }

    public void dibujarCircunferencia(int x, int y, int x2, int y2, Graphics g, Color color, boolean fill) {
        g.setColor(color);
        int R = (int) (Math.sqrt(Math.pow((Double.valueOf(x2) - Double.valueOf(x)), 2) + Math.pow((Double.valueOf(y2) - Double.valueOf(y)), 2)));
        if (fill) {
            g.fillOval(x2 - R, y2 - R, 2 * R, 2 * R);
        } else {
            g.drawOval(x2 - R, y2 - R, 2 * R, 2 * R);
        }
        figurasDibujadas.add(new Figura(new int[]{x, y, x2, y2}, fill, getColorHex(), 0));
    }

 public void dibujarPoligonoR(int x, int y, int x2, int y2, int nPoints, Graphics g, Color color, boolean fill) {
    g.setColor(color);

    int[] puntosX = new int[nPoints];
    int[] puntosY = new int[nPoints];
    int[] puntos = new int[2 * nPoints];

    double radio = Math.sqrt(Math.pow((x2 - x), 2) + Math.pow((y2 - y), 2));
    double anguloInicial = -Math.PI / 2; // Iniciar en -90 grados para que el primer punto esté en la parte superior
    double anguloIncremento = 2 * Math.PI / nPoints;

    for (int i = 0; i < nPoints; i++) {
        double angulo = anguloInicial + i * anguloIncremento;
        puntosX[i] = (int) (x + radio * Math.cos(angulo));
        puntosY[i] = (int) (y + radio * Math.sin(angulo));
        puntos[2 * i] = puntosX[i];
        puntos[2 * i + 1] = puntosY[i];
    }

    if (fill) {
        g.fillPolygon(puntosX, puntosY, nPoints);
    } else {
        g.drawPolygon(puntosX, puntosY, nPoints);
    }
    figurasDibujadas.add(new Figura(puntos, fill, getColorHex(), nPoints));
}


    public void dibujarPoligonoI(int[] xPoints, int[] yPoints, int nPoints, Graphics g, Color color, boolean fill) {
        g.setColor(color);

        if (fill) {
            g.fillPolygon(xPoints, yPoints, nPoints);
        } else {
            for (int i = 0; i < nPoints; i++) {
                int siguienteIndice = (i + 1) % nPoints;
                g.drawLine(xPoints[i], yPoints[i], xPoints[siguienteIndice], yPoints[siguienteIndice]);
            }
        }
        int[] puntos = new int[2 * nPoints];
        for (int i = 0; i < nPoints; i++) {
            puntos[2 * i] = xPoints[i];
            puntos[2 * i + 1] = yPoints[i];
        }
        figurasDibujadas.add(new Figura(puntos, fill, getColorHex(), nPoints));
    }

    public List<Figura> obtenerFigurasDibujadas() {
        return figurasDibujadas;
    }
}
