package Common;

import javax.swing.*;
import java.awt.*;

public class LoadingScreen extends JDialog {
    public LoadingScreen(JFrame parent) {
        super(parent, "Loading", true);
        JLabel label = new JLabel("Loading, please wait...");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
        setSize(300, 100);
        setLocationRelativeTo(parent);
    }
}

