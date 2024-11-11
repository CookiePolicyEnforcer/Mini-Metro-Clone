package base.gameObjects.station;

import base.gameObjects.AbstractGameObject;
import base.gameObjects.Grid;
import base.gameObjects.Passenger;
import base.gameObjects.shape.Shape;
import base.gameObjects.shape.ShapeFactory;
import base.gameObjects.shape.ShapeType;
import base.main.GamePanel;

import java.awt.*;
import java.util.ArrayList;

public class Station extends AbstractGameObject {
    public static final int SIZE = Grid.GRID_SIZE;
    public static final int BORDER_THICKNESS = 5;

    private boolean selected = false;
    private Color selectedColor = Color.RED;
    private final StationExclusionCircle exclusionCircle;
    private final GamePanel gamePanel;
    private ShapeType currentShapeType = ShapeType.CIRCLE;
    private Shape currentShape;
    private boolean shapeChangeHandled = false;  // Allow only one shape change per click
    private final ArrayList<Passenger> passengers;
    private final PassengerSpawner passengerSpawner;

    public Station(int x, int y, GamePanel gamePanel) {
        super(GamePanel.STATION_Z_INDEX, gamePanel);

        this.gamePanel = gamePanel;
        this.x = x;
        this.y = y;
        this.exclusionCircle = new StationExclusionCircle(this, gamePanel);
        this.setPressable(true);
        this.currentShape = ShapeFactory.getShape(currentShapeType);
        this.passengers = new ArrayList<>();
        this.passengerSpawner = new PassengerSpawner(this, gamePanel);
    }

    @Override
    public void update(double deltaTime) {
        // Remove station or change shape if in build mode
        if (gamePanel.isInBuildMode()) {
            if (isRightPressed()) {
                gamePanel.getGrid().removeStation(this);
            }
            if (isLeftPressed() && !selected && !shapeChangeHandled) {
                currentShapeType = currentShapeType.next();
                currentShape = ShapeFactory.getShape(currentShapeType);
                passengers.clear();
                shapeChangeHandled = true;
            }
        }
        // Spawn passengers if not in build mode
        else {
            passengerSpawner.update(deltaTime, passengers);
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw station
        currentShape.draw(
                g2D,
                new Shape.Position(x, y),
                new Shape.Style(SIZE, Color.WHITE),
                new Shape.Border(BORDER_THICKNESS, Color.BLACK),
                new Shape.SelectionState(selected, selectedColor));

        // Draw passengers
        for (Passenger passenger : passengers) {
            passenger.draw(g2D);
        }
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return currentShape.containsPoint(x, y, this.x, this.y, SIZE);
    }

    @Override
    public void setLeftPressed(boolean pressed) {
        super.setLeftPressed(pressed);
        if (!pressed) {
            setSelected(false, null);
            shapeChangeHandled = false;  // Setze zurück wenn Taste losgelassen wird
        }
    }

    public void setSelected(boolean selected, Color selectedColor) {
        this.selected = selected;
        this.selectedColor = selectedColor;
    }

    public void setShapeType(ShapeType shapeType) {
        this.currentShapeType = shapeType;
        this.currentShape = ShapeFactory.getShape(shapeType);
    }

    public StationExclusionCircle getExclusionCircle() {
        return exclusionCircle;
    }

    public ShapeType getCurrentShapeType() {
        return currentShapeType;
    }

    public ArrayList<Passenger> getPassengers() {
        return passengers;
    }
}
