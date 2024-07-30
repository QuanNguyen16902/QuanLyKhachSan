package view.reception;

import Common.Con;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

public class Invoice extends JDialog {
    private JLabel lblEmployee;
    private JLabel lblRoomNum;
    private JLabel lblMaxOccupied;
    private JLabel lblNumOfDate;
    private JTable serviceListTb;
    private JLabel lblRoomPrice;
    private JLabel lblTotalPrice;
    private JTable amenityTable;
    private JLabel lblCreatedDate;
    private JLabel lblCusName;
    private JPanel pnlInvoice;
    private JButton confirmBtn;

    public Integer customerId;

    private Integer selectedBookingId;
    public Invoice(Integer selectedBookingId) {
        this.selectedBookingId = selectedBookingId;
        setTitle("Hóa Đơn");
        setSize(500, 600);

        setContentPane(pnlInvoice);

        Con c = new Con();
        try {
            // Query to get booking details
            String query = "SELECT b.*, r.room_number, r.max_occupied, r.price, " +
                    "c.full_name, NOW() AS created_date " +
                    "FROM bookings b " +
                    "JOIN rooms r ON b.room_id = r.id " +
                    "JOIN customers c ON b.customer_id = c.id " +
                    "WHERE b.booking_id = ?";
            PreparedStatement pst = c.connection.prepareStatement(query);
            pst.setInt(1, selectedBookingId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                lblCusName.setText(rs.getString("full_name"));
                lblRoomNum.setText(rs.getString("room_number"));
                lblMaxOccupied.setText(String.valueOf(rs.getInt("max_occupied")));
                lblRoomPrice.setText(String.format("%.2f", rs.getDouble("price")));
                lblTotalPrice.setText(rs.getString("total_invoice"));
                customerId = rs.getInt("customer_id");
                Date checkinDate = rs.getDate("check_in_date");
                Date checkoutDate = rs.getDate("check_out_date");
                if (checkinDate != null && checkoutDate != null) {
                    long diffInMillis = checkoutDate.getTime() - checkinDate.getTime();
                    long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
                    lblNumOfDate.setText(String.valueOf(diffInDays));
                }

                lblCreatedDate.setText(rs.getString("created_date"));
            }

            // Query to get service list
            String serviceQuery = "SELECT s.service_name, s.service_price " +
                    "FROM booking_services bs " +
                    "JOIN services s ON bs.service_id = s.service_id " +
                    "WHERE bs.booking_id = ?";
            pst = c.connection.prepareStatement(serviceQuery);
            pst.setInt(1, selectedBookingId);
            ResultSet serviceRs = pst.executeQuery();

            // Update service list table
            DefaultTableModel serviceModel = (DefaultTableModel) serviceListTb.getModel();
            while (serviceRs.next()) {
                serviceModel.addRow(new Object[] {
                        serviceRs.getString("service_name"),
                        serviceRs.getDouble("service_price")
                });
            }

            // Query to get amenities list
            String amenityQuery = "SELECT a.amenity_name " +
                    "FROM booking_amenities ba " +
                    "JOIN amenities a ON ba.amenity_id = a.amenity_id " +
                    "WHERE ba.booking_id = ?";
            pst = c.connection.prepareStatement(amenityQuery);
            pst.setInt(1, selectedBookingId);
            ResultSet amenityRs = pst.executeQuery();

            // Update amenities table
            DefaultTableModel amenityModel = (DefaultTableModel) amenityTable.getModel();
            while (amenityRs.next()) {
                amenityModel.addRow(new Object[] {
                        amenityRs.getString("amenity_name")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadBookingAmenities();
        loadBookingService();

        setVisible(true);
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Calculate total service and amenity amounts
                    double totalService = 0;
                    for (int i = 0; i < serviceListTb.getRowCount(); i++) {
                        totalService += (double) serviceListTb.getValueAt(i, 2);
                    }

                    double totalAmenity = 0;

                    for (int i = 0; i < amenityTable.getRowCount(); i++) {
                        // Assuming amenities also have prices if needed
                        totalAmenity += (double) amenityTable.getValueAt(i, 3);
                    }

                    double roomPrice = Double.parseDouble(lblRoomPrice.getText());
                    int numOfDays = Integer.parseInt(lblNumOfDate.getText());
                    double totalAmount = (roomPrice * numOfDays) + totalService + totalAmenity;

                    // Insert into invoices table
                    String insertInvoiceQuery = "INSERT INTO invoices (customer_id, invoice_date, num_of_people, " +
                            "num_of_day, room_price, total_service, total_amenity, total_amount) " +
                            "VALUES (?, NOW(), ?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertPst = c.connection.prepareStatement(insertInvoiceQuery);
                    insertPst.setInt(1, customerId);
                    insertPst.setInt(2, Integer.parseInt(lblMaxOccupied.getText()));
                    insertPst.setDouble(3, numOfDays);
                    insertPst.setDouble(4, roomPrice);
                    insertPst.setDouble(5, totalService);
                    insertPst.setDouble(6, totalAmenity);
                    insertPst.setDouble(7, totalAmount);

                    insertPst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Thanh toán thành công!");
                   // Thanh toán xong xóa bookings
                    String deleteBookingQuery = "DELETE FROM bookings WHERE booking_id = ?";
                    PreparedStatement deletePst = c.connection.prepareStatement(deleteBookingQuery);
                    deletePst.setInt(1, selectedBookingId);

                    deletePst.executeUpdate();

                    setVisible(false);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Lỗi.");
                }
            }
        });
    }
    private void loadBookingAmenities() {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Tên tiện nghi");
        columnNames.add("Số lượng");
        columnNames.add("Giá");

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
                row.add(rs.getDouble("price"));
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            amenityTable.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private void loadBookingService() {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID");
        columnNames.add("Tên dịch vụ");
        columnNames.add("Giá");

        Vector<Vector<Object>> data = new Vector<>();

        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement("SELECT ba.service_id, a.service_name, a.service_price FROM booking_services ba JOIN services a ON ba.service_id = a.service_id WHERE ba.booking_id = ?");

            pstmt.setInt(1, selectedBookingId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("service_id"));
                row.add(rs.getString("service_name"));
                row.add(rs.getDouble("service_price"));
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames);
            serviceListTb.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
