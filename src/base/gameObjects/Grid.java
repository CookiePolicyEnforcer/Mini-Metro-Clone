package base.gameObjects;

import base.gameObjects.station.Station;
import base.gameObjects.station.StationExclusionCircle;
import base.main.GamePanel;

import java.awt.*;
import java.util.ArrayList;

public class Grid extends AbstractGameObject {
    public static final int GRID_SIZE = 50;

    private final GamePanel gamePanel;
    private final ArrayList<Station> stationsToRemove;

    public Grid(GamePanel gamePanel) {
        super(GamePanel.GRID_Z_INDEX, gamePanel);
        this.gamePanel = gamePanel;
        this.stationsToRemove = new ArrayList<>();
        this.setPressable(true);
    }

    @Override
    public void update(double deltaTime) {
        // Allow placing stations only in build mode
        if (isLeftPressed() && gamePanel.isInBuildMode()) {
            Point mousePosition = getMousePosition();
            addStation(mousePosition.x, mousePosition.y);
            setLeftPressed(false);
        }

        for (Station station : stationsToRemove) {
            gamePanel.getStations().remove(station);
            gamePanel.getExclusionCircles().remove(station.getExclusionCircle());
        }
        stationsToRemove.clear();
    }

    @Override
    public void draw(Graphics2D g2D) {
        int width = gamePanel.getWidth();
        int height = gamePanel.getHeight();
        g2D.setColor(new Color(200, 200, 200));
        for (int x = 0; x <= width; x += (int) GRID_SIZE) {
            g2D.drawLine(x, 0, x, height);
        }
        for (int y = 0; y <= height; y += (int) GRID_SIZE) {
            g2D.drawLine(0, y, width, y);
        }
    }

    @Override
    public boolean containsPoint(int x, int y) {
        int width = gamePanel.getWidth();
        int height = gamePanel.getHeight();
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }

    public void addStation(int x, int y) {
        int gridX = Math.round((float) x / GRID_SIZE) * GRID_SIZE;
        int gridY = Math.round((float) y / GRID_SIZE) * GRID_SIZE;

        for (StationExclusionCircle exclusionCircle : gamePanel.getExclusionCircles()) {
            if (exclusionCircle.containsPoint(gridX, gridY)) {
                return;
            }
        }

        Station station = new Station(gridX, gridY, gamePanel);
        gamePanel.getStations().add(station);
        gamePanel.getExclusionCircles().add(station.getExclusionCircle());
    }

    /**
     * Removes a station from the grid.
     * @param station The station to remove.
     */
    public void removeStation(Station station) {
        // Marks a station for removal in the next update cycle.
        // Can't be removed immediately because it would cause a ConcurrentModificationException.
        stationsToRemove.add(station);
    }
}