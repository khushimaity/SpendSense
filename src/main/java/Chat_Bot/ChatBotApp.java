package Chat_Bot;

import Connect.DatabaseInitializer;

import javax.swing.*;

public class ChatBotApp {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Spend Sense");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.add(new LoginPanel(frame));
            frame.setVisible(true);
        });
    }
}
