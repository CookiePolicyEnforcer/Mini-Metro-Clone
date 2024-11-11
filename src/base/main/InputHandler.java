package base.main;

import base.gameObjects.AbstractGameObject;

import javax.swing.*;
import java.awt.event.*;

public class InputHandler implements KeyListener, MouseListener {

    private GamePanel gamePanel;

    public InputHandler(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) { }

    // MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        // Set the object with the highest z-index as pressed (if it's pressable and contains the mouse pointer)
        AbstractGameObject objectWithHighestZIndex = null;
        for (AbstractGameObject interactableGameObject : gamePanel.getInteractableGameObjects()) {
            if (interactableGameObject.isPressable() && interactableGameObject.containsPoint(mouseX, mouseY)) {
                if (objectWithHighestZIndex == null || interactableGameObject.z > objectWithHighestZIndex.z) {
                    objectWithHighestZIndex = interactableGameObject;
                }
            }
        }
        if (objectWithHighestZIndex != null) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                objectWithHighestZIndex.setLeftPressed(true);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                objectWithHighestZIndex.setRightPressed(true);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (AbstractGameObject interactableGameObject : gamePanel.getInteractableGameObjects()) {
            if (interactableGameObject.isPressable() && interactableGameObject.isLeftPressed()) {
                interactableGameObject.setLeftPressed(false);
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}
