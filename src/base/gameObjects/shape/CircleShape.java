package base.gameObjects.shape;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class CircleShape implements Shape {
    @Override
    public boolean containsPoint(int x, int y, int centerX, int centerY, double size) {
        int dx = x - centerX;
        int dy = y - centerY;
        int distance = (int) Math.hypot(dx, dy);
        return distance <= size / 2;
    }

    @Override
    public GeneralPath getShapePath(int centerX, int centerY, int size) {
        int radius = size / 2;

        GeneralPath circle = new GeneralPath();
        circle.append(new Ellipse2D.Double(
                centerX - radius,
                centerY - radius,
                size,
                size
        ), false);

        return circle;
    }

    @Override
    public ShapeType getType() {
        return ShapeType.CIRCLE;
    }
}