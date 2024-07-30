package view.admin;

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
import java.util.Arrays;
import java.util.Vector;

public class UserManage extends JPanel{
    private JPanel pnlAbove;
    private JPanel pnlTitle;
    private JPanel pnlField;
    private JTextField txtFullName;
    private JButton deleteBtn;
    private JButton listBtn;
    private JButton addBtn;
    private JButton editBtn;
    private JButton clearButton;
    private JButton searchBtn;
    private JTextField txtSearch;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JPanel pnlBelow;
    private JScrollPane pnlTable;
    private JTable listUserTb;
    private JComboBox genderBox;
    private JTextField txtUsername;
    private JTextField txtPhone;
    private JTextField txtPassword;
    private JCheckBox roleckBox1;
    private JCheckBox roleCkBox2;
    private JPanel pnlUser;

    public UserManage(){
        setLayout(new BorderLayout());
        add(pnlUser);
        String[] genderOptions = {"Nam", "Nữ", "Khác"};
        Arrays.stream(genderOptions).forEach(genderBox::addItem);

        listUserTb.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = listUserTb.getSelectedRow();
                    if (selectedRow != -1) { // Nếu có dòng được chọn
                        String fullName = (String) listUserTb.getValueAt(selectedRow, 0);
                        String email = (String) listUserTb.getValueAt(selectedRow, 1);
                        String address = (String) listUserTb.getValueAt(selectedRow, 2);
                        String phone = (String) listUserTb.getValueAt(selectedRow, 3);
                        String gender = (String) listUserTb.getValueAt(selectedRow, 4);
                        String username = (String) listUserTb.getValueAt(selectedRow, 5);
                        String password = (String) listUserTb.getValueAt(selectedRow, 6);
                        String roles = (String) listUserTb.getValueAt(selectedRow, 7);

                        // Hiển thị thông tin lên các JTextField
                        txtFullName.setText(fullName);
                        txtEmail.setText(email);
                        txtAddress.setText(address);
                        txtPhone.setText(phone);
                        txtUsername.setText(username);
                        txtPassword.setText(password);
                        genderBox.setSelectedItem(gender);
                        roleckBox1.setSelected(false);
                        roleCkBox2.setSelected(false);
                        if (roles != null && !roles.isEmpty()) {
                            String[] rolesArray = roles.split(", ");
                            for (String role : rolesArray) {
                                if (role.equals("Admin")) {
                                    roleckBox1.setSelected(true);
                                } else if (role.equals("Reception")) {
                                    roleCkBox2.setSelected(true);
                                }
                            }
                        }
                    }
                }
            }
        });
        listBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listUserData();
            }


        });
        addBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUser();
            }
        });
        editBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editUser();
            }
        });
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUser();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchUser();
            }
        });
    }
    private boolean validateInput(String fullName, String email, String address, String phone, String username, String password) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        String phoneRegex = "^\\d{10}$";

        if (fullName.isEmpty() || address.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin.");
            return false;
        }

        if (!email.matches(emailRegex)) {
            JOptionPane.showMessageDialog(this, "Email không đúng định dạng.");
            return false;
        }

        if (!phone.matches(phoneRegex)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải là 10 số.");
            return false;
        }

        return true;
    }

    private void listUserData() {
            Vector<String> vctHeader = new Vector<>();
            vctHeader.add("Tên");
            vctHeader.add("Email");
            vctHeader.add("Địa chỉ");
            vctHeader.add("SĐT");
            vctHeader.add("Giới tính");
            vctHeader.add("Username");
            vctHeader.add("Password");
            vctHeader.add("Roles");
            vctHeader.add("Created_at");

            Vector<Vector<Object>> vctData = new Vector<>();
            Con c = new Con();
            String sql = "SELECT * FROM users";
            try {
                ResultSet rs = c.statement.executeQuery(sql);
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("full_name"));
                    row.add(rs.getString("email"));
                    row.add(rs.getString("address"));
                    row.add(rs.getString("phone"));
                    row.add(rs.getString("gender"));
                    row.add(rs.getString("username"));
                    row.add(rs.getString("password"));
                    row.add(rs.getString("roles"));
                    row.add(rs.getString("created_at"));
                    vctData.add(row);
                }
                rs.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            listUserTb.setModel(new DefaultTableModel(vctData, vctHeader));
        }
    private void addUser() {
        String fullName = txtFullName.getText();
        String email = txtEmail.getText();
        String address = txtAddress.getText();
        String phone = txtPhone.getText();
        String gender = (String) genderBox.getSelectedItem();
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        boolean role1 = roleckBox1.isSelected();
        boolean role2 = roleCkBox2.isSelected();
        String roles = (role1 ? roleckBox1.getText() +", " : "") + (role2 ? roleCkBox2.getText()+ ", " : "");

        if (roles.endsWith(", ")) {
            roles = roles.substring(0, roles.length() - 2);
        }

        if (!validateInput(fullName, email, address, phone, username, password)) {
            return;
        }

        Con c = new Con();

        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try {
            PreparedStatement checkStmt = c.connection.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username đã tồn tại. Vui lòng chọn username khác.");
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi kiểm tra username: " + ex.getMessage());
            return;
        }

        String sql = "INSERT INTO users (full_name, email, address, phone, gender, roles, username, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            pstmt.setString(5, gender);
            pstmt.setString(6, roles);
            pstmt.setString(7, username);
            pstmt.setString(8, password);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Người dùng đã được thêm thành công!");

                listUserData(); // Refresh the employee data
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi thêm người dùng: " + ex.getMessage());
        }
    }

    private void editUser() {
        String fullName = txtFullName.getText();
        String email = txtEmail.getText();
        String address = txtAddress.getText();
        String phone = txtPhone.getText();
        String gender = (String) genderBox.getSelectedItem();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        boolean role1 = roleckBox1.isSelected();
        boolean role2 = roleCkBox2.isSelected();
        String roles = (role1 ? roleckBox1.getText() +", " : "") + (role2 ? roleCkBox2.getText()+ ", " : "");

        if (roles.endsWith(", ")) {
            roles = roles.substring(0, roles.length() - 2);
        }
        if (!validateInput(fullName, email, address, phone, username, password)) {
            return;
        }

        String sql = "UPDATE users SET full_name = ?, email = ?, address = ?, phone = ?, gender = ?, roles = ?, password = ? WHERE username = ?";

        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            pstmt.setString(5, gender);
            pstmt.setString(6, roles);
            pstmt.setString(7, password);
            pstmt.setString(8, username);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Nhân viên đã được cập nhật thành công!");
                listUserData(); // Refresh the employee data
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi cập nhật người dùng: " + ex.getMessage());
        }
    }
    private void deleteUser() {
        String username = txtUsername.getText();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập username người dùng.");
            return;
        }
        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa người dùng này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        String sql = "DELETE FROM users WHERE username = ?";
        Con c = new Con();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            pstmt.setString(1, username);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Khách hàng đã được xóa thành công!");
                listUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng với username đã nhập.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi xóa người dùng: " + ex.getMessage());
        }
    }
    private void searchUser() {
        String searchKeyword = txtSearch.getText().trim();
        if (searchKeyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa tìm kiếm.");
            return;
        }
        String sql = "SELECT * FROM users WHERE username LIKE ? OR full_name LIKE ? OR email LIKE ? OR phone LIKE ?";

        Con c = new Con();
        Vector<String> vctHeader = new Vector<>();
        vctHeader.add("Tên");
        vctHeader.add("Email");
        vctHeader.add("Địa chỉ");
        vctHeader.add("SĐT");
        vctHeader.add("Giới tính");
        vctHeader.add("Username");
        vctHeader.add("Password");
        vctHeader.add("Roles");
        vctHeader.add("Created_at");

        Vector<Vector<Object>> vctData = new Vector<>();
        try {
            PreparedStatement pstmt = c.connection.prepareStatement(sql);
            String keyword = "%" + searchKeyword + "%";
            pstmt.setString(1, keyword);
            pstmt.setString(2, keyword);
            pstmt.setString(3, keyword);
            pstmt.setString(4, keyword);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("full_name"));
                row.add(rs.getString("email"));
                row.add(rs.getString("address"));
                row.add(rs.getString("phone"));
                row.add(rs.getString("gender"));
                row.add(rs.getString("username"));
                row.add(rs.getString("password"));
                row.add(rs.getString("roles"));
                row.add(rs.getString("created_at"));
                vctData.add(row);
            }
            rs.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi tìm kiếm người dùng: " + ex.getMessage());
            return;
        }

        // Update table with search results
        listUserTb.setModel(new DefaultTableModel(vctData, vctHeader));
    }


    private void clearFields() {
        txtEmail.setText("");
        txtAddress.setText("");
        txtPassword.setText("");
        txtUsername.setText("");
        txtPhone.setText("");
        txtFullName.setText("");
        genderBox.setSelectedItem(null);
        roleckBox1.setSelected(false);
        roleCkBox2.setSelected(false);
    }
    }
