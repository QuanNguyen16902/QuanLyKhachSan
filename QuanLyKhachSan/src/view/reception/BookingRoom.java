package view.reception;

import Common.Con;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookingRoom extends JFrame {

    private JTextField txtCusName;
    private JTextField txtCusPhone;
    private JRadioButton maleRdo;
    private JRadioButton femaleRdo;
    private JTextField txtCusEmail;
    private JTextField txtCusCCCD;
    private JLabel lblRoomName;
    private JRadioButton otherRdo;
    private JPanel pnlCheckinDate;
    private JPanel pnlCheckoutDate;
    private JButton submitBtn;
    private JTextField txtTotal;
    private JTextField txtDeposit;
    private JPanel pnlBooking;
    private JTextField txtCusAddress;
    private JTextArea descriptArea;
    private JLabel lblTotal;
    JDateChooser checkInDateChooser = new JDateChooser();
    JDateChooser checkOutDateChooser = new JDateChooser();
    ButtonGroup genderBg = new ButtonGroup();
    private String roomType;
    private double roomPrice;
    private Integer roomId;

    public BookingRoom(String roomNum){
        setContentPane(pnlBooking);
        setTitle("Đặt phòng " + roomNum);
        setSize(400, 500);
        lblRoomName.setText("Phòng " + roomNum);
        pnlCheckinDate.setLayout(new BorderLayout());
        pnlCheckinDate.add(checkInDateChooser);
        pnlCheckoutDate.setLayout(new BorderLayout());
        pnlCheckoutDate.add(checkOutDateChooser);

        genderBg.add(maleRdo);
        genderBg.add(femaleRdo);
        genderBg.add(otherRdo);

        loadRoomDetails(roomNum); // Load chi tiết phòng gồm cả giá và kiểu phòng
        txtTotal.setText(String.valueOf(roomPrice));
        txtTotal.setEditable(false);
        setVisible(true);
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc chắn muốn đặt phòng này?", "Xác nhận đặt phòng", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        addBooking(roomNum);
                    }
                }
            }
        });
    }

    private void loadRoomDetails(String roomNum) {
        Con c = new Con();
        try {
            String sql = "SELECT room_type, price, id FROM rooms WHERE room_number = ?";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sql);
            pstmt.setString(1, roomNum);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                roomType = rs.getString("room_type");
                roomPrice = rs.getDouble("price");
                roomId = rs.getInt("id");
            }
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    private boolean validateFields() {
        if (txtCusName.getText().isEmpty() || txtCusPhone.getText().isEmpty() || txtCusEmail.getText().isEmpty() ||
                txtCusCCCD.getText().isEmpty() || checkInDateChooser.getDate() == null || checkOutDateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các trường bắt buộc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // định dạng email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(txtCusEmail.getText());

        // SĐT phải có 11 số
        String phoneRegex = "^\\d{10}$";
        Pattern phonePattern = Pattern.compile(phoneRegex);
        Matcher phoneMatcher = phonePattern.matcher(txtCusPhone.getText());

        // CCCD phải có 12 số
        String idCardRegex = "^\\d{12}$";
        Pattern idCardPattern = Pattern.compile(idCardRegex);
        Matcher idCardMatcher = idCardPattern.matcher(txtCusCCCD.getText());

        // Validate email
        if (!emailMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng.");
            return false;
        }

        // Validate SDT
        if (!phoneMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải là 10 số.");
            return false;
        }

        // Validate CCCD
        if (!idCardMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "CCCD phải là 12 số.");
            return false;
        }
        return true;
    }
    private void addBooking(String roomNum) {
        String cusName = txtCusName.getText();
        String cusPhone = txtCusPhone.getText();
        String cusEmail = txtCusEmail.getText();
        String cusCCCD = txtCusCCCD.getText();
        String cusAddress = txtCusAddress.getText();
        String cusGender = "";

        if (maleRdo.isSelected()) {
            cusGender = "Nam";
        } else if (femaleRdo.isSelected()) {
            cusGender = "Nữ";
        } else if (otherRdo.isSelected()) {
            cusGender = "Khác";
        }

        java.util.Date checkInDate = checkInDateChooser.getDate();
        java.util.Date checkOutDate = checkOutDateChooser.getDate();
        java.sql.Timestamp checkInTimestamp = new java.sql.Timestamp(checkInDate.getTime());
        java.sql.Timestamp checkOutTimestamp = new java.sql.Timestamp(checkOutDate.getTime());
        // Tổng hóa đơn bằng số ngày * giá phòng
        long diffInMillies = Math.abs(checkOutDate.getTime() - checkInDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        double totalInvoice = diff * roomPrice;
        lblTotal.setText(String.valueOf(totalInvoice));

        String description = descriptArea.getText();

        Con c = new Con();
        try {
            // Thêm khách hàng vào bảng customers
            String sqlCustomer = "INSERT INTO customers (full_name, phone_number, email, id_card_number, gender, address) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmtCustomer = c.statement.getConnection().prepareStatement(sqlCustomer, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmtCustomer.setString(1, cusName);
            pstmtCustomer.setString(2, cusPhone);
            pstmtCustomer.setString(3, cusEmail);
            pstmtCustomer.setString(4, cusCCCD);
            pstmtCustomer.setString(5, cusGender);
            pstmtCustomer.setString(6, cusAddress);
            pstmtCustomer.executeUpdate();

            ResultSet rs = pstmtCustomer.getGeneratedKeys();
            int customerId = -1;
            if (rs.next()) {
                // Lấy customer_id vừa được tạo
                customerId = rs.getInt(1);
            }

            pstmtCustomer.close();

            // Thêm thông tin đặt phòng vào bảng bookings
            String sqlBooking = "INSERT INTO bookings (customer_id, room_num, room_type, total_invoice, check_in_date, check_out_date, customer_name, email, room_price, description, room_id) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmtBooking = c.statement.getConnection().prepareStatement(sqlBooking);
            pstmtBooking.setInt(1, customerId);
            pstmtBooking.setString(2, roomNum);
            pstmtBooking.setString(3, roomType);
            pstmtBooking.setDouble(4, totalInvoice);
            pstmtBooking.setTimestamp(5, checkInTimestamp);
            pstmtBooking.setTimestamp(6, checkOutTimestamp);
            pstmtBooking.setString(7, cusName);
            pstmtBooking.setString(8, cusEmail);
            pstmtBooking.setDouble(9, roomPrice);
            pstmtBooking.setString(10, description);
            pstmtBooking.setInt(11, roomId);
            pstmtBooking.executeUpdate();
            pstmtBooking.close();

            // Cập nhật trạng thái phòng trong bảng rooms
            String sqlUpdateRoom = "UPDATE rooms SET clean_status = 'Đã đặt' WHERE room_number = ?";
            PreparedStatement pstmtUpdateRoom = c.statement.getConnection().prepareStatement(sqlUpdateRoom);
            pstmtUpdateRoom.setString(1, roomNum);
            pstmtUpdateRoom.executeUpdate();
            pstmtUpdateRoom.close();

            JOptionPane.showMessageDialog(this, "Đặt phòng thành công!");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
