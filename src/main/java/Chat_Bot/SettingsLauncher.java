package Chat_Bot;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class SettingsLauncher {
    public void launchSettings() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("User Settings");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 600);
            frame.add(new UserSettingsPanel()); // ✅ Use default constructor
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
