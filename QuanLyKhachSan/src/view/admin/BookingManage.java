package view.admin;

import Common.Con;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class BookingManage extends JPanel {
    private JPanel pnlAbove;
    private JPanel pnlTitle;
    private JPanel pnlField;
    private JButton deleteBtn;
    private JButton listBtn;
    private JButton addBtn;
    private JButton editBtn;
    private JButton clearButton;
    private JButton searchBtn;
    private JTextField txtSearch;
    private JComboBox roomNumBox;
    private JComboBox bookStatusBox;
    private JPanel pnlBelow;
    private JScrollPane pnlTable;
    private JTable listBookingTb;
    private JPanel jCalendarCheckIn;
    private JPanel pnlBooking;
    private JPanel jCalendarCheckout;
    private JComboBox roomTypeBox;
    private JComboBox customerNameBox;
    private JTextField txtRoomStatus;
    private JComboBox emailBox;
    private JTextArea descriptionTextArea;
    private JButton amenityBtn;
    private JButton serviceBtn;
    private JTextField txtRoomPrice;
    private JButton detailBtn;

    private int selectedBookingId;  // Thêm trường này để lưu trữ id của booking đã chọn
    //    Calendar calendarCheckIn = Calendar.getInstance();
    JDateChooser dateChooserCheckIn = new JDateChooser();
    //    Calendar calendarCheckOut = Calendar.getInstance();
    JDateChooser dateChooserCheckOut = new JDateChooser();

    public BookingManage() {
        setLayout(new BorderLayout());
        jCalendarCheckIn.setLayout(new BorderLayout());
        jCalendarCheckIn.add(dateChooserCheckIn);
        dateChooserCheckIn.setDateFormatString("yyyy-MM-dd HH:mm:ss ");

        jCalendarCheckout.setLayout(new BorderLayout());
        jCalendarCheckout.add(dateChooserCheckOut);
        dateChooserCheckOut.setDateFormatString("yyyy-MM-dd HH:mm:ss ");
        txtRoomStatus.setEditable(false);
        txtRoomPrice.setEditable(false);
        add(pnlBooking);
        Con c = new Con();
        try {
            String sqlRoomType = "SELECT DISTINCT room_type FROM rooms";
            ResultSet rsRoomType = c.statement.executeQuery(sqlRoomType);
            roomTypeBox.removeAllItems();
            while (rsRoomType.next()) {
                roomTypeBox.addItem(rsRoomType.getString("room_type"));
            }
            rsRoomType.close();

            // Truy vấn các số phòng duy nhất
            String sqlRoomNumber = "SELECT DISTINCT room_number FROM rooms";
            ResultSet rsRoomNumber = c.statement.executeQuery(sqlRoomNumber);
            roomNumBox.removeAllItems();
            while (rsRoomNumber.next()) {
                roomNumBox.addItem(rsRoomNumber.getString("room_number"));
            }
            rsRoomNumber.close();

            String sqlCustomerName = "SELECT DISTINCT full_name FROM customers";
            ResultSet rsCustomerName = c.statement.executeQuery(sqlCustomerName);
            customerNameBox.removeAllItems();
            while (rsCustomerName.next()) {
                customerNameBox.addItem(rsCustomerName.getString("full_name"));
            }
            rsCustomerName.close();

            String[] bookingStatusOps = {"Chờ xác nhận", "Xác nhận", "Đã nhận phòng", "Đã trả phòng", "Hủy đặt phòng",  "Không đến"};
            Arrays.stream(bookingStatusOps).forEach(bookStatusBox::addItem);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        listBookingTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = listBookingTb.getSelectedRow();
                    if (selectedRow != -1) { // Nếu có dòng được chọn
                        selectedBookingId = (int) listBookingTb.getValueAt(selectedRow, 0); // Lấy id của booking đã chọn
                        String customerName = (String) listBookingTb.getValueAt(selectedRow, 1);
                        String email = (String) listBookingTb.getValueAt(selectedRow, 2);
                        String roomNum = (String) listBookingTb.getValueAt(selectedRow, 3);
                        String roomType = (String) listBookingTb.getValueAt(selectedRow, 4);
                        String roomPrice = (String) listBookingTb.getValueAt(selectedRow, 5);
                        String bookingStatus = (String) listBookingTb.getValueAt(selectedRow, 6);
                        String description = (String) listBookingTb.getValueAt(selectedRow, 7);

                        Timestamp checkInDate = (Timestamp) listBookingTb.getValueAt(selectedRow, 8);
                        Timestamp checkOutDate = (Timestamp) listBookingTb.getValueAt(selectedRow, 9);
//                        Timestamp createdAt = (Timestamp) listBookingTb.getValueAt(selectedRow, 10);

                        // Hiển thị thông tin lên các JTextField
                        customerNameBox.setSelectedItem(customerName);
                        roomNumBox.setSelectedItem(roomNum);
                        roomTypeBox.setSelectedItem(roomType);
                        emailBox.setSelectedItem(email);
                        bookStatusBox.setSelectedItem(bookingStatus);
                        txtRoomPrice.setText(roomPrice);
                        descriptionTextArea.setText(description);
                        dateChooserCheckOut.setDate(checkOutDate);
                        dateChooserCheckIn.setDate(checkInDate);



                    }
                }
            }
        });

        listBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listBookingData();
            }

        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBooking();
            }
        });
        roomTypeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedRoomType = (String) roomTypeBox.getSelectedItem();
                    Con c = new Con();
                    try {
                        String sqlRoomNumber = "SELECT room_number FROM rooms WHERE room_type = ?";
                        PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sqlRoomNumber);
                        pstmt.setString(1, selectedRoomType);
                        ResultSet rsRoomNumber = pstmt.executeQuery();
                        roomNumBox.removeAllItems();
                        while (rsRoomNumber.next()) {
                            roomNumBox.addItem(rsRoomNumber.getString("room_number"));
                        }
                        rsRoomNumber.close();
                        pstmt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        customerNameBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedCustomer = (String) customerNameBox.getSelectedItem();
                    Con c = new Con();
                    try {
                        String sqlCustomer = "SELECT email FROM customers WHERE full_name = ?";
                        PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sqlCustomer);
                        pstmt.setString(1, selectedCustomer);
                        ResultSet rsCustomer = pstmt.executeQuery();
                        emailBox.removeAllItems();
                        while (rsCustomer.next()) {
                            emailBox.addItem(rsCustomer.getString("email"));
                        }
                        rsCustomer.close();
                        pstmt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        roomNumBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String selectedRoomNum = (String) roomNumBox.getSelectedItem();
                    Con c = new Con();
                    try {
                        String sqlRoomNum = "SELECT clean_status, price FROM rooms WHERE room_number = ?";
                        PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sqlRoomNum);
                        pstmt.setString(1, selectedRoomNum);
                        ResultSet rsRoomNum = pstmt.executeQuery();
                        while (rsRoomNum.next()) {
                            txtRoomStatus.setText(rsRoomNum.getString("clean_status"));
                            txtRoomPrice.setText(rsRoomNum.getString("price"));
                        }
                        rsRoomNum.close();
                        pstmt.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editBooking();
            }
        });
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBooking();
            }
        });

        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = txtSearch.getText().trim();
                searchBooking(searchText);
            }
        });
        amenityBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedBookingId != 0){
                    new AmenityOfBooking(selectedBookingId);
                }else {
                    JOptionPane.showMessageDialog(null,"Bạn chưa chọn booking");
                }
            }
        });
        serviceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedBookingId != 0){
                    new ServiceOfBooking(selectedBookingId);
                }else {
                    JOptionPane.showMessageDialog(null,"Bạn chưa chọn booking");
                }
            }
        });

        detailBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedBookingId != 0) {
                    showInvoice(selectedBookingId);
                } else {
                    JOptionPane.showMessageDialog(null, "Bạn chưa chọn booking");
                }
            }
        });
    }
    private void showInvoice(int bookingId) {
        // Tạo JFrame để hiển thị hóa đơn
        JFrame invoiceFrame = new JFrame("Hóa đơn");
        invoiceFrame.setSize(600, 400);
        invoiceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        invoiceFrame.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(0, 1));

        Con c = new Con();
        try {
            // Truy vấn thông tin booking
            String sqlBooking = "SELECT * FROM bookings WHERE booking_id = ?";
            PreparedStatement pstmtBooking = c.statement.getConnection().prepareStatement(sqlBooking);
            pstmtBooking.setInt(1, bookingId);
            ResultSet rsBooking = pstmtBooking.executeQuery();

            if (rsBooking.next()) {
                // Tính tiền phòng
                double roomPrice = rsBooking.getDouble("room_price");
                Timestamp checkIndate = rsBooking.getTimestamp("check_in_date");
                Timestamp checkOutdate = rsBooking.getTimestamp("check_out_date");
                long diffInMillies = Math.abs(checkOutdate.getTime() - checkIndate.getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                double totalRoomPrice = diff * roomPrice;
                // Hiển thị thông tin booking
                contentPanel.add(new JLabel("ID: " + rsBooking.getInt("booking_id")));
                contentPanel.add(new JLabel("Người đặt phòng: " + rsBooking.getString("customer_name")));
                contentPanel.add(new JLabel("Email: " + rsBooking.getString("email")));
                contentPanel.add(new JLabel("Số phòng: " + rsBooking.getString("room_num")));
                contentPanel.add(new JLabel("Loại phòng: " + rsBooking.getString("room_type")));
                contentPanel.add(new JLabel("Giá phòng: " + rsBooking.getDouble("room_price")));
                contentPanel.add(new JLabel("Trạng thái: " + rsBooking.getString("booking_status")));
                contentPanel.add(new JLabel("Mô tả: " + rsBooking.getString("description")));
                contentPanel.add(new JLabel("Ngày nhận phòng: " + rsBooking.getTimestamp("check_in_date")));
                contentPanel.add(new JLabel("Ngày trả phòng: " + rsBooking.getTimestamp("check_out_date")));
                contentPanel.add(new JLabel("Số ngày ở: " + diff));


                // Tính tiền tiện nghi
                double totalAmenitiesPrice = 0;
                String sqlAmenities = "SELECT a.price, ba.quantity FROM booking_amenities ba JOIN amenities a ON ba.amenity_id = a.amenity_id WHERE ba.booking_id = ?";
                PreparedStatement pstmtAmenities = c.statement.getConnection().prepareStatement(sqlAmenities);
                pstmtAmenities.setInt(1, bookingId);
                ResultSet rsAmenities = pstmtAmenities.executeQuery();
                while (rsAmenities.next()) {
                    totalAmenitiesPrice += rsAmenities.getDouble("price") * rsAmenities.getInt("quantity");
                }

                // Tính tiền dịch vụ
                double totalServicesPrice = 0;
                String sqlServices = "SELECT s.service_price FROM booking_services bs JOIN services s ON bs.service_id = s.service_id WHERE bs.booking_id = ?";
                PreparedStatement pstmtServices = c.statement.getConnection().prepareStatement(sqlServices);
                pstmtServices.setInt(1, bookingId);
                ResultSet rsServices = pstmtServices.executeQuery();
                while (rsServices.next()) {
                    totalServicesPrice += rsServices.getDouble("service_price");
                }

                // Tính tổng tiền

                double totalAmount = totalRoomPrice + totalAmenitiesPrice + totalServicesPrice;
                contentPanel.add(new JLabel("Tiền phòng: " + roomPrice));
                contentPanel.add(new JLabel("Tiền tiện nghi: " + totalAmenitiesPrice));
                contentPanel.add(new JLabel("Tiền dịch vụ: " + totalServicesPrice));
                contentPanel.add(new JLabel("Tổng tiền: " + totalAmount));
            }

            rsBooking.close();
            pstmtBooking.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        invoiceFrame.add(scrollPane, BorderLayout.CENTER);
        invoiceFrame.setVisible(true);
    }






    private void listBookingData() {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("ID");  // Thêm ID vào tiêu đề
        vctHeader.add("Người đặt phòng");
        vctHeader.add("Email");
        vctHeader.add("Số phòng");
        vctHeader.add("Loại phòng");
        vctHeader.add("Giá phòng");
        vctHeader.add("Trạng thái");
        vctHeader.add("Mô tả");
        vctHeader.add("Ngày nhận phòng");
        vctHeader.add("Ngày trả phòng");
        vctHeader.add("Ngày tạo");

        Vector<Vector<Object>> vctData = new Vector<>();

        Con c = new Con();
        try {
            String sql = "SELECT * FROM bookings";
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> vctRow = new Vector<>();
                vctRow.add(rs.getInt("booking_id"));  // Thêm id vào dữ liệu hàng
                vctRow.add(rs.getString("customer_name"));
                vctRow.add(rs.getString("email"));
                vctRow.add(rs.getString("room_num"));
                vctRow.add(rs.getString("room_type"));
                vctRow.add(rs.getString("room_price"));
                vctRow.add(rs.getString("booking_status"));
                vctRow.add(rs.getString("description"));
                vctRow.add(rs.getTimestamp("check_in_date"));
                vctRow.add(rs.getTimestamp("check_out_date"));
                vctRow.add(rs.getTimestamp("created_at"));
                vctData.add(vctRow);
            }
            rs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(vctData, vctHeader);
        listBookingTb.setModel(model);
    }

    private void clearFields() {
        customerNameBox.setSelectedIndex(0);
        emailBox.setSelectedIndex(0);
        roomNumBox.setSelectedIndex(0);
        roomTypeBox.setSelectedIndex(0);
        bookStatusBox.setSelectedIndex(0);
        descriptionTextArea.setText("");
        dateChooserCheckIn.setDate(null);
        dateChooserCheckOut.setDate(null);
        txtRoomStatus.setText("");
        txtRoomPrice.setText("");
    }

    private void addBooking() {
        String customerName = (String) customerNameBox.getSelectedItem();
        String email = (String) emailBox.getSelectedItem();
        String roomNum = (String) roomNumBox.getSelectedItem();
        String roomType = (String) roomTypeBox.getSelectedItem();
        String roomPrice = (String) txtRoomPrice.getText();
        String bookingStatus = (String) bookStatusBox.getSelectedItem();
        String description = descriptionTextArea.getText();

        java.util.Date checkInDate = dateChooserCheckIn.getDate();
        java.util.Date checkOutDate = dateChooserCheckOut.getDate();
        java.sql.Timestamp checkInTimestamp = new java.sql.Timestamp(checkInDate.getTime());
        java.sql.Timestamp checkOutTimestamp = new java.sql.Timestamp(checkOutDate.getTime());

        Con c = new Con();
        try {
            // Kiểm tra trạng thái phòng và lấy room_id
            String checkRoomStatusSql = "SELECT clean_status, id FROM rooms WHERE room_number = ?";
            PreparedStatement checkRoomStatusStmt = c.statement.getConnection().prepareStatement(checkRoomStatusSql);
            checkRoomStatusStmt.setString(1, roomNum);
            ResultSet rs = checkRoomStatusStmt.executeQuery();

            if (rs.next()) {
                String roomStatus = rs.getString("clean_status");
                int roomId = rs.getInt("id"); // Lấy room_id từ bảng rooms
                if (!"Trống".equalsIgnoreCase(roomStatus)) {
                    JOptionPane.showMessageDialog(this, "Phòng này hiện không trống và không thể được đặt.");
                    return;
                }

                // Lấy customer_id từ bảng customers dựa trên email
                String getCustomerIdSql = "SELECT id FROM customers WHERE email = ?";
                PreparedStatement getCustomerIdStmt = c.statement.getConnection().prepareStatement(getCustomerIdSql);
                getCustomerIdStmt.setString(1, email);
                ResultSet rsCustomer = getCustomerIdStmt.executeQuery();
                int customerId = 0;
                if (rsCustomer.next()) {
                    customerId = rsCustomer.getInt("id");
                } else {
                    JOptionPane.showMessageDialog(this, "Khách hàng không tồn tại.");
                    return;
                }

                // Thêm booking mới
                String sqlInsert = "INSERT INTO bookings (customer_id, room_id, customer_name, email, room_num, room_type, room_price, booking_status, description, check_in_date, check_out_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, customerId);
                pstmt.setInt(2, roomId);
                pstmt.setString(3, customerName);
                pstmt.setString(4, email);
                pstmt.setString(5, roomNum);
                pstmt.setString(6, roomType);
                pstmt.setDouble(7, Double.parseDouble(roomPrice));
                pstmt.setString(8, bookingStatus);
                pstmt.setString(9, description);
                pstmt.setTimestamp(10, checkInTimestamp);
                pstmt.setTimestamp(11, checkOutTimestamp);

                pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int bookingId = generatedKeys.getInt(1);

                    // Tính tiền tiện ích
                    double totalAmenitiesPrice = 0;
                    String sqlAmenities = "SELECT a.price, ba.quantity FROM booking_amenities ba JOIN amenities a ON ba.amenity_id = a.amenity_id WHERE ba.booking_id = ?";
                    PreparedStatement pstmtAmenities = c.statement.getConnection().prepareStatement(sqlAmenities);
                    pstmtAmenities.setInt(1, bookingId);
                    ResultSet rsAmenities = pstmtAmenities.executeQuery();
                    while (rsAmenities.next()) {
                        totalAmenitiesPrice += rsAmenities.getDouble("price") * rsAmenities.getInt("quantity");
                    }

                    // Tính tiền dịch vụ
                    double totalServicesPrice = 0;
                    String sqlServices = "SELECT s.service_price FROM booking_services bs JOIN services s ON bs.service_id = s.service_id WHERE bs.booking_id = ?";
                    PreparedStatement pstmtServices = c.statement.getConnection().prepareStatement(sqlServices);
                    pstmtServices.setInt(1, bookingId);
                    ResultSet rsServices = pstmtServices.executeQuery();
                    while (rsServices.next()) {
                        totalServicesPrice += rsServices.getDouble("service_price");
                    }

                    // Tính tổng tiền
                    // tổng tiền phòng là số ngày * giá phòng
                    long diffInMillies = Math.abs(checkOutTimestamp.getTime() - checkInTimestamp.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    double roomPriceValue = Double.parseDouble(roomPrice);
                    double totalRoomPrice = diff * roomPriceValue;
                    double totalAmount = totalRoomPrice + totalAmenitiesPrice + totalServicesPrice;

                    // Cập nhật tổng hóa đơn vào bảng bookings
                    String sqlUpdate = "UPDATE bookings SET total_invoice = ? WHERE booking_id = ?";
                    PreparedStatement pstmtUpdate = c.statement.getConnection().prepareStatement(sqlUpdate);
                    pstmtUpdate.setDouble(1, totalAmount);
                    pstmtUpdate.setInt(2, bookingId);
                    pstmtUpdate.executeUpdate();
                    pstmtUpdate.close();

                    JOptionPane.showMessageDialog(this, "Thêm đặt phòng thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi thêm đặt phòng.");
                }
                pstmt.close();
            } else {
                JOptionPane.showMessageDialog(this, "Phòng không tồn tại.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            listBookingData();
        }
    }



    private void editBooking() {
        String customerName = (String) customerNameBox.getSelectedItem();
        String email = (String) emailBox.getSelectedItem();
        String roomNum = (String) roomNumBox.getSelectedItem();
        String roomType = (String) roomTypeBox.getSelectedItem();
        String roomPrice = txtRoomPrice.getText();
        String bookingStatus = (String) bookStatusBox.getSelectedItem();
        String description = descriptionTextArea.getText();

        java.util.Date checkInDate = dateChooserCheckIn.getDate();
        java.util.Date checkOutDate = dateChooserCheckOut.getDate();
        java.sql.Timestamp checkInTimestamp = new java.sql.Timestamp(checkInDate.getTime());
        java.sql.Timestamp checkOutTimestamp = new java.sql.Timestamp(checkOutDate.getTime());

        Con c = new Con();
        try {
            String sql = "UPDATE bookings SET customer_name = ?, email = ?, room_num = ?, room_type = ?, room_price = ?, booking_status = ?, description = ?, check_in_date = ?, check_out_date = ? WHERE booking_id = ?";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sql);
            pstmt.setString(1, customerName);
            pstmt.setString(2, email);
            pstmt.setString(3, roomNum);
            pstmt.setString(4, roomType);
            pstmt.setString(5, roomPrice);
            pstmt.setString(6, bookingStatus);
            pstmt.setString(7, description);
            pstmt.setTimestamp(8, checkInTimestamp);
            pstmt.setTimestamp(9, checkOutTimestamp);
            pstmt.setInt(10, selectedBookingId);
            JOptionPane.showMessageDialog(this, "Chỉnh sửa đặt phòng thành công!");
            listBookingData();
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        listBookingData();
    }

    private void deleteBooking() {
        if (selectedBookingId == -1) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một đặt phòng để xóa.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa đặt phòng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Con c = new Con();
            try {

                String sqlAmenities = "DELETE FROM booking_amenities WHERE booking_id = ?";
                PreparedStatement pstmtAmenities = c.statement.getConnection().prepareStatement(sqlAmenities);
                pstmtAmenities.setInt(1, selectedBookingId);
                pstmtAmenities.executeUpdate();
                pstmtAmenities.close();


                String sqlServices = "DELETE FROM booking_services WHERE booking_id = ?";
                PreparedStatement pstmtServices = c.statement.getConnection().prepareStatement(sqlServices);
                pstmtServices.setInt(1, selectedBookingId);
                pstmtServices.executeUpdate();
                pstmtServices.close();

                String sqlBookings = "DELETE FROM bookings WHERE booking_id = ?";
                PreparedStatement pstmtBookings = c.statement.getConnection().prepareStatement(sqlBookings);
                pstmtBookings.setInt(1, selectedBookingId);
                pstmtBookings.executeUpdate();
                pstmtBookings.close();



                JOptionPane.showMessageDialog(this, "Xóa đặt phòng thành công!");
                listBookingData();
            } catch (SQLException ex) {
                try {
                    c.statement.getConnection().rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                ex.printStackTrace();
            } finally {
                try {
                    c.statement.getConnection().setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private void searchBooking(String searchText) {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("ID");
        vctHeader.add("Người đặt phòng");
        vctHeader.add("Email");
        vctHeader.add("Số phòng");
        vctHeader.add("Loại phòng");
        vctHeader.add("Giá phòng");
        vctHeader.add("Trạng thái");
        vctHeader.add("Mô tả");
        vctHeader.add("Ngày nhận phòng");
        vctHeader.add("Ngày trả phòng");
        vctHeader.add("Ngày tạo");

        Vector<Vector<Object>> vctData = new Vector<>();

        Con c = new Con();
        try {
            String sql = "SELECT * FROM bookings WHERE customer_name LIKE ? OR email LIKE ? OR room_num LIKE ?";
            PreparedStatement pstmt = c.statement.getConnection().prepareStatement(sql);
            String searchPattern = "%" + searchText + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vector<Object> vctRow = new Vector<>();
                vctRow.add(rs.getInt("booking_id"));  // Thêm id vào dữ liệu hàng
                vctRow.add(rs.getString("customer_name"));
                vctRow.add(rs.getString("email"));
                vctRow.add(rs.getString("room_num"));
                vctRow.add(rs.getString("room_type"));
                vctRow.add(rs.getDouble("room_price"));
                vctRow.add(rs.getString("booking_status"));
                vctRow.add(rs.getString("description"));
                vctRow.add(rs.getTimestamp("check_in_date"));
                vctRow.add(rs.getTimestamp("check_out_date"));
                vctRow.add(rs.getTimestamp("created_at"));
                vctData.add(vctRow);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        DefaultTableModel model = new DefaultTableModel(vctData, vctHeader);
        listBookingTb.setModel(model);
    }


}
