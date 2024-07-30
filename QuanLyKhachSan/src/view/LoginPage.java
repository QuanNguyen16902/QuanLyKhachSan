package view;

import Common.Con;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage extends JFrame {
    private JTextField txtUsername;
    private JPanel pnlMain;
    private JButton loginBtn;
    private JButton registerBtn;
    private JLabel lblBgImg;
    private JPasswordField txtPassword;
    private JLabel lblHotel;
    private JPanel pnlBgImg;

    private JButton forgotPassBtn;
    private JLabel lblWelcome;
    private ImageIcon imgIcon;

    public LoginPage() throws IOException {

        setTitle("Hotel Management Panel");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        lblHotel.setBorder(BorderFactory.createEmptyBorder(10,5,0,5));
        // Khởi tạo các thành phần giao diện
        Image originalImage = ImageIO.read(LoginPage.class.getResource("avatar.jpg"));

        Image resizedImage = originalImage.getScaledInstance(350, 250, Image.SCALE_REPLICATE);

        imgIcon = new ImageIcon(resizedImage);

        lblBgImg.setIcon(imgIcon);
        lblBgImg.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        setContentPane(pnlMain);
        setVisible(true);
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Con c = new Con();
                String username = txtUsername.getText();
                String password = txtPassword.getText();


                String validUser = "select * from users where username ='" + username+ "' and password = '"+ password+"'";

                try {
                    ResultSet resultSet = c.statement.executeQuery(validUser);
                    if(resultSet.next()){
                        JOptionPane.showMessageDialog(LoginPage.this, "Đăng nhập thành công");
                        setVisible(false);
                        new CheckRoles(username);
                    }
                    else{
                        if(username.isEmpty()) {
                            JOptionPane.showMessageDialog(LoginPage.this, "Bạn chưa nhập username");
                            txtUsername.requestFocus();
                        }else if(password.isEmpty()) {
                            JOptionPane.showMessageDialog(LoginPage.this, "Bạn chưa nhập password");
                            txtPassword.requestFocus();
                        }else{
                            JOptionPane.showMessageDialog(LoginPage.this, "Sai username hoặc mật khẩu");
                            txtPassword.requestFocus();
                        }
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new RegisterPage();
            }
        });
        forgotPassBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    new ForgotPassword();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }


    public static void main(String[] args) throws IOException {
        new LoginPage();
    }


}
