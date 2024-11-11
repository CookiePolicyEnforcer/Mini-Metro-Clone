package base.gameObjects;

import base.gameObjects.shape.ShapeFactory;
import base.gameObjects.shape.ShapeType;
import base.gameObjects.shape.Shape;
import base.gameObjects.station.Station;
import base.main.GamePanel;

import java.awt.*;

public class Passenger extends AbstractGameObject {
    public static final int SIZE = (int) (Station.SIZE * 0.4);
    private int size = SIZE;  // Default size

    private final Shape shape;
    private final Color color;
    private boolean isInTrain;

    public Passenger(int x, int y, ShapeType shapeType, GamePanel gamePanel) {
        super(GamePanel.PASSENGER_Z_INDEX, gamePanel);
        this.x = x;
        this.y = y;
        this.shape = ShapeFactory.getShape(shapeType);
        this.color = Color.BLACK; // Schwarze Passagiere an Stationen
        this.isInTrain = false;
    }

    @Override
    public void update(double deltaTime) {

    }

    @Override
    public void draw(Graphics2D g2D) {
        Color drawColor = isInTrain ? Color.WHITE : color;
        shape.draw(
                g2D,
                new Shape.Position(x, y),
                new Shape.Style(size, drawColor),
                new Shape.Border(0, Color.BLACK),
                new Shape.SelectionState(false, null));
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return shape.containsPoint(x, y, this.x, this.y, SIZE);
    }

    public ShapeType getShapeType() {
        return shape.getType();
    }

    public void setInTrain(boolean inTrain) {
        this.isInTrain = inTrain;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
