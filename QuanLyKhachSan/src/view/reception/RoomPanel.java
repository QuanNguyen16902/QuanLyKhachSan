package view.reception;

import javax.swing.*;
import java.awt.*;

public class RoomPanel extends JPanel{
    private JLabel roomNumberLabel;
    private JLabel cleanStatusLabel;
    private JLabel bedTypeLabel;
    private JLabel numberOfBedsLabel;
    private JLabel maxOccupiedLabel;
    private JPanel pnlRoomPanel;

    public RoomPanel(String roomNumber, String cleanStatus, String bedType, int numberOfBeds, int maxOccupied) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        add(pnlRoomPanel);
        roomNumberLabel.setText(roomNumber);
        cleanStatusLabel.setText(cleanStatus);
        bedTypeLabel.setText("Loại giường: " + bedType);
        numberOfBedsLabel.setText("Số giường: " + numberOfBeds);
        maxOccupiedLabel.setText("Số người tối đa: " + maxOccupied);

        updateRoomColor(cleanStatus);

    }
    public void updateRoomColor(String cleanStatus) {
        Color statusColor;
        switch (cleanStatus) {
            case "Trống":
                statusColor = new Color(144, 238, 144);
                break;
            case "Đang sử dụng":
                statusColor = new Color(255, 105, 97);
                break;
            case "Đang dọn dẹp":
                statusColor = new Color(253, 253, 150);
                break;
            case "Đã đặt":
                statusColor = new Color(255, 233, 0);
                break;
            case "Bảo trì":
                statusColor = new Color(211, 211, 211);
                break;
            default:
                statusColor = Color.GRAY;
                break;
        }
        pnlRoomPanel.setBackground(statusColor);
        pnlRoomPanel.setSize(100,100);
    }
}
