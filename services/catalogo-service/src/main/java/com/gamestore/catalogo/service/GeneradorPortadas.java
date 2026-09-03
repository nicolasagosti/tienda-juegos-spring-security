package com.gamestore.catalogo.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Genera portadas de juego "de mentira" (un gradiente + el titulo) para
 * los datos de ejemplo, y las guarda en el mismo directorio /uploads que
 * usa ImagenStorageService cuando un vendedor sube una imagen real.
 *
 * No es magia: es lo unico practico para que la demo arranque con
 * catalogo completo sin depender de descargar imagenes de internet (ni
 * de tener licencia para usarlas). Un vendedor real, en cambio, sube su
 * propio archivo desde el formulario "Publicar juego".
 */
public final class GeneradorPortadas {

    private static final int ANCHO = 480;
    private static final int ALTO = 270;

    private GeneradorPortadas() {
    }

    /** Genera la imagen, la guarda en uploadDir y devuelve la URL publica ("/uploads/xxx.png"). */
    public static String generar(Path uploadDir, String titulo, String etiqueta, Color colorDesde, Color colorHasta) {
        try {
            BufferedImage imagen = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = imagen.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Fondo en degrade diagonal
            g.setPaint(new GradientPaint(0, 0, colorDesde, ANCHO, ALTO, colorHasta));
            g.fillRect(0, 0, ANCHO, ALTO);

            // Circulos decorativos translucidos (efecto "portada de juego")
            g.setColor(new Color(255, 255, 255, 28));
            g.fill(new Ellipse2D.Double(ANCHO - 160, -60, 260, 260));
            g.setColor(new Color(0, 0, 0, 35));
            g.fill(new Ellipse2D.Double(-80, ALTO - 140, 220, 220));

            // Etiqueta de categoria (arriba a la izquierda)
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.setColor(new Color(255, 255, 255, 210));
            g.drawString(etiqueta.toUpperCase(), 22, 34);

            // Titulo del juego (centrado, con sombra sutil), partido en lineas si no entra
            g.setFont(new Font("SansSerif", Font.BOLD, 34));
            var lineas = partirEnLineas(g, titulo, ANCHO - 44);
            int alturaLinea = g.getFontMetrics().getHeight();
            int yInicial = (ALTO - alturaLinea * lineas.size()) / 2 + g.getFontMetrics().getAscent();
            for (int i = 0; i < lineas.size(); i++) {
                String linea = lineas.get(i);
                int anchoTexto = g.getFontMetrics().stringWidth(linea);
                int x = (ANCHO - anchoTexto) / 2;
                int y = yInicial + i * alturaLinea;
                g.setColor(new Color(0, 0, 0, 120));
                g.drawString(linea, x + 2, y + 2);
                g.setColor(Color.WHITE);
                g.drawString(linea, x, y);
            }

            g.dispose();

            Files.createDirectories(uploadDir);
            String nombreArchivo = UUID.randomUUID() + ".png";
            Path destino = uploadDir.resolve(nombreArchivo);
            ImageIO.write(imagen, "png", destino.toFile());

            return "/uploads/" + nombreArchivo;
        } catch (IOException e) {
            // Si por algun motivo no se puede generar/guardar, seguimos sin
            // imagen en vez de romper el arranque de toda la aplicacion.
            return null;
        }
    }

    private static java.util.List<String> partirEnLineas(Graphics2D g, String texto, int anchoMaximo) {
        java.util.List<String> lineas = new java.util.ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.isEmpty() ? palabra : actual + " " + palabra;
            if (g.getFontMetrics().stringWidth(prueba) > anchoMaximo && !actual.isEmpty()) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (!actual.isEmpty()) {
            lineas.add(actual.toString());
        }
        return lineas;
    }
}
