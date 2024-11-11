package base.gameObjects.station;

import base.gameObjects.AbstractGameObject;
import base.gameObjects.Grid;
import base.main.GamePanel;

import java.awt.*;

public class StationExclusionCircle extends AbstractGameObject {
    public static final double EXCLUSION_CIRCLE_RADIUS_IN_GRID_CELLS = 2.2;

    private Station station;
    private final int size;

    public StationExclusionCircle(Station station, GamePanel gamePanel) {
        super(GamePanel.STATION_EXCLUSION_CIRCLE_Z_INDEX, gamePanel);
        this.station = station;
        this.size = (int) (EXCLUSION_CIRCLE_RADIUS_IN_GRID_CELLS * 2 * Grid.GRID_SIZE);
        this.x = station.x - size / 2;
        this.y = station.y - size / 2;
    }

    @Override
    public void update(double deltaTime) {
    }

    @Override
    public void draw(Graphics2D g2D) {
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setColor(GamePanel.BACKGROUND_COLOR);
        g2D.fillOval(x, y, size, size);
    }

    @Override
    public boolean containsPoint(int x, int y) {
        int centerX = this.x + size / 2;
        int centerY = this.y + size / 2;
        int distance = (int) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        return distance <= size / 2;
    }
}
