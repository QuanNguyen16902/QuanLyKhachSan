package Common;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Căn giữa các dòng
        setHorizontalAlignment(SwingConstants.CENTER);

        // Màu nền xen kẽ giữa các dòng
        if (row % 2 == 0) {
            cellComponent.setBackground(Color.WHITE);
        } else {
            cellComponent.setBackground(new Color(240, 240, 240)); // Màu xám nhạt
        }

        return cellComponent;
    }
}
