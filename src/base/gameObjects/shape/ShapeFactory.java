package base.gameObjects.shape;

public class ShapeFactory {
    private static final Shape circleShape = new CircleShape();
    private static final Shape triangleShape = new TriangleShape();
    private static final Shape squareShape = new SquareShape();

    public static Shape getShape(ShapeType type) {
        return switch (type) {
            case CIRCLE -> circleShape;
            case TRIANGLE -> triangleShape;
            case SQUARE -> squareShape;
        };
    }
}