package view.admin;

import Common.LoadingScreen;
import Common.PanelSwitcher;
import view.LoginPage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Admin extends JFrame {
    private JButton logoutBtn;
    private JPanel pnlNavbar;
    private JButton roomBtn;
    private JButton cusBtn;
    private JButton bookBtn;
    private JButton mapRoomBtn;
    private JButton amenityBtn;
    private JButton serviceBtn;
    private JButton doanhThuButton;
    private JLabel usernameLbl;
    private JLabel titleLbl;
    private JPanel pnlAdmin;
    private JPanel pnlContent;
    private JLabel logoLbl;

    private ImageIcon imgIcon;
    private PanelSwitcher panelSwitcher;
    public Admin(String username, String role) throws IOException {
        usernameLbl.setText("User: " + username);
        titleLbl.setText("QUẢN LÝ KHÁCH SẠN (" + role +")");

        setContentPane(pnlAdmin);
        setSize(1100, 800);
        setTitle("Dashboard Hotel Management");
        setVisible(true);
        panelSwitcher = new PanelSwitcher(this, pnlContent);

        pnlNavbar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        Image originalImage = ImageIO.read(Admin.class.getResource("logo2.png"));
        Image resizedImage = originalImage.getScaledInstance(200, 200, Image.SCALE_DEFAULT);
        imgIcon = new ImageIcon(resizedImage);

        logoLbl.setIcon(imgIcon);
        logoLbl.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        pnlContent.setVisible(false);
        pnlContent.setLayout(new CardLayout());

        roomBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("RoomManage");
            }
        });
        cusBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("CustomerManage");
            }
        });
        bookBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("EmployeeManage");
            }
        });
        mapRoomBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("BookingManage");
            }
        });
        amenityBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("AmenityManage");
            }
        });
        serviceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("ServiceManage");
            }
        });
        doanhThuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("RevenueManage");
            }
        });
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int ret = JOptionPane.showConfirmDialog(Admin.this, "Bạn có muốn đăng xuất?");
                    if(ret == JOptionPane.YES_NO_OPTION){
                        setVisible(false);
                        new LoginPage();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }


}
