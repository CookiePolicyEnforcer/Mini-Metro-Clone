package base.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {

    public static void main(String[] args) {
        JFrame window = new JFrame("Mini Metro Clone");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();

        // Move window to the screen where the mouse cursor is located (if you have multiple screens)
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        GraphicsDevice targetScreen = null;
        for (GraphicsDevice screen : screens) {
            GraphicsConfiguration config = screen.getDefaultConfiguration();
            Rectangle bounds = config.getBounds();
            if (bounds.contains(mouseLocation)) {
                targetScreen = screen;
                break;
            }
        }

        if (targetScreen != null) {
            GraphicsConfiguration config = targetScreen.getDefaultConfiguration();
            Rectangle bounds = config.getBounds();

            int x = bounds.x + (bounds.width - window.getWidth()) / 2;
            int y = bounds.y + (bounds.height - window.getHeight()) / 2;

            window.setLocation(x, y);
        } else {
            // Position the window in the center of the screen if no screen was found
            window.setLocationRelativeTo(null);
        }

        window.setVisible(true);

        gamePanel.startGameThread();

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.stopGameThread();
            }
        });
    }
}
