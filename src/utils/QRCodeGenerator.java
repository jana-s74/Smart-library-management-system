package utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * QR Code Generator Utility using Java2D matrix encoding simulation.
 */
public class QRCodeGenerator {

    public static BufferedImage generateQRCodeImage(String data, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // Background white
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        int seed = data.hashCode();

        int size = 21; // Standard QR matrix grid 21x21
        int cellW = width / (size + 4);
        int cellH = height / (size + 4);

        int startX = (width - (size * cellW)) / 2;
        int startY = (height - (size * cellH)) / 2;

        // Draw position corners (standard QR corner squares)
        drawCornerSquare(g, startX, startY, cellW, cellH);
        drawCornerSquare(g, startX + (size - 7) * cellW, startY, cellW, cellH);
        drawCornerSquare(g, startX, startY + (size - 7) * cellH, cellW, cellH);

        // Pseudorandom internal pattern based on data hash
        java.util.Random rand = new java.util.Random(seed);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                // Avoid corner finder patterns
                if ((r < 7 && c < 7) || (r < 7 && c >= size - 7) || (r >= size - 7 && c < 7)) {
                    continue;
                }
                if (rand.nextBoolean()) {
                    g.fillRect(startX + (c * cellW), startY + (r * cellH), cellW, cellH);
                }
            }
        }

        g.dispose();
        return image;
    }

    private static void drawCornerSquare(Graphics2D g, int x, int y, int cellW, int cellH) {
        g.fillRect(x, y, 7 * cellW, 7 * cellH);
        g.setColor(Color.WHITE);
        g.fillRect(x + cellW, y + cellH, 5 * cellW, 5 * cellH);
        g.setColor(Color.BLACK);
        g.fillRect(x + 2 * cellW, y + 2 * cellH, 3 * cellW, 3 * cellH);
    }

    public static String generateAndSave(String data, String fileName) {
        try {
            File dir = new File("assets/qr");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, fileName + ".png");
            BufferedImage qrImage = generateQRCodeImage(data, 250, 250);
            ImageIO.write(qrImage, "png", file);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
