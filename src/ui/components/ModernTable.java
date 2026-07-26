package ui.components;

import utils.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ModernTable extends JTable {

    public ModernTable(DefaultTableModel model) {
        super(model);
        setRowHeight(40);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
        setSelectionBackground(new Color(0xFF, 0xED, 0xD5)); // Soft Orange selection
        setSelectionForeground(ThemeManager.TEXT_DARK);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));

        // Header Styling
        JTableHeader header = getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(0xF3, 0xF4, 0xF6));
        header.setForeground(ThemeManager.TEXT_DARK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));

        // Center / Padding Cell Renderer
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFA, 0xFA));
                }

                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        };

        setDefaultRenderer(Object.class, customRenderer);
    }
}
