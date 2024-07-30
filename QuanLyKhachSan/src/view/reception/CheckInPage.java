package view.reception;

import Common.Con;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class CheckInPage extends JPanel {
    private JTextField roomIdField;
    private JTextField customerNameField;
    private JButton checkInButton;
    private JButton checkOutButton;
    private JTextArea statusArea;
    private JTable roomListTb;
    private JButton checkInBtn;
    private JButton cancelRoomBtn;
    private JPanel pnlCheckin;
    private JScrollPane pnlTable;
    private DefaultTableModel tableModel;

    public CheckInPage() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Booking ID", "Người đặt phòng", "Tên phòng", "Tình trạng phòng", "Tình trạng booking", "Ngày nhận phòng", "Ngày trả phòng"}, 0);
        roomListTb.setModel(tableModel);
        listBookingData();
        add(pnlCheckin);

        checkInBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleCheckIn();
            }
        });

        cancelRoomBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCancelBooking();
            }
        });

    }

    private void handleCancelBooking() {
        int selectedRow = roomListTb.getSelectedRow();

        if (selectedRow >= 0) {
            int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
            String bookingStatus = (String) tableModel.getValueAt(selectedRow, 4);

            if (!"Chờ xác nhận".equalsIgnoreCase(bookingStatus)) {
                JOptionPane.showMessageDialog(this, "Chỉ có thể hủy các đơn đặt phòng đang chờ xác nhận.");
                return;
            }

            try {
                Con c = new Con();
                String updateBookingSql = "UPDATE bookings SET booking_status = 'Hủy đặt phòng' WHERE booking_id = ?";
                try (PreparedStatement updateBookingStatement = c.connection.prepareStatement(updateBookingSql)) {
                    updateBookingStatement.setInt(1, bookingId);
                    updateBookingStatement.executeUpdate();
                }

                // Cập nhật trạng thái trong bảng
                tableModel.setValueAt("Hủy đặt phòng", selectedRow, 4);

                JOptionPane.showMessageDialog(this, "Đã hủy đặt phòng thành công.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn đặt phòng để hủy.");
        }
    }



    private void listBookingData() {

        Vector<Vector<Object>> vctData = new Vector<>();

        Con c = new Con();
        try {
            String sql = "SELECT b.booking_id, b.customer_name, b.room_id, b.email, b.description, b.check_in_date, b.check_out_date, r.clean_status AS room_status, b.booking_status, b.room_num " +
                    "FROM bookings b " +
                    "JOIN rooms r ON b.room_id = r.id " +
                    "WHERE b.booking_status = 'Chờ xác nhận'";
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> vctRow = new Vector<>();
                vctRow.add(rs.getInt("booking_id"));
                vctRow.add(rs.getString("customer_name"));
                vctRow.add(rs.getString("room_num"));
                vctRow.add(rs.getString("room_status"));
                vctRow.add(rs.getString("booking_status"));
                vctRow.add(rs.getTimestamp("check_in_date"));
                vctRow.add(rs.getTimestamp("check_out_date"));
                vctData.add(vctRow);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        for (Vector<Object> row : vctData) {
            tableModel.addRow(row);
        }
    }

    private void handleCheckIn() {
        int selectedRow = roomListTb.getSelectedRow();

        if (selectedRow >= 0) {
            int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
            String roomNum = (String) tableModel.getValueAt(selectedRow, 2);
            String roomStatus = (String) tableModel.getValueAt(selectedRow, 3);

            if (!"Đã đặt".equalsIgnoreCase(roomStatus) && !"Trống".equalsIgnoreCase(roomStatus)) {
                JOptionPane.showMessageDialog(this, "Phòng không trống để check-in.");
                return;
            }

            try {
                Con c = new Con();
                String updateBookingSql = "UPDATE bookings SET booking_status = 'Đã nhận phòng' WHERE booking_id = ?";
                try (PreparedStatement updateBookingStatement = c.connection.prepareStatement(updateBookingSql)) {
                    updateBookingStatement.setInt(1, bookingId);
                    updateBookingStatement.executeUpdate();
                }

                String updateRoomSql = "UPDATE rooms SET clean_status = 'Đang sử dụng' WHERE room_number = ?";
                try (PreparedStatement updateRoomStatement = c.connection.prepareStatement(updateRoomSql)) {
                    updateRoomStatement.setString(1, roomNum);
                    updateRoomStatement.executeUpdate();
                }

                tableModel.setValueAt("Đã nhận phòng", selectedRow, 4);
                tableModel.setValueAt("Đang sử dụng", selectedRow, 3);

                JOptionPane.showMessageDialog(this, "Check-in thành công.");
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi cơ sở dữ liệu: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đặt phòng để check-in.");
        }
    }


}
