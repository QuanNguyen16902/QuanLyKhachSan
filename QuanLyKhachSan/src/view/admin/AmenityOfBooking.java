package view.admin;

import Common.Con;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class AmenityOfBooking extends JFrame {
    private JPanel pnlAB;
    private JScrollPane pnlTable;
    private JPanel pnlBelow;
    private JTable bookingAmenitiesTable;
    private JComboBox<String> idBox;
    private JTextField txtName;
    private JTextField txtQuantityRemain;
    private JButton addBtn;
    private JButton delBtn;
    private JPanel pnlAbove;
    private JTextField txtQuantityTake;

    private Integer selectedBookingId;
    private Map<Integer, String> amenityIdToNameMap = new HashMap<>();
    public AmenityOfBooking(Integer selectedBookingId) {
        this.selectedBookingId = selectedBookingId;
        setContentPane(pnlAB);
        setTitle("Danh sách tiện nghi của booking " + selectedBookingId);
        setSize(500, 400);
        setVisible(true);

        loadAmenities();
        loadBookingAmenities();

        idBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer amenityId = getSelectedAmenityId();
                if (amenityId != null) {
                    updateAmenityInfo(amenityId);
                }
            }
        });

        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addAmenity();
            }
        });

        delBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAmenity();
            }
        });

    }

    private void loadAmenities() {
        amenityIdToNameMap.clear();
        idBox.removeAllItems();
        Con c = new Con();
        try {
            ResultSet rs = c.statement.executeQuery("SELECT amenity_id, amenity_name FROM amenities WHERE quantity > 0 AND available = true");
            while (rs.next()) {
                int id = rs.getInt("amenity_id");
                String name = rs.getString("amenity_name");
                amenityIdToNameMap.put(id, name);
                idBox.addItem(String.valueOf(id));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadBookingAmenities() {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Tên tiện nghi");
        columnNames.add("Số lượng");
        columnNames.add("Giá");
        columnNames.add("Ghi chú");

        Vector<Vector<Object>> data = new Vector<>();

        Con c = new Con();
        try{
            PreparedStatement pstmt = c.connection.prepareStatement("SELECT ba.amenity_id, ba.quantity, a.amenity_name, a.price, a.notes FROM booking_amenities ba JOIN amenities a ON ba.amenity_id = a.amenity_id WHERE ba.booking_id = ?");

            pstmt.setInt(1, selectedBookingId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("amenity_id"));
                row.add(rs.getString("amenity_name"));
                row.add(rs.getInt("quantity"));
                row.add(rs.getString("price"));
                row.add(rs.getString("notes"));
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            bookingAmenitiesTable.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateAmenityInfo(Integer amenityId) {
        String name = amenityIdToNameMap.get(amenityId);
        if (name != null) {
            Con c = new Con();
            try {
                PreparedStatement pstmt = c.connection.prepareStatement("SELECT quantity FROM amenities WHERE amenity_id = ?");
                pstmt.setInt(1, amenityId);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    txtName.setText(name);
                    txtQuantityRemain.setText(String.valueOf(rs.getInt("quantity")));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Integer getSelectedAmenityId() {
        String selectedId = (String) idBox.getSelectedItem();
        return selectedId != null ? Integer.parseInt(selectedId) : null;
    }

    private void addAmenity() {
        Integer amenityId = getSelectedAmenityId();
        if (amenityId != null) {
            try {
                int quantityTake = Integer.parseInt(txtQuantityTake.getText());
                if (quantityTake <= 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0.");
                    return;
                }

                Con c = new Con();
                PreparedStatement checkAvailabilityStmt = c.connection.prepareStatement("SELECT price, quantity FROM amenities WHERE amenity_id = ?");
                PreparedStatement checkExistenceStmt = c.connection.prepareStatement("SELECT quantity FROM booking_amenities WHERE booking_id = ? AND amenity_id = ?");
                PreparedStatement updateBookingStmt = c.connection.prepareStatement("UPDATE bookings SET total_invoice = total_invoice + ? WHERE booking_id = ?");

                checkAvailabilityStmt.setInt(1, amenityId);
                ResultSet rsAvailability = checkAvailabilityStmt.executeQuery();

                if (rsAvailability.next()) {
                    int quantityRemain = rsAvailability.getInt("quantity");
                    double amenityPrice = rsAvailability.getDouble("price");
                    if (quantityRemain >= quantityTake) {
                        checkExistenceStmt.setInt(1, selectedBookingId);
                        checkExistenceStmt.setInt(2, amenityId);
                        ResultSet rsExistence = checkExistenceStmt.executeQuery();

                        int totalAmount = quantityTake * (int) amenityPrice;
                        if (rsExistence.next()) {
                            // Update the quantity if it already exists
                            int currentQuantity = rsExistence.getInt("quantity");
                            try (PreparedStatement updateStmt = c.connection.prepareStatement("UPDATE booking_amenities SET quantity = ? WHERE booking_id = ? AND amenity_id = ?")) {
                                updateStmt.setInt(1, currentQuantity + quantityTake);
                                updateStmt.setInt(2, selectedBookingId);
                                updateStmt.setInt(3, amenityId);
                                updateStmt.executeUpdate();
                            }
                        } else {
                            // Insert new entry if it does not exist
                            try (PreparedStatement insertStmt = c.connection.prepareStatement("INSERT INTO booking_amenities (booking_id, amenity_id, quantity) VALUES (?, ?, ?)")) {
                                insertStmt.setInt(1, selectedBookingId);
                                insertStmt.setInt(2, amenityId);
                                insertStmt.setInt(3, quantityTake);
                                insertStmt.executeUpdate();
                            }
                        }

                        // Update the quantity in the amenities table
                        try (PreparedStatement updateStmt = c.connection.prepareStatement("UPDATE amenities SET quantity = quantity - ? WHERE amenity_id = ?")) {
                            updateStmt.setInt(1, quantityTake);
                            updateStmt.setInt(2, amenityId);
                            updateStmt.executeUpdate();
                        }

                        // Update total_invoice for the booking
                        updateBookingStmt.setDouble(1, totalAmount);
                        updateBookingStmt.setInt(2, selectedBookingId);
                        updateBookingStmt.executeUpdate();

                        JOptionPane.showMessageDialog(this, "Thêm tiện nghi thành công.");
                        updateAmenityInfo(amenityId);
                        loadBookingAmenities();
                    } else {
                        JOptionPane.showMessageDialog(this, "Số lượng tiện nghi không đủ.");
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng hợp lệ.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }




    private void deleteAmenity() {
        int selectedRow = bookingAmenitiesTable.getSelectedRow();
        if (selectedRow != -1) {
            int amenityId = (Integer) bookingAmenitiesTable.getValueAt(selectedRow, 0);

            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotelms_db", "root", "amthambenem169");
                 PreparedStatement deleteStmt = con.prepareStatement("DELETE FROM booking_amenities WHERE booking_id = ? AND amenity_id = ?")) {

                deleteStmt.setInt(1, selectedBookingId);
                deleteStmt.setInt(2, amenityId);
                deleteStmt.executeUpdate();

                // Increase the quantity
                try (PreparedStatement updateStmt = con.prepareStatement("UPDATE amenities SET quantity = quantity + 1 WHERE amenity_id = ?")) {
                    updateStmt.setInt(1, amenityId);
                    updateStmt.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Xóa tiện nghi thành công.");
                updateAmenityInfo(amenityId);
                loadBookingAmenities();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Hãy chọn tiện nghi để xóa.");
        }
    }
}
