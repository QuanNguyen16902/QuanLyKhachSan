package view.admin;

import Common.Con;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookingDetail  extends JFrame {


        public BookingDetail(int bookingId) {
            setTitle("Chi tiết đặt phòng");
            setSize(800, 600);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // Panel chính để chứa thông tin chi tiết
            JPanel contentPanel = new JPanel(new BorderLayout());

            JTextArea detailsTextArea = new JTextArea();
            detailsTextArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(detailsTextArea);

            // Thêm scrollPane vào contentPanel
            contentPanel.add(scrollPane, BorderLayout.CENTER);
            add(contentPanel);

            // Load thông tin chi tiết đặt phòng
            loadBookingDetails(bookingId, detailsTextArea);
        }

        private void loadBookingDetails(int bookingId, JTextArea detailsTextArea) {
            Con c = new Con();
            try {
                String sqlBooking = "SELECT * FROM bookings WHERE booking_id = ?";
                PreparedStatement pstmtBooking = c.statement.getConnection().prepareStatement(sqlBooking);
                pstmtBooking.setInt(1, bookingId);
                ResultSet rsBooking = pstmtBooking.executeQuery();

                if (rsBooking.next()) {
                    StringBuilder details = new StringBuilder();
                    details.append("ID: ").append(rsBooking.getInt("booking_id")).append("\n");
                    details.append("Người đặt phòng: ").append(rsBooking.getString("customer_name")).append("\n");
                    details.append("Email: ").append(rsBooking.getString("email")).append("\n");
                    details.append("Số phòng: ").append(rsBooking.getString("room_num")).append("\n");
                    details.append("Loại phòng: ").append(rsBooking.getString("room_type")).append("\n");
                    details.append("Giá phòng: ").append(rsBooking.getDouble("room_price")).append("\n");
                    details.append("Trạng thái: ").append(rsBooking.getString("booking_status")).append("\n");
                    details.append("Mô tả: ").append(rsBooking.getString("description")).append("\n");
                    details.append("Ngày nhận phòng: ").append(rsBooking.getTimestamp("check_in_date")).append("\n");
                    details.append("Ngày trả phòng: ").append(rsBooking.getTimestamp("check_out_date")).append("\n");
                    details.append("Ngày tạo: ").append(rsBooking.getTimestamp("created_at")).append("\n");

                    // Thêm tiện nghi
                    details.append("\nTiện nghi:\n");
                    String sqlAmenities = "SELECT a.amenity_name FROM booking_amenities ba JOIN amenities a ON ba.amenity_id = a.amenity_id WHERE ba.booking_id = ?";
                    PreparedStatement pstmtAmenities = c.statement.getConnection().prepareStatement(sqlAmenities);
                    pstmtAmenities.setInt(1, bookingId);
                    ResultSet rsAmenities = pstmtAmenities.executeQuery();
                    while (rsAmenities.next()) {
                        details.append("- ").append(rsAmenities.getString("amenity_name")).append("\n");
                    }
                    rsAmenities.close();
                    pstmtAmenities.close();

                    // Thêm dịch vụ
                    details.append("\nDịch vụ:\n");
                    String sqlServices = "SELECT s.service_name FROM booking_services bs JOIN services s ON bs.service_id = s.service_id WHERE bs.booking_id = ?";
                    PreparedStatement pstmtServices = c.statement.getConnection().prepareStatement(sqlServices);
                    pstmtServices.setInt(1, bookingId);
                    ResultSet rsServices = pstmtServices.executeQuery();
                    while (rsServices.next()) {
                        details.append("- ").append(rsServices.getString("service_name")).append("\n");
                    }
                    rsServices.close();
                    pstmtServices.close();

                    detailsTextArea.setText(details.toString());
                }
                rsBooking.close();
                pstmtBooking.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }

}
