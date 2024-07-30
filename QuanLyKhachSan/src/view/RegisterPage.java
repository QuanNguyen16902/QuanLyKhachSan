package view;

import Common.Con;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterPage extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtRewritePassword;
    private JLabel lblUsername;
    private JButton registerBtn;
    private JButton backButton;
    private JPanel pnlRegister;
    private JCheckBox receptionCheckBox;
    private JCheckBox adminCheckBox;
    public List<String> getRoles() {
        List<String> roles = new ArrayList<>();
        if (adminCheckBox.isSelected()) {
            roles.add("Admin");
        }
        if (receptionCheckBox.isSelected()) {
            roles.add("Reception");
        }
        return roles;
    }

    public RegisterPage() {
        setContentPane(pnlRegister);
        setSize(500, 350);
        setTitle("Form Đăng Ký");
        setVisible(true);
        pnlRegister.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setVisible(false);
                    new LoginPage();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Con c = new Con();
                String username = txtUsername.getText();
                String password = new String(txtPassword.getPassword());
                String rewritePassword = new String(txtRewritePassword.getPassword());

                List<String> roleList = getRoles();
                String roleString = roleList.toString();
                String roles = Arrays.toString(roleString.split(",")).replaceAll("[\\[\\]]", "");
                System.out.println(roles);
                int newId = getNextUserId(c);

                if (newId == -1) {
                    JOptionPane.showMessageDialog(RegisterPage.this, "Lỗi khi lấy ID người dùng mới");
                    return;
                }

                String sql = "INSERT INTO users (id, username, password, roles) VALUES (" + newId + ", '" + username + "', '" + password + "', '"+ roles+"')";
                String validUser = "SELECT username FROM users WHERE username = '" + username + "'";

                try {
                    ResultSet resultSet = c.statement.executeQuery(validUser);
                    if (!resultSet.next()) {
                        if(username.isEmpty()){
                            JOptionPane.showMessageDialog(RegisterPage.this,"Bạn chưa nhập username");
                            txtUsername.requestFocus();
                        }else if(password.isEmpty()){
                            JOptionPane.showMessageDialog(RegisterPage.this,"Bạn chưa nhập password");
                            txtPassword.requestFocus();
                        }else if(rewritePassword.isEmpty()){
                            JOptionPane.showMessageDialog(RegisterPage.this,"Bạn chưa nhập rewrite password");
                            txtRewritePassword.requestFocus();
                        }else if(!rewritePassword.equals(password)){
                            JOptionPane.showMessageDialog(RegisterPage.this,"Mật khẩu không khớp");
                            txtRewritePassword.requestFocus();
                        }
                        else{
                            int record = c.statement.executeUpdate(sql);
                            if (record > 0) {
                                JOptionPane.showMessageDialog(RegisterPage.this, "Đăng ký thành công " + username);
                                txtPassword.setText(null);
                                txtRewritePassword.setText(null);
                                txtUsername.setText(null);
                            }
                        }

                    } else {
                        JOptionPane.showMessageDialog(RegisterPage.this, "Đã tồn tại username: " + username);
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
    private int getNextUserId(Con c) {
        String sql = "SELECT MAX(id) AS max_id FROM users";
        try {
            ResultSet resultSet = c.statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt("max_id") + 1;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}
