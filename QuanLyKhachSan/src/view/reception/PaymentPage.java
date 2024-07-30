package view.reception;

import Common.Con;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;

public class PaymentPage extends JPanel{
    private JPanel pnlChild;
    private JButton payBtn;
    private JScrollPane pnlTable;
    private JTable paymentListTb;
    private DefaultTableModel tableModel;
    public Integer selectedBookingId;
    public PaymentPage() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new Object[]{"Booking ID", "Người đặt phòng", "Tên phòng", "Tình trạng phòng", "Tình trạng booking", "Ngày nhận phòng", "Ngày trả phòng"}, 0);
        paymentListTb.setModel(tableModel);
        listBookingData();
        add(pnlChild);

        paymentListTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = paymentListTb.getSelectedRow();
                    if (selectedRow != -1) { // Nếu có dòng được chọn
                        selectedBookingId = (int) paymentListTb.getValueAt(selectedRow, 0); // Lấy id của booking đã chọn
                        String customerName = (String) paymentListTb.getValueAt(selectedRow, 1);
                        String roomNum = (String) paymentListTb.getValueAt(selectedRow, 2);
                        String roomStatus = (String) paymentListTb.getValueAt(selectedRow, 3);
                        String bookingStatus = (String) paymentListTb.getValueAt(selectedRow, 4);
                        Timestamp checkInDate = (Timestamp) paymentListTb.getValueAt(selectedRow, 5);
                        Timestamp checkOutDate = (Timestamp) paymentListTb.getValueAt(selectedRow, 6);

                    }
                }
            }
        });
        payBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                handlePaid();
            }
        });

        payBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Invoice(selectedBookingId);
            }
        });
    }

    private void listBookingData() {

        Vector<Vector<Object>> vctData = new Vector<>();

        Con c = new Con();
        try {
            String sql = "SELECT b.booking_id, b.customer_name, b.room_id, b.total_invoice, b.check_in_date, b.check_out_date, r.clean_status AS room_status, b.booking_status, b.room_num " +
                    "FROM bookings b " +
                    "JOIN rooms r ON b.room_id = r.id " +
                    "WHERE b.booking_status = 'Đã trả phòng'";
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

    private void handlePaid() {
        int selectedRow = paymentListTb.getSelectedRow();

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
