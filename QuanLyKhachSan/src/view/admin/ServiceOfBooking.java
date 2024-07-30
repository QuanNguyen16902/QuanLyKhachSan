package view.admin;

import Common.Con;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class ServiceOfBooking extends JFrame{
    private JPanel pnlAbove;
    private JButton delBtn;
    private JTextField txtName;
    private JTextField txtAvailable;
    private JComboBox idBox;
    private JButton addBtn;
    private JPanel pnlBelow;
    private JScrollPane pnlTable;
    private JTable bookingServiceTable;
    private JPanel pnlSB;
    private Integer selectedBookingId;
    private Map<Integer, String> serviceIdToNameMap = new HashMap<>();

    public ServiceOfBooking(Integer selectedBookingId){
        this.selectedBookingId = selectedBookingId;
        setContentPane(pnlSB);
        setTitle("Các dịch vụ của booking (" + selectedBookingId + ")");
        setSize(500, 400);
        setVisible(true);

        loadService();
        loadBookingService();

        idBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer serviceId = getSelectedServiceId();
                if (serviceId != null) {
                    updateServiceInfo(serviceId);
                }
            }
        });

        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addService();
            }
        });

        delBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteService();
            }
        });

    }
    private void loadService() {
        serviceIdToNameMap.clear();
        idBox.removeAllItems();
        Con c = new Con();
        try {
            Statement stmt = c.connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT service_id, service_name FROM services");
            while (rs.next()) {
                int id = rs.getInt("service_id");
                String name = rs.getString("service_name");
                serviceIdToNameMap.put(id, name);
                idBox.addItem(String.valueOf(id));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadBookingService() {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Tên dịch vụ");
        columnNames.add("Giá");
        columnNames.add("Nhà cung cấp");
        columnNames.add("Mô tả");

        Vector<Vector<Object>> data = new Vector<>();

        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement("SELECT ba.service_id, a.service_name, a.service_price, a.service_provider, a.description FROM booking_services ba JOIN services a ON ba.service_id = a.service_id WHERE ba.booking_id = ?");

            pstmt.setInt(1, selectedBookingId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("service_id"));
                row.add(rs.getString("service_name"));
                row.add(rs.getString("service_price"));
                row.add(rs.getString("service_provider"));
                row.add(rs.getString("description"));
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            bookingServiceTable.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void addService() {
        Integer serviceId = getSelectedServiceId();
        if (serviceId != null) {
            try {
                Con c = new Con();
                // Kiểm tra tính sẵn có của dịch vụ
                PreparedStatement checkAvailabilityStmt = c.connection.prepareStatement("SELECT available, service_price FROM services WHERE service_id = ?");
                // Kiểm tra dịch vụ đã tồn tại trong booking_services
                PreparedStatement checkExistenceStmt = c.connection.prepareStatement("SELECT COUNT(*) FROM booking_services WHERE booking_id = ? AND service_id = ?");
                // Cập nhật total_invoice
                PreparedStatement updateBookingStmt = c.connection.prepareStatement("UPDATE bookings SET total_invoice = total_invoice + ? WHERE booking_id = ?");

                checkAvailabilityStmt.setInt(1, serviceId);
                ResultSet rsAvailability = checkAvailabilityStmt.executeQuery();

                if (rsAvailability.next()) {
                    String available = rsAvailability.getString("available");
                    double servicePrice = rsAvailability.getDouble("service_price");

                    if (Objects.equals(available, "Có sẵn")) {
                        checkExistenceStmt.setInt(1, selectedBookingId);
                        checkExistenceStmt.setInt(2, serviceId);
                        ResultSet rsExistence = checkExistenceStmt.executeQuery();

                        if (rsExistence.next() && rsExistence.getInt(1) == 0) {
                            // Thêm dịch vụ vào bảng booking_services
                            try (PreparedStatement insertStmt = c.connection.prepareStatement("INSERT INTO booking_services (booking_id, service_id) VALUES (?, ?)")) {
                                insertStmt.setInt(1, selectedBookingId);
                                insertStmt.setInt(2, serviceId);
                                insertStmt.executeUpdate();
                            }

                            // Cập nhật total_invoice cho booking
                            updateBookingStmt.setDouble(1, servicePrice);
                            updateBookingStmt.setInt(2, selectedBookingId);
                            updateBookingStmt.executeUpdate();

                            JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công.");
                            updateServiceInfo(serviceId);
                            loadBookingService();
                        } else {
                            JOptionPane.showMessageDialog(this, "Dịch vụ đã được thêm.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Dịch vụ này không có sẵn.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void updateServiceInfo(Integer serviceId) {
        String name = serviceIdToNameMap.get(serviceId);
        if (name != null) {
            try {
                Con c = new Con();
                PreparedStatement pstmt = c.connection.prepareStatement("SELECT available FROM services WHERE service_id = ?");

                    pstmt.setInt(1, serviceId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    txtName.setText(name);
                    txtAvailable.setText(rs.getString("available"));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Integer getSelectedServiceId() {
        String selectedId = (String) idBox.getSelectedItem();
        return selectedId != null ? Integer.parseInt(selectedId) : null;
    }



            private void deleteService() {
        int selectedRow = bookingServiceTable.getSelectedRow();
        if (selectedRow != -1) {
            int serviceId = (Integer) bookingServiceTable.getValueAt(selectedRow, 0);

            Con c = new Con();
            try{
                PreparedStatement deleteStmt = c.connection.prepareStatement("DELETE FROM booking_services WHERE booking_id = ? AND service_id = ?");

                deleteStmt.setInt(1, selectedBookingId);
                deleteStmt.setInt(2, serviceId);
                deleteStmt.executeUpdate();


                JOptionPane.showMessageDialog(this, "Xóa dịch vụ thành công.");
                loadBookingService();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Bạn chưa chọn dịch vụ.");
        }
    }
}
