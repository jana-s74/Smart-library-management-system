package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleChartPanel extends JPanel {

    private String chartTitle;
    private final Map<String, Integer> dataMap;
    private final Color[] barColors = {
            ThemeManager.PRIMARY_ORANGE,
            new Color(0x3B, 0x82, 0xF6), // Blue
            ThemeManager.SUCCESS_GREEN,
            ThemeManager.REPORT_PURPLE,
            ThemeManager.WARNING_AMBER
    };

    public SimpleChartPanel(String chartTitle) {
        this.chartTitle = chartTitle;
        this.dataMap = new LinkedHashMap<>();
        setOpaque(false);
    }

    public void addData(String category, int value) {
        dataMap.put(category, value);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw Card Background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, width - 1, height - 1, 16, 16);
        g2.setColor(ThemeManager.BORDER_GRAY);
        g2.drawRoundRect(0, 0, width - 1, height - 1, 16, 16);

        // Header Title
        g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2.setColor(ThemeManager.TEXT_DARK);
        g2.drawString(chartTitle, 20, 30);

        if (dataMap.isEmpty()) {
            g2.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.drawString("No analytical data to display.", 20, 70);
            g2.dispose();
            return;
        }

        int maxVal = 1;
        for (int v : dataMap.values()) {
            if (v > maxVal) maxVal = v;
        }

        int startX = 50;
        int startY = height - 40;
        int chartHeight = height - 90;
        int numBars = dataMap.size();
        int barWidth = Math.max(24, (width - 100) / (numBars * 2));
        int gap = barWidth;

        // Draw Baseline
        g2.setColor(ThemeManager.BORDER_GRAY);
        g2.drawLine(startX - 10, startY, width - 30, startY);

        int idx = 0;
        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            int val = entry.getValue();
            int bHeight = (int) (((double) val / maxVal) * chartHeight);
            int x = startX + idx * (barWidth + gap);
            int y = startY - bHeight;

            // Draw Bar
            Color barColor = barColors[idx % barColors.length];
            g2.setColor(barColor);
            g2.fillRoundRect(x, y, barWidth, bHeight, 8, 8);

            // Draw Value
            g2.setColor(ThemeManager.TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(String.valueOf(val), x + (barWidth / 4), y - 5);

            // Draw Label
            g2.setColor(ThemeManager.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            String label = entry.getKey();
            if (label.length() > 10) label = label.substring(0, 8) + "..";
            g2.drawString(label, x - 5, startY + 20);

            idx++;
        }

        g2.dispose();
    }
}
