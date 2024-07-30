package view.reception;

import Common.Con;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoomMap extends JPanel {
    private JTabbedPane roomTabbedPane;
    private JPanel roomType;
    private JPanel pnlRoomMap;
    private JPanel pnl;

    public RoomMap() {
        setLayout(new BorderLayout());
        add(pnlRoomMap);

        loadRoomTypes();
    }

    private void loadRoomTypes() {
        Con c = new Con();
        try {
            String sqlRoomTypes = "SELECT DISTINCT room_type FROM rooms";
            ResultSet rsRoomTypes = c.statement.executeQuery(sqlRoomTypes);
            roomTabbedPane.removeAll(); // Clear existing tabs
            while (rsRoomTypes.next()) {
                String roomType = rsRoomTypes.getString("room_type");
                JPanel roomTypePanel = new JPanel();
                // Giả định có 5 phòng trên mỗi hàng, khoảng cách giữa các ô vuông là 10px
                roomTypePanel.setLayout(new GridLayout(0, 5, 10, 10));
                roomTypePanel.setBackground(new Color(253, 240, 192));
                loadRoomsForType(roomType, roomTypePanel);

                roomTabbedPane.addTab(roomType, roomTypePanel);
            }
            rsRoomTypes.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadRoomsForType(String roomType, JPanel roomTypePanel) {
        Con c = new Con();
        try {
            String sqlRooms = "SELECT room_number, clean_status, bed_type, number_of_bed, max_occupied FROM rooms WHERE room_type = ?";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sqlRooms);
            pstmt.setString(1, roomType);
            ResultSet rsRooms = pstmt.executeQuery();

            while (rsRooms.next()) {
                String roomNumber = rsRooms.getString("room_number");
                String cleanStatus = rsRooms.getString("clean_status");
                String bedType = rsRooms.getString("bed_type");
                int numberOfBeds = rsRooms.getInt("number_of_bed");
                int maxOccupied = rsRooms.getInt("max_occupied");


                RoomPanel roomPanel = new RoomPanel(roomNumber, cleanStatus, bedType, numberOfBeds, maxOccupied);
                // Thêm trình lắng nghe sự kiện chuột vào ô phòng
                roomPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            showRoomOptions(e, roomNumber, cleanStatus);
                        }
                    }
                });

                roomTypePanel.add(roomPanel);
            }
            rsRooms.close();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showRoomOptions(MouseEvent e, String roomNumber, String cleanStatus) {
        JPopupMenu roomOptions = new JPopupMenu();
        JMenuItem bookRoom = new JMenuItem("Đặt phòng");
        JMenuItem cleanRoom = new JMenuItem("Dọn dẹp");
        JMenuItem repairRoom = new JMenuItem("Bảo trì");
        JMenuItem freeRoom = new JMenuItem("Trống");
        JMenuItem inUseRoom = new JMenuItem("Đang sử dụng");

        // Thêm các mục tùy chọn vào menu
        roomOptions.add(bookRoom);
        roomOptions.add(cleanRoom);
        roomOptions.add(repairRoom);
        roomOptions.add(freeRoom);
        roomOptions.add(inUseRoom);

        // Hiển thị menu tại vị trí chuột
        roomOptions.show(e.getComponent(), e.getX(), e.getY());

        // Thêm các hành động khi chọn các mục trong menu
        bookRoom.addActionListener(event -> {
            if (cleanStatus.equals("Trống") || cleanStatus.equals("Đang dọn dẹp")) {
                new BookingRoom(roomNumber);
            } else if(cleanStatus.equals("Đang sử dụng")){
                JOptionPane.showMessageDialog(this, "Phòng đang được sử dụng.");
            }else if(cleanStatus.equals("Bảo trì")){
                JOptionPane.showMessageDialog(this, "Phòng đang bảo trì.");
            }
            else if(cleanStatus.equals("Đã đặt")){
                JOptionPane.showMessageDialog(this, "Phòng đã được đặt.");
            }
        });
        cleanRoom.addActionListener(event -> updateRoomStatus(roomNumber, "Đang dọn dẹp"));
        repairRoom.addActionListener(event -> updateRoomStatus(roomNumber, "Bảo trì"));
        inUseRoom.addActionListener(event -> updateRoomStatus(roomNumber, "Đang sử dụng"));
        freeRoom.addActionListener(event -> updateRoomStatus(roomNumber, "Trống"));
    }

    private void updateRoomStatus(String roomNumber, String newStatus) {
        Con c = new Con();
        try {
            String sqlUpdate = "UPDATE rooms SET clean_status = ? WHERE room_number = ?";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sqlUpdate);
            pstmt.setString(1, newStatus);
            pstmt.setString(2, roomNumber);
            int updatedRows = pstmt.executeUpdate();
            if (updatedRows > 0) {
                JOptionPane.showMessageDialog(this, "Trạng thái của phòng " + roomNumber + " đã được cập nhật thành " + newStatus);
                refreshRoomMap(roomTabbedPane.getSelectedIndex());
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại cho phòng " + roomNumber);
            }
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void refreshRoomMap(int selectedIndex) {
        roomTabbedPane.removeAll();
        loadRoomTypes();
        roomTabbedPane.setSelectedIndex(selectedIndex);
    }

}
