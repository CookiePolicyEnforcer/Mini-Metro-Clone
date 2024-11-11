package base.gameObjects.shape;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

public class TriangleShape implements Shape {
    @Override
    public void draw(Graphics2D g2D, Position position, Style style, Border border, SelectionState selectionState) {
        // Draw selection border if selected (scaled outward)
        if (selectionState.selected()) {
            GeneralPath selectionTriangle = createScaledTriangle(position, style.size(), -border.borderThickness());
            g2D.setColor(selectionState.selectionColor());
            g2D.fill(selectionTriangle);
        }

        // Draw outer triangle (border, not scaled)
        GeneralPath triangleOuter = createScaledTriangle(position, style.size(), 0);
        g2D.setColor(border.borderColor());
        g2D.fill(triangleOuter);

        // Draw inner triangle (fill, scaled inward)
        GeneralPath triangleInner = createScaledTriangle(position, style.size(), border.borderThickness());
        g2D.setColor(style.fillColor());
        g2D.fill(triangleInner);
    }

    /**
     * Creates a scaled triangle using geometric homothety for uniform border effect.
     * The scaling process:
     * 1. Creates triangle with given size and center point
     * 2. Calculates the inscribed circle's radius (R) using triangle's area and perimeter
     * 3. Determines scaling factor k = (R - borderThickness) / R for homothetic transformation
     * 4. Finds inscribed circle's center (Q) as weighted average of vertices
     * 5. Scales triangle vertices from point Q by factor k to create scaled triangle
     *
     * This ensures the scaled triangle maintains the same shape and has exact borderThickness
     * distance from all edges of the original triangle. A negative borderThickness scales outward,
     * zero means no scaling, and positive scales inward.
     */
    private GeneralPath createScaledTriangle(Position position, int size, double borderThickness) {
        // Calculate triangle points
        double edgeLength = 2.0 * size / Math.sqrt(3);
        double halfSize = size / 2.0;

        Point2D.Double A = new Point2D.Double(position.x(), position.y() - halfSize);  // top
        Point2D.Double B = new Point2D.Double(position.x() + edgeLength/2.0, position.y() + halfSize);  // right
        Point2D.Double C = new Point2D.Double(position.x() - edgeLength/2.0, position.y() + halfSize);  // left

        // If no scaling needed (borderThickness == 0), return triangle directly
        if (borderThickness == 0) {
            return getShapePath(A.x, A.y, B.x, B.y, C.x, C.y);
        }

        // Calculate scaling factor using triangle sides
        double a = B.distance(C);  // side lengths
        double b = C.distance(A);
        double c = A.distance(B);
        double p = (a + b + c) / 2.0;  // semi-perimeter
        double area = Math.sqrt(p * (p-a) * (p-b) * (p-c));  // area (using Heron's formula)
        double R = 2 * area / (a + b + c);  // radius of inscribed circle
        double k = (R - borderThickness) / R;  // scaling factor

        // Calculate incenter point Q (weighted average of vertices)
        double qx = (a*position.x() + b*(B.x) + c*(C.x)) / (a + b + c);
        double qy = (a*(A.y) + b*(B.y) + c*(C.y)) / (a + b + c);
        Point2D.Double Q = new Point2D.Double(qx, qy);

        // Scale points from center Q using factor k
        Point2D.Double A2 = scalePoint(A, Q, k);
        Point2D.Double B2 = scalePoint(B, Q, k);
        Point2D.Double C2 = scalePoint(C, Q, k);

        return getShapePath(A2.x, A2.y, B2.x, B2.y, C2.x, C2.y);
    }

    /**
     * Scales point P from center Q by factor k using homothetic transformation
     */
    private Point2D.Double scalePoint(Point2D.Double P, Point2D.Double Q, double k) {
        return new Point2D.Double(
                Q.x + k * (P.x - Q.x),
                Q.y + k * (P.y - Q.y)
        );
    }

    @Override
    public GeneralPath getShapePath(int centerX, int centerY, int size) {
        double edgeLength = 2.0 * size / Math.sqrt(3);

        int topX = centerX;
        int topY = centerY - size/2;

        int rightX = (int)(centerX + edgeLength/2);
        int rightY = centerY + size/2;

        int leftX = (int)(centerX - edgeLength/2);
        int leftY = centerY + size/2;

        return getShapePath(topX, topY, rightX, rightY, leftX, leftY);
    }

    private GeneralPath getShapePath(double x1, double y1, double x2, double y2, double x3, double y3) {
        GeneralPath triangle = new GeneralPath();
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.closePath();
        return triangle;
    }

    @Override
    public boolean containsPoint(int x, int y, int centerX, int centerY, double size) {
        double h = (Math.sqrt(3) / 2) * size;
        double halfSize = size / 2;

        double topX = centerX;
        double topY = centerY - h / 2;
        double leftX = centerX - halfSize;
        double leftY = centerY + h / 2;
        double rightX = centerX + halfSize;
        double rightY = centerY + h / 2;

        int[] xPoints = {(int) topX, (int) leftX, (int) rightX};
        int[] yPoints = {(int) topY, (int) leftY, (int) rightY};

        Polygon triangle = new Polygon(xPoints, yPoints, 3);
        return triangle.contains(x, y);
    }

    @Override
    public ShapeType getType() {
        return ShapeType.TRIANGLE;
    }
}
