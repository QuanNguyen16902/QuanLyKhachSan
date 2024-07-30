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

public class ForgotPassword extends JFrame{
    private JTextField txtUsername;
    private JPanel pnlForgotPassword;
    private JLabel lblForgot;
    private JButton confirmBtn;
    private JPasswordField txtPass;
    private JPasswordField txtRewritePass;
    private ImageIcon imgIcon;

    public ForgotPassword() throws IOException {
        setContentPane(pnlForgotPassword);
        setSize(400,300);
        setTitle("Forgot Password");

        Image originalImage = ImageIO.read(ForgotPassword.class.getResource("forgot-password.png"));

        Image resizedImage = originalImage.getScaledInstance(200, 150, Image.SCALE_REPLICATE);

        imgIcon = new ImageIcon(resizedImage);

        lblForgot.setIcon(imgIcon);

        setVisible(true);
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Con c = new Con();
                String username = txtUsername.getText();
                String password = new String(txtPass.getPassword());
                String rewritePassword = new String(txtRewritePass.getPassword());

                String sql = "select username from users where username = '"+ username+"'";
                String updateQuery = "Update users set password = '"+ password+"' where username = '"+ username +"'";
                try {
                    ResultSet rs = c.statement.executeQuery(sql);
                        if(username.isEmpty()){
                            JOptionPane.showMessageDialog(ForgotPassword.this,"Bạn chưa nhập username");
                            txtUsername.requestFocus();
                        }else if(password.isEmpty()){
                            JOptionPane.showMessageDialog(ForgotPassword.this,"Bạn chưa nhập password");
                            txtPass.requestFocus();
                        }else if(rewritePassword.isEmpty()){
                            JOptionPane.showMessageDialog(ForgotPassword.this,"Bạn chưa nhập rewrite password");
                            txtRewritePass.requestFocus();
                        }else if(!rewritePassword.equals(password)){
                            JOptionPane.showMessageDialog(ForgotPassword.this,"Mật khẩu không khớp");
                            txtRewritePass.requestFocus();
                        }else{
                            if(!rs.next()){
                                JOptionPane.showMessageDialog(ForgotPassword.this, "Không có username là: " + username);
                            }
                            else{
                                int record = c.statement.executeUpdate(updateQuery);
                                if(record > 0){
                                    JOptionPane.showMessageDialog(ForgotPassword.this, "Đổi mật khẩu thành công");
                                    setVisible(false);
                                }else{
                                    JOptionPane.showMessageDialog(ForgotPassword.this, "Đổi mật khẩu thất bại");
                                }
                            }

                        }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
    }
}
