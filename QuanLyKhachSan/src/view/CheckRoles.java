package view;

import Common.Con;
import view.admin.Admin;
import view.reception.Reception;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckRoles extends JFrame {
    private JPanel panel1;
    private JComboBox roleBox;
    private JLabel Role;
    private JButton saveBtn;

    public CheckRoles(String username) throws IOException {
        setContentPane(panel1);
        setTitle("Roles");
        setSize(350, 200);
        Con c = new Con();
        String sql = "select roles from users where username = '" + username + "'";
        try {
            ResultSet rs = c.statement.executeQuery(sql);
            if (rs.next()) {
                String roles = rs.getString("roles");
                if (roles != null && !roles.isEmpty()) {
                    // Tách vai trò dựa trên dấu phẩy
                    String[] roleArray = roles.split(",");
                    for (String role : roleArray) {
                        // Thêm từng vai trò vào JComboBox
                        roleBox.addItem(role.trim());
                    }
                    setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Bạn chưa có role", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    setVisible(false);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Người dùng không tồn tại.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        saveBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    if(roleBox.getSelectedItem().toString().equals("Admin")){
                        new Admin(username, roleBox.getSelectedItem().toString());
                        setVisible(false);
                    }else if(roleBox.getSelectedItem().toString().equals("Reception")){
                        new Reception(username, roleBox.getSelectedItem().toString());
                        setVisible(false);
                    }else{
                        JOptionPane.showMessageDialog(CheckRoles.this, "No Response", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    }
                }catch (Exception a){
                    a.printStackTrace();
                }

            }
        });
    }
}
