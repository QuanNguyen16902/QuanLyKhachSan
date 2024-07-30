package view.reception;

import Common.PanelSwitcher;
import view.LoginPage;
import view.admin.Admin;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class Reception extends JFrame{
    private JPanel pnlReception;
    private JButton logoutBtn;
    private JButton btnBasicInfor;
    private JPanel pnlNavbar;
    private JButton roomMapBtn;
    private JButton checkInButton;
    private JButton checkOutButton;
    private JButton serviceBtn;
    private JButton paymentBtn;
    private JButton inforBtn;
    private JLabel usernameLbl;
    private JLabel TitleLbl;
    private JLabel imgRoom;
    private JLabel lblImage;
    private JPanel pnlContent;
    private JButton invoiceBtn;
    private JMenuBar jMenu;
    private ImageIcon imgIcon;
    public PanelSwitcher panelSwitcher;
    public Reception(String username, String role) {
        usernameLbl.setText("User: " + username);
        TitleLbl.setText("QUẢN LÝ KHÁCH SẠN (" + role +")");
        Image originalImage = null;
        try {
            originalImage = ImageIO.read(Admin.class.getResource("logo2.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Image resizedImage = originalImage.getScaledInstance(200, 200, Image.SCALE_DEFAULT);
        imgIcon = new ImageIcon(resizedImage);

        lblImage.setIcon(imgIcon);
        lblImage.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        setContentPane(pnlReception);
        setSize(1100, 700);
        setTitle("Dashboard Hotel Management");
        setVisible(true);
        pnlContent.setVisible(false);
        pnlContent.setLayout(new CardLayout());
       panelSwitcher = new PanelSwitcher(this, pnlContent);

        pnlNavbar.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        logoutBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int ret = JOptionPane.showConfirmDialog(Reception.this, "Bạn có muốn đăng xuất?");
                    if(ret == JOptionPane.YES_NO_OPTION){
                        setVisible(false);
                        new LoginPage();
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        roomMapBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("RoomMap");
            }
        });
        checkInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("CheckInPage");
            }
        });
        checkOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("CheckOutPage");
            }
        });
        serviceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("ServicePage");
            }
        });
        paymentBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("PaymentPage");
            }
        });
        inforBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("CustomerPage");
            }
        });
        invoiceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panelSwitcher.showLoadingScreenAndSwitchPanel("InvoicePage");
            }
        });
    }

}
