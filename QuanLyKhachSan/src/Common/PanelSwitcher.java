package Common;
import view.admin.*;
import view.reception.*;

import javax.swing.*;
import java.awt.*;

public class PanelSwitcher {
    private final JPanel pnlContent;
    private final JFrame parentFrame;
    //
    public PanelSwitcher(JFrame parentFrame, JPanel pnlContent) {
        this.parentFrame = parentFrame;
        this.pnlContent = pnlContent;
    }

    public void showLoadingScreenAndSwitchPanel(String panelName) {
        LoadingScreen loadingScreen = new LoadingScreen(parentFrame);
        SwingUtilities.invokeLater(() -> loadingScreen.setVisible(true));
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(1000);
                refreshPanel(panelName);
                return null;
            }

            @Override
            protected void done() {
                loadingScreen.dispose();
                pnlContent.setVisible(true);
                CardLayout cl = (CardLayout) pnlContent.getLayout();
                cl.show(pnlContent, panelName);
            }
        };

        worker.execute();
    }

    private void refreshPanel(String panelName) {
        Component existingPanel = getPanelByName(panelName);

        if (existingPanel != null) {
            pnlContent.remove(existingPanel);
        }
        // Tạo JPanel mới và thêm vào pnlContent
        JPanel newPanel = createPanelByName(panelName);
        pnlContent.add(newPanel, panelName);

        // Cập nhật giao diện ng dùng
        pnlContent.revalidate();
        pnlContent.repaint();
    }
    private Component getPanelByName(String panelName) {
        for (Component comp : pnlContent.getComponents()) {
            if (panelName.equals(comp.getName())) {
                return comp;
            }
        }
        return null;
    }

    private JPanel createPanelByName(String panelName) {
        switch (panelName) {
            case "RoomManage":
                return new RoomManage();
            case "CustomerManage":
                return new CustomerManage();
            case "EmployeeManage":
                return new UserManage();
            case "BookingManage":
                return new BookingManage();
            case "AmenityManage":
                return new AmenityManage();
            case "ServiceManage":
                return new ServiceManage();
            case "RevenueManage":
                return new RevenueManage();
            case "RoomMap":
                return new RoomMap();
            case "CheckInPage":
                return new CheckInPage();
            case "CheckOutPage":
                return new CheckOutPage();
            case "ServicePage":
                return new ServicePage();
            case "PaymentPage":
                return new PaymentPage();
            case "CustomerPage":
                return new CustomerPage();
            case "InvoicePage":
                return new InvoicePage();
            default:
                return new JPanel();
        }
    }


}

