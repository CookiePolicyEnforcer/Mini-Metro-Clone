package base.gameObjects;

import base.main.GamePanel;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractGameObject {
    private boolean pressable, leftPressed, rightPressed;
    private final GamePanel gamePanel;
    public int x, y, z;

    public AbstractGameObject(int z, GamePanel gamePanel) {
        this.z = z;
        this.gamePanel = gamePanel;
        pressable = false;
        leftPressed = false;
        rightPressed = false;
    }

    public abstract void update(double deltaTime);

    public abstract void draw(Graphics2D g2D);

    public abstract boolean containsPoint(int x, int y);

    public boolean isPressable() { return pressable; }

    public void setPressable(boolean pressable) { this.pressable = pressable; }

    public boolean isLeftPressed() { return leftPressed; }

    public void setLeftPressed(boolean pressed) {
        this.leftPressed = pressed;
        // Prevent both buttons from being pressed at the same time
        if (this.rightPressed && this.leftPressed) {
            this.rightPressed = false;
        }
    }

    public boolean isRightPressed() { return rightPressed; }

    public void setRightPressed(boolean pressed) {
        this.rightPressed = pressed;
        // Prevent both buttons from being pressed at the same time
        if (this.leftPressed && this.rightPressed) {
            this.leftPressed = false;
        }
    }

    public Point getMousePosition() {
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePosition, gamePanel); // Coordinates relative to gamePanel
        return mousePosition;
    }
}
