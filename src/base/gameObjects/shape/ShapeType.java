// Shape enum and interface
package base.gameObjects.shape;

public enum ShapeType {
    CIRCLE,
    TRIANGLE,
    SQUARE;

    public ShapeType next() {
        ShapeType[] shapes = values();
        return shapes[(ordinal() + 1) % shapes.length];
    }
}