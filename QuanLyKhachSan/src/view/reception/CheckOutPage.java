package view.reception;

import Common.Con;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class CheckOutPage extends JPanel {
    private JButton checkOutButton;
    private JTable roomListTb;
    private JButton checkOutBtn;
    private JButton cancelRoomBtn;
    private JScrollPane pnlTable;
    private DefaultTableModel tableModel;
    private JPanel pnlCheckout;
    private JPanel pnlChild;

    public CheckOutPage() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Booking ID", "Người đặt phòng", "Tên phòng", "Tình trạng phòng", "Tình trạng booking", "Ngày nhận phòng", "Ngày trả phòng"}, 0);
        roomListTb.setModel(tableModel);
        listBookingData();
        add(pnlChild);

        checkOutBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleCheckOut();
            }
        });

    }

    private void listBookingData() {

        Vector<Vector<Object>> vctData = new Vector<>();

        Con c = new Con();
        try {
            String sql = "SELECT b.booking_id, b.customer_name, b.room_id, b.email, b.description, b.check_in_date, b.check_out_date, r.clean_status AS room_status, b.booking_status, b.room_num " +
                    "FROM bookings b " +
                    "JOIN rooms r ON b.room_id = r.id " +
                    "WHERE b.booking_status = 'Đã nhận phòng'";
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

    private void handleCheckOut() {
        int selectedRow = roomListTb.getSelectedRow();

        if (selectedRow >= 0) {
            int bookingId = (int) tableModel.getValueAt(selectedRow, 0);
            String roomNum = (String) tableModel.getValueAt(selectedRow, 2);
            String roomStatus = (String) tableModel.getValueAt(selectedRow, 3);

            if (!"Đang sử dụng".equalsIgnoreCase(roomStatus)) {
                JOptionPane.showMessageDialog(this, "Phòng không sử dụng để check-out.");
                return;
            }

            try {
                Con c = new Con();
                String updateBookingSql = "UPDATE bookings SET booking_status = 'Đã trả phòng' WHERE booking_id = ?";
                try (PreparedStatement updateBookingStatement = c.connection.prepareStatement(updateBookingSql)) {
                    updateBookingStatement.setInt(1, bookingId);
                    updateBookingStatement.executeUpdate();
                }

                String updateRoomSql = "UPDATE rooms SET clean_status = 'Trống' WHERE room_number = ?";
                try (PreparedStatement updateRoomStatement = c.connection.prepareStatement(updateRoomSql)) {
                    updateRoomStatement.setString(1, roomNum);
                    updateRoomStatement.executeUpdate();
                }

                tableModel.setValueAt("Đã trả phòng", selectedRow, 4);
                tableModel.setValueAt("Trống", selectedRow, 3);

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
