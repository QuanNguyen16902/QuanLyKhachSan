package view.admin;

import Common.Con;
import Common.CustomTableCellRenderer;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Vector;

public class RoomManage extends JPanel {
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JTable listRoomTb;
    private JTextField txtRoomNum;
    private JTextField txtRoomType;
    private JComboBox bedTypeBox;
    private JComboBox statusBox;
    private JTextField txtPrice;
    private JScrollPane pnlTable;
    private JPanel pnlField;
    private JTextField txtSearch;
    private JButton searchBtn;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton listBtn;
    private JPanel pnlRoom;
    private JPanel pnlAbove;
    private JPanel pnlBelow;
    private JButton addBtn;
    private JPanel pnlTitle;
    private JButton clearButton;
    private JSpinner bedNumSpin;
    private JSpinner peoNumSpin;
    private JComboBox sortBox;
    private JPanel pnlCleanDate;
    JDateChooser cleanDateChooser = new JDateChooser();

    public RoomManage() {
        setLayout(new BorderLayout());
        pnlCleanDate.setLayout(new BorderLayout());
        pnlCleanDate.add(cleanDateChooser);

        String[] statusOptions = {"Trống", "Đã đặt", "Đang sử dụng", "Đang dọn dẹp", "Bảo trì"};
        for (String a : statusOptions) {
            statusBox.addItem(a);
        }
        String[] bedTypeOptions = {"Đơn", "Đôi"};
        for (String a : bedTypeOptions) {
            bedTypeBox.addItem(a);
        }

        String[] sortOptions = {"Mã số phòng", "Số gường", "Số người tối đa", "Giá", "Ngày tạo"};
        Arrays.stream(sortOptions).forEach(sortBox::addItem);
        add(pnlRoom);
        listBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listRoomData();
            }
        });

        listRoomTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = listRoomTb.getSelectedRow();
                    if (selectedRow != -1) { // Nếu có dòng được chọn
                        String roomNumber = (String) listRoomTb.getValueAt(selectedRow, 0);
                        String roomType = (String) listRoomTb.getValueAt(selectedRow, 1);
                        String bedType = (String) listRoomTb.getValueAt(selectedRow, 2);
                        Integer numberOfBed = (Integer) listRoomTb.getValueAt(selectedRow, 3);
                        Integer maxOccupied = (Integer) listRoomTb.getValueAt(selectedRow, 4);
                        String status = (String) listRoomTb.getValueAt(selectedRow, 5);
                        String price = (String) listRoomTb.getValueAt(selectedRow, 6);
                        Timestamp lastCleanedDate = (Timestamp) listRoomTb.getValueAt(selectedRow, 7);
//                        Date createdDate = (Date) listRoomTb.getValueAt(selectedRow, 8);

                        // Hiển thị thông tin lên các JTextField
                        txtRoomNum.setText(roomNumber);
                        txtRoomType.setText(roomType);
                        txtPrice.setText(price);
                        statusBox.setSelectedItem(status);
                        bedTypeBox.setSelectedItem(bedType);
                        bedNumSpin.setValue(numberOfBed);
                        peoNumSpin.setValue(maxOccupied);
                        cleanDateChooser.setDate(lastCleanedDate);
                    }
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtPrice.setText("");
                txtRoomNum.setText("");
                txtRoomType.setText("");
                txtSearch.setText("");
                bedTypeBox.setSelectedIndex(0);
                statusBox.setSelectedIndex(0);
//                txtCleanDate.setText("");
                bedNumSpin.setValue(0);
                peoNumSpin.setValue(0);
                cleanDateChooser.setDate(null);
            }
        });
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRoom();
            }
        });


        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRoom(txtRoomNum.getText());
            }
        });
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editRoom();
            }
        });
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchRoom();
            }
        });
        sortBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sortRoomData();
            }
        });
    }

    private void addRoom() {
        try {
            Con c = new Con();
            String roomNum = txtRoomNum.getText().trim();
            String roomType = txtRoomType.getText().trim();
            String bedType = bedTypeBox.getSelectedItem() != null ? bedTypeBox.getSelectedItem().toString() : "";
            String statusRoom = statusBox.getSelectedItem() != null ? statusBox.getSelectedItem().toString() : "";
            String newPrice = txtPrice.getText().trim();
            Integer numOfBed = (Integer) bedNumSpin.getValue();
            Integer maxOccupied = (Integer) peoNumSpin.getValue();
            java.util.Date cleanDate = cleanDateChooser.getDate();

            // Kiểm tra các trường không được để trống
            if (roomNum.isEmpty() || roomType.isEmpty() || bedType.isEmpty() || statusRoom.isEmpty() || newPrice.isEmpty() || cleanDate == null || numOfBed == null || maxOccupied == null) {
                JOptionPane.showMessageDialog(null, "Vui lòng điền đầy đủ tất cả các trường.");
                return;
            }
            if (numOfBed <= 0) {
                JOptionPane.showMessageDialog(null, "Số giường lớn hơn 0.");
                return;
            }
            if (maxOccupied <= 0) {
                JOptionPane.showMessageDialog(null, "Số người lớn hơn 0.");
                return;
            }
            java.sql.Timestamp cleanDateTimestamp = new java.sql.Timestamp(cleanDate.getTime());
            String checkSql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
            PreparedStatement pstmt = c.connection.prepareStatement(checkSql);
            pstmt.setString(1, roomNum);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "Mã phòng đã tồn tại. Hãy dùng mã khác.");
                return;
            }

            String insertSql = "INSERT INTO rooms (room_number, room_type, bed_type, number_of_bed, max_occupied, clean_status, price, last_cleaned_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = c.connection.prepareStatement(insertSql);
            pstmt.setString(1, roomNum);
            pstmt.setString(2, roomType);
            pstmt.setString(3, bedType);
            pstmt.setInt(4, numOfBed);
            pstmt.setInt(5, maxOccupied);
            pstmt.setString(6, statusRoom);
            pstmt.setString(7, newPrice);
            pstmt.setTimestamp(8, cleanDateTimestamp);
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Thêm phòng thành công.");
                listRoomData();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage());
        }
    }

    private void listRoomData() {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("Mã số phòng");
        vctHeader.add("Loại phòng");
        vctHeader.add("Loại giường");
        vctHeader.add("Số giường");
        vctHeader.add("Người tối đa");
        vctHeader.add("Tình trạng");
        vctHeader.add("Giá");

        vctHeader.add("Lần dọn gần nhất");
        vctHeader.add("Ngày tạo");

        Vector<Vector<Object>> vctData = new Vector<>();
        Con c = new Con();
        String sql = "SELECT * FROM rooms";
        try {
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("room_number"));
                row.add(rs.getString("room_type"));
                row.add(rs.getString("bed_type"));
                row.add(rs.getInt("number_of_bed"));
                row.add(rs.getInt("max_occupied"));
                row.add(rs.getString("clean_status"));
                row.add(rs.getString("price"));
                row.add(rs.getTimestamp("last_cleaned_date"));
                row.add(rs.getTimestamp("created_at"));
                vctData.add(row);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Set dữ liệu vào table model
        listRoomTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }

    private void editRoom() {
        try {
            String roomNum = txtRoomNum.getText();
            String roomType = txtRoomType.getText();
            String bedType = bedTypeBox.getSelectedItem().toString();
            String statusRoom = statusBox.getSelectedItem().toString();
            String newPrice = txtPrice.getText();
            Integer numOfBed = (Integer) bedNumSpin.getValue();
            Integer maxOccupied = (Integer) peoNumSpin.getValue();
            java.util.Date cleanDate = cleanDateChooser.getDate();
            java.sql.Timestamp cleanDateTimestamp = new java.sql.Timestamp(cleanDate.getTime());

            Con c = new Con();
            // Kiểm tra xem phòng có tồn tại không
            String checkSql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
            PreparedStatement pstmt = c.connection.prepareStatement(checkSql);
            pstmt.setString(1, roomNum);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                JOptionPane.showMessageDialog(null, "Mã phòng không tồn tại.");
                return;
            }
            if (numOfBed <= 0) {
                JOptionPane.showMessageDialog(null, "Số giường lớn hơn 0.");
                return;
            }
            if (maxOccupied <= 0) {
                JOptionPane.showMessageDialog(null, "Số người lớn hơn 0.");
                return;
            }

            // Cập nhật thông tin phòng
            String updateSql = "UPDATE rooms SET room_type = ?, bed_type = ?, number_of_bed = ?, max_occupied = ?, clean_status = ?, price = ?, last_cleaned_date = ? WHERE room_number = ?";
            pstmt = c.connection.prepareStatement(updateSql);
            pstmt.setString(1, roomType);
            pstmt.setString(2, bedType);
            pstmt.setInt(3, numOfBed);
            pstmt.setInt(4, maxOccupied);
            pstmt.setString(5, statusRoom);
            pstmt.setString(6, newPrice);
            pstmt.setTimestamp(7, cleanDateTimestamp);
            pstmt.setString(8, roomNum);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Cập nhật phòng thành công.");
                listRoomData(); // Cập nhật bảng phòng sau khi cập nhật thành công
            } else {
                JOptionPane.showMessageDialog(null, "Lỗi cập nhật phòng.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage());
        }
    }

    private void deleteRoom(String roomNum) {
        try {
            Con c = new Con();
            int confirm = JOptionPane.showConfirmDialog(null, "Bạn có muốn xóa phòng?", "Xác nhận xóa phòng", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.NO_OPTION) {
                return;
            }

            // Kiểm tra trạng thái phòng
            String checkStatusSql = "SELECT clean_status, id FROM rooms WHERE room_number = ?";
            PreparedStatement pstmt = c.connection.prepareStatement(checkStatusSql);
            pstmt.setString(1, roomNum);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String statusRoom = rs.getString("clean_status");
                int roomId = rs.getInt("id");

                if ("Đã đặt".equalsIgnoreCase(statusRoom)) {
                    JOptionPane.showMessageDialog(null, "Phòng không thể xóa vì có khách đã đặt.");
                    return;
                } else if ("Đang sử dụng".equalsIgnoreCase(statusRoom)) {
                    JOptionPane.showMessageDialog(null, "Phòng không thể xóa vì có khách đang sử dụng.");
                    return;
                }

                // Xóa tất cả các tiện nghi liên quan đến các đặt phòng của phòng này
                String deleteBookingAmenitiesSql = "DELETE FROM booking_amenities WHERE booking_id IN (SELECT booking_id FROM bookings WHERE room_id = ?)";
                pstmt = c.connection.prepareStatement(deleteBookingAmenitiesSql);
                pstmt.setInt(1, roomId);
                pstmt.executeUpdate();

                // Xóa tất cả các đặt phòng liên quan đến phòng này
                String deleteBookingsSql = "DELETE FROM bookings WHERE room_num = ?";
                pstmt = c.connection.prepareStatement(deleteBookingsSql);
                pstmt.setString(1, roomNum);
                pstmt.executeUpdate();

                // Sau khi xóa các đặt phòng liên quan, tiến hành xóa phòng
                String deleteRoomSql = "DELETE FROM rooms WHERE room_number = ?";
                pstmt = c.connection.prepareStatement(deleteRoomSql);
                pstmt.setString(1, roomNum);

                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(null, "Xóa phòng thành công.");
                    listRoomData(); // Gọi phương thức cập nhật bảng sau khi xóa phòng
                } else {
                    JOptionPane.showMessageDialog(null, "Lỗi xóa phòng.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Không tìm thấy mã số phòng.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage());
        }
    }


    private void searchRoom() {
        try {
            Con c = new Con();
            String searchText = txtSearch.getText().trim();
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM rooms WHERE 1=1");

            if (!searchText.isEmpty()) {
                sqlBuilder.append(" AND (room_number LIKE ? OR bed_type LIKE ? OR room_type LIKE ? OR clean_status LIKE ? OR price LIKE ?)");
            }

            String sql = sqlBuilder.toString();
            PreparedStatement pstmt = c.connection.prepareStatement(sql);

            // Gán giá trị cho các tham số nếu có
            int index = 1;
            if (!searchText.isEmpty()) {
                pstmt.setString(index++, "%" + searchText + "%"); // Mã số phòng
                pstmt.setString(index++, "%" + searchText + "%"); // Loại giường
                pstmt.setString(index++, "%" + searchText + "%"); // Loại phòng
                pstmt.setString(index++, "%" + searchText + "%"); // Tình trạng phòng
                pstmt.setString(index++, "%" + searchText + "%"); // Giá phòng
            }
            ResultSet rs = pstmt.executeQuery();
            Vector<String> vctHeader = new Vector<>();
            vctHeader.add("Mã số phòng");
            vctHeader.add("Loại phòng");
            vctHeader.add("Loại giường");
            vctHeader.add("Số giường");
            vctHeader.add("Người tối đa");
            vctHeader.add("Tình trạng");
            vctHeader.add("Giá");
            vctHeader.add("Lần dọn gần nhất");
            vctHeader.add("Ngày tạo");
            Vector<Vector<Object>> vctData = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("room_number"));
                row.add(rs.getString("room_type"));
                row.add(rs.getString("bed_type"));
                row.add(rs.getInt("number_of_bed"));
                row.add(rs.getInt("max_occupied"));
                row.add(rs.getString("clean_status"));
                row.add(rs.getString("price"));
                row.add(rs.getTimestamp("last_cleaned_date"));
                row.add(rs.getTimestamp("created_at"));
                vctData.add(row);
            }

            // Cập nhật bảng
            listRoomTb.setModel(new DefaultTableModel(vctData, vctHeader));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage());
        }
    }

    private void sortRoomData() {
        try {
            Con c = new Con();
            String selectedSortOption = (String) sortBox.getSelectedItem();
            String sortColumn = "";
            switch (selectedSortOption) {
                case "Mã số phòng":
                    sortColumn = "room_number";
                    break;
                case "Số gường":
                    sortColumn = "number_of_bed";
                    break;
                case "Số người tối đa":
                    sortColumn = "max_occupied";
                    break;
                case "Giá":
                    sortColumn = "price";
                    break;
                case "Ngày tạo":
                    sortColumn = "created_at";
                    break;
            }

            String sql = "SELECT * FROM rooms ORDER BY " + sortColumn;
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            Vector<String> vctHeader = new Vector<>();
            vctHeader.add("Mã số phòng");
            vctHeader.add("Loại phòng");
            vctHeader.add("Loại giường");
            vctHeader.add("Số giường");
            vctHeader.add("Người tối đa");
            vctHeader.add("Tình trạng");
            vctHeader.add("Giá");
            vctHeader.add("Lần dọn gần nhất");
            vctHeader.add("Ngày tạo");

            Vector<Vector<Object>> vctData = new Vector<>();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("room_number"));
                row.add(rs.getString("room_type"));
                row.add(rs.getString("bed_type"));
                row.add(rs.getInt("number_of_bed"));
                row.add(rs.getInt("max_occupied"));
                row.add(rs.getString("clean_status"));
                row.add(rs.getString("price"));
                row.add(rs.getTimestamp("last_cleaned_date"));
                row.add(rs.getTimestamp("created_at"));
                vctData.add(row);
            }

            // Cập nhật bảng
            listRoomTb.setModel(new DefaultTableModel(vctData, vctHeader));

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi: " + e.getMessage());
        }
    }

}
