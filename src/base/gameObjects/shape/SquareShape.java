package base.gameObjects.shape;

import java.awt.geom.GeneralPath;

public class SquareShape implements Shape {
    @Override
    public boolean containsPoint(int x, int y, int centerX, int centerY, double size) {
        double halfSize = size / 2;
        return x >= centerX - halfSize && x <= centerX + halfSize &&
                y >= centerY - halfSize && y <= centerY + halfSize;
    }

    @Override
    public GeneralPath getShapePath(int centerX, int centerY, int size) {
        int halfSize = size / 2;
        int topLeftX = centerX - halfSize;
        int topLeftY = centerY - halfSize;

        GeneralPath square = new GeneralPath();
        square.moveTo(topLeftX, topLeftY);
        square.lineTo(topLeftX + size, topLeftY);
        square.lineTo(topLeftX + size, topLeftY + size);
        square.lineTo(topLeftX, topLeftY + size);
        square.closePath();

        return square;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.SQUARE;
    }
}