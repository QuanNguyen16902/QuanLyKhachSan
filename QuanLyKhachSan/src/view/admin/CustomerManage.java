package view.admin;

import Common.Con;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerManage extends JPanel{
    private JTextField txtName;
    private JPanel pnlCustomer;
    private JTextField txtEmail;
    private JPanel pnlField;
    private JPanel pnlBelow;
    private JPanel pnlTitle;
    private JTable listFieldTb;
    private JScrollPane pnlTable;
    private JTextField txtAddress;
    private JComboBox genderBox;
    private JComboBox statusBox;
    private JTextField txtPhone;
    private JTextField txtCCCD;
    private JButton addBtn;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton listBtn;
    private JButton clearButton;
    private JButton searchBtn;
    private JTextField txtSearch;
    private JLabel lblImage;
    private JButton uploadImageButton;
    private JButton delImgBtn;
    private DefaultTableModel tableModel;
    private String imagePath;

    public CustomerManage(){
        setLayout(new BorderLayout());
        pnlTable.setViewportView(listFieldTb);
        add(pnlCustomer);
        String[] statusOptions = {"Active", "Inactive", "Banned"};
        Arrays.stream(statusOptions).forEach(statusBox::addItem);
        String[] genderOptions = {"Nam", "Nữ", "Khác"};
        Arrays.stream(genderOptions).forEach(genderBox::addItem);

        lblImage.setPreferredSize(new Dimension(150, 150));


        listFieldTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = listFieldTb.getSelectedRow();
                    if (selectedRow != -1) { // Nếu có dòng được chọn
                        String customerName = (String) listFieldTb.getValueAt(selectedRow, 0);
                        String address = (String) listFieldTb.getValueAt(selectedRow, 1);
                        String phone = (String) listFieldTb.getValueAt(selectedRow, 2);
                        String email = (String) listFieldTb.getValueAt(selectedRow, 3);
                        String CCCD = (String) listFieldTb.getValueAt(selectedRow, 4);
                        String gender = (String) listFieldTb.getValueAt(selectedRow, 5);
                        String status = (String) listFieldTb.getValueAt(selectedRow, 6);
                        String imgPath = (String) listFieldTb.getValueAt(selectedRow, 7);

                        // Hiển thị thông tin lên các JTextField
                        txtName.setText(customerName);
                        txtEmail.setText(email);
                        txtAddress.setText(address);
                        txtPhone.setText(phone);
                        txtCCCD.setText(CCCD);
                        statusBox.setSelectedItem(status);
                        genderBox.setSelectedItem(gender);
                        setImage(imgPath);
                    }
                }
            }
        });
        listBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listCustomerData();
            }
        });
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCustomer();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 clearFields();
            }
        });
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCustomer();
            }
        });
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editCustomer();
            }
        });

        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchCustomer();
            }
        });
        uploadImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadImage();
            }
        });
        delImgBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtEmail.getText();

                if (email.isEmpty()) {
                    JOptionPane.showMessageDialog(CustomerManage.this, "Vui lòng nhập email khách hàng.");
                    return;
                }

                String sqlSelect = "SELECT image_path FROM customers WHERE email = ?";
                String sqlUpdate = "UPDATE customers SET image_path = NULL WHERE email = ?";

                Con c = new Con();
                try {
                    PreparedStatement pstmtSelect = c.connection.prepareStatement(sqlSelect);
                    pstmtSelect.setString(1, email);
                    ResultSet rs = pstmtSelect.executeQuery();
                    String imageFileName = null;
                    if (rs.next()) {
                        imageFileName = rs.getString("image_path");
                    }
                    rs.close();

                    PreparedStatement pstmtUpdate = c.connection.prepareStatement(sqlUpdate);
                    pstmtUpdate.setString(1, email);
                    int rowsUpdated = pstmtUpdate.executeUpdate();
                    if(imageFileName == null){
                        JOptionPane.showMessageDialog(CustomerManage.this, "Chưa có ảnh!");
                    }else{
                        if (rowsUpdated > 0) {
                            JOptionPane.showMessageDialog(CustomerManage.this, "Xóa ảnh thành công!");
                            setImage("");
                            lblImage.setIcon(null);
                            listCustomerData();
                        }
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CustomerManage.this, "Đã xảy ra lỗi khi xóa ảnh: " + ex.getMessage());
                }
            }
        });
    }

    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imagePath = selectedFile.getAbsolutePath();
            setImage(imagePath);
        }
    }

    private void setImage(String path) {
        if (path != null && !path.isEmpty()) {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(img));
        } else {
            lblImage.setIcon(null);
        }
    }
    private void searchCustomer() {
        String keyword = txtSearch.getText();

        // SQL query
        String sql = "SELECT * FROM customers WHERE full_name LIKE ? OR gender LIKE ? OR account_status LIKE ?";

        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("Tên");
        vctHeader.add("Địa chỉ");
        vctHeader.add("SĐT");
        vctHeader.add("Email");
        vctHeader.add("CCCD");
        vctHeader.add("Giới tính");
        vctHeader.add("Tình trạng");
        vctHeader.add("Ngày đăng ký");

        Vector<Vector<Object>> vctData = new Vector<>();
        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("full_name"));
                row.add(rs.getString("address"));
                row.add(rs.getString("phone_number"));
                row.add(rs.getString("email"));
                row.add(rs.getString("id_card_number"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("account_status"));
                row.add(rs.getTimestamp("registration_date"));
                vctData.add(row);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Set dữ liệu vào table model
        listFieldTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }


    private void listCustomerData() {
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("Tên");
        vctHeader.add("Địa chỉ");
        vctHeader.add("SĐT");
        vctHeader.add("Email");
        vctHeader.add("CCCD");
        vctHeader.add("Giới tính");
        vctHeader.add("Tình trạng");
        vctHeader.add("Ảnh");
        vctHeader.add("Ngày đăng ký");

        Vector<Vector<Object>> vctData = new Vector<>();
        Con c = new Con();
        String sql = "SELECT * FROM customers";
        try {
            ResultSet rs = c.statement.executeQuery(sql);
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("full_name"));
                row.add(rs.getString("address"));
                row.add(rs.getString("phone_number"));
                row.add(rs.getString("email"));
                row.add(rs.getString("id_card_number"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("account_status"));
                row.add(rs.getString("image_path"));
                row.add(rs.getTimestamp("registration_date"));
                vctData.add(row);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Set dữ liệu vào table model
        listFieldTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }
    private boolean isCustomerEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM customers WHERE email = ?";
        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void addCustomer() {
        String fullName = txtName.getText();
        String address = txtAddress.getText();
        String phoneNumber = txtPhone.getText();
        String email = txtEmail.getText();
        String idCardNumber = txtCCCD.getText();
        String gender = (String) genderBox.getSelectedItem();
        String accountStatus = (String) statusBox.getSelectedItem();
        java.sql.Date registrationDate = new java.sql.Date(System.currentTimeMillis());

        // định dạng email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(email);

        // SĐT phải có 10 số
        String phoneRegex = "^\\d{10}$";
        Pattern phonePattern = Pattern.compile(phoneRegex);
        Matcher phoneMatcher = phonePattern.matcher(phoneNumber);

        // CCCD phải có 12 số
        String idCardRegex = "^\\d{12}$";
        Pattern idCardPattern = Pattern.compile(idCardRegex);
        Matcher idCardMatcher = idCardPattern.matcher(idCardNumber);

        // Validate email
        if (!emailMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng.");
            return;
        }

        // Validate SDT
        if (!phoneMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải là 11 số.");
            return;
        }

        // Validate CCCD
        if (!idCardMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "CCCD phải là 12 số.");
            return;
        }

        // Kiểm tra email tồn tại
        if (isCustomerEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "Email đã tồn tại.");
            return;
        }

        String sql = "INSERT INTO customers (full_name, address, phone_number, email, id_card_number, gender, account_status, image_path ,registration_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, address);
            pstmt.setString(3, phoneNumber);
            pstmt.setString(4, email);
            pstmt.setString(5, idCardNumber);
            pstmt.setString(6, gender);
            pstmt.setString(7, accountStatus);
            pstmt.setString(8, imagePath);
            pstmt.setDate(9, registrationDate);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công.", "Success", JOptionPane.INFORMATION_MESSAGE);
            listCustomerData();
            clearFields();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi thêm khách hàng: " + ex.getMessage());
        }
    }

    private void deleteCustomer() {
        String customerName = txtEmail.getText();

        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập email khách hàng.");
            return;
        }

        String sql = "DELETE FROM customers WHERE email = ?";

        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, customerName);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Khách hàng đã được xóa thành công!");
                listCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng với email đã nhập.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi xóa khách hàng: " + ex.getMessage());
        }
    }
    private void editCustomer() {
        String fullName = txtName.getText();
        String address = txtAddress.getText();
        String phoneNumber = txtPhone.getText();
        String email = txtEmail.getText();
        String idCardNumber = txtCCCD.getText();
        String gender = (String) genderBox.getSelectedItem();
        String accountStatus = (String) statusBox.getSelectedItem();

        // định dạng email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(email);

        // SĐT phải có 11 số
        String phoneRegex = "^\\d{10}$";
        Pattern phonePattern = Pattern.compile(phoneRegex);
        Matcher phoneMatcher = phonePattern.matcher(phoneNumber);

        // CCCD phải có 12 số
        String idCardRegex = "^\\d{12}$";
        Pattern idCardPattern = Pattern.compile(idCardRegex);
        Matcher idCardMatcher = idCardPattern.matcher(idCardNumber);

        // Validate email
        if (!emailMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng.");
            return;
        }

        // Validate SDT
        if (!phoneMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải là 10 số.");
            return;
        }

        // Validate CCCD
        if (!idCardMatcher.matches()) {
            JOptionPane.showMessageDialog(this, "CCCD phải là 12 số.");
            return;
        }

        String sql = "UPDATE customers SET full_name = ?, address = ?, phone_number = ?, id_card_number = ?, gender = ?, account_status = ?, image_path = ? WHERE email = ?";

        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, address);
            pstmt.setString(3, phoneNumber);
            pstmt.setString(4, idCardNumber);
            pstmt.setString(5, gender);
            pstmt.setString(6, accountStatus);
            pstmt.setString(7, imagePath);
            pstmt.setString(8, email);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Khách hàng đã được cập nhật thành công!");
                listCustomerData();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng với email đã nhập.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi cập nhật khách hàng: " + ex.getMessage());
        }
    }
    private void clearFields() {
        txtEmail.setText("");
        txtAddress.setText("");
        txtCCCD.setText("");
        txtName.setText("");
        txtPhone.setText("");
        setImage("");
        lblImage.setIcon(null);
        genderBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
    }
}
