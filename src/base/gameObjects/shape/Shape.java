package base.gameObjects.shape;

import java.awt.*;
import java.awt.geom.GeneralPath;

public interface Shape {
    record Position(int x, int y) {}
    record Style(int size, Color fillColor) {}
    record Border(int borderThickness, Color borderColor) {}
    record SelectionState(boolean selected, Color selectionColor) {}

    default void draw(Graphics2D g2D, Position position, Style style, Border border, SelectionState selectionState) {
        if (selectionState.selected) {
            // Draw outermost shape (selection border)
            g2D.setColor(selectionState.selectionColor);
            g2D.fill(getShapePath(position.x, position.y, style.size + 2 * border.borderThickness));
        }

        // Draw outer shape (border)
        g2D.setColor(border.borderColor);
        g2D.fill(getShapePath(position.x, position.y, style.size));

        // Draw inner shape (fill)
        g2D.setColor(style.fillColor);
        g2D.fill(getShapePath(position.x, position.y, style.size - 2 * border.borderThickness));
    }
    boolean containsPoint(int x, int y, int centerX, int centerY, double size);
    GeneralPath getShapePath(int centerX, int centerY, int size);
    ShapeType getType();
}