package base.gameObjects.ui;

import base.gameObjects.AbstractGameObject;
import base.main.GamePanel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;


public class ModeToggle extends AbstractGameObject {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 50;
    private static final int MARGIN = 20;
    private static final int PADDING = 5;
    private static final Color BUILD_COLOR = new Color(121, 189, 154);
    private static final Color PLAY_COLOR = new Color(45, 45, 45);

    private boolean isInBuildMode = false;

    public ModeToggle(GamePanel gamePanel) {
        super(GamePanel.UI_Z_INDEX, gamePanel);
        this.x = MARGIN;
        this.y = 600 - HEIGHT - MARGIN;
        this.setPressable(true);
    }

    @Override
    public void update(double deltaTime) {
        if (isLeftPressed()) {
            isInBuildMode = !isInBuildMode;
            setLeftPressed(false);
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D background = new RoundRectangle2D.Double(
                x, y, WIDTH, HEIGHT, HEIGHT, HEIGHT
        );
        g2D.setColor(isInBuildMode ? BUILD_COLOR : PLAY_COLOR);
        g2D.fill(background);

        int circleSize = HEIGHT - 2 * PADDING;
        int circleX = isInBuildMode ?
                x + WIDTH - circleSize - PADDING :
                x + PADDING;
        int circleY = y + PADDING;

        g2D.setColor(Color.WHITE);
        g2D.fillOval(circleX, circleY, circleSize, circleSize);
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return x >= this.x && x <= this.x + WIDTH &&
                y >= this.y && y <= this.y + HEIGHT;
    }

    public boolean isInBuildMode() {
        return isInBuildMode;
    }
}