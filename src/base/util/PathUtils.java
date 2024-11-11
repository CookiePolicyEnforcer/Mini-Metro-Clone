package base.util;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

public class PathUtils {
    public static final double DEFAULT_PATH_TOLERANCE = 5.0;
    public static final int DEFAULT_CURVE_STEPS = 10;

    public static class PathPosition {
        public final int segmentIndex;
        public final double distance;

        public PathPosition(int segmentIndex, double distance) {
            this.segmentIndex = segmentIndex;
            this.distance = distance;
        }
    }

    public static double findDistanceOnPath(Path2D path, int targetX, int targetY) {
        PathTraversal traversal = new PathTraversal();
        double[] coords = new double[6];
        PathIterator pi = path.getPathIterator(null);

        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    traversal.handleMoveTo(coords);
                    break;
                case PathIterator.SEG_LINETO:
                    traversal.handleLineTo(coords, targetX, targetY);
                    break;
                case PathIterator.SEG_QUADTO:
                    traversal.handleQuadTo(coords, targetX, targetY);
                    break;
            }
            pi.next();
        }
        return traversal.getResult();
    }

    public static double calculatePathLength(Path2D path) {
        LengthCalculator calculator = new LengthCalculator();
        double[] coords = new double[6];
        PathIterator pi = path.getPathIterator(null);

        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    calculator.processMoveTo(coords);
                    break;
                case PathIterator.SEG_LINETO:
                    calculator.processLineTo(coords);
                    break;
                case PathIterator.SEG_QUADTO:
                    calculator.processQuadTo(coords);
                    break;
            }
            pi.next();
        }
        return calculator.getLength();
    }

    public static double[] calculateQuadraticPoint(double t, double x0, double y0,
                                                   double x1, double y1,
                                                   double x2, double y2) {
        double mt = 1 - t;
        return new double[] {
                mt * mt * x0 + 2 * mt * t * x1 + t * t * x2,
                mt * mt * y0 + 2 * mt * t * y1 + t * t * y2
        };
    }

    public static boolean isPointNearSegment(int px, int py, double x1, double y1, double x2, double y2) {
        return isPointNearSegment(px, py, x1, y1, x2, y2, DEFAULT_PATH_TOLERANCE);
    }

    public static boolean isPointNearSegment(int px, int py, double x1, double y1,
                                             double x2, double y2, double tolerance) {
        double normalLength = Math.hypot(x2-x1, y2-y1);
        if (normalLength == 0) return false;

        double distance = Math.abs((px-x1)*(y2-y1) - (py-y1)*(x2-x1)) / normalLength;
        double dotProduct = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) /
                (normalLength * normalLength);

        return distance < tolerance && dotProduct >= 0 && dotProduct <= 1;
    }

    private static class PathTraversal {
        private double totalDistance = 0;
        private double lastX = 0, lastY = 0;
        private double result = -1;
        private boolean first = true;

        void handleMoveTo(double[] coords) {
            if (first) {
                lastX = coords[0];
                lastY = coords[1];
                first = false;
            }
        }

        void handleLineTo(double[] coords, int targetX, int targetY) {
            double segmentLength = Math.hypot(coords[0] - lastX, coords[1] - lastY);

            if (isPointNearSegment(targetX, targetY, lastX, lastY, coords[0], coords[1])) {
                double ratio = ((targetX - lastX) * (coords[0] - lastX) +
                        (targetY - lastY) * (coords[1] - lastY)) /
                        (segmentLength * segmentLength);
                result = totalDistance + (segmentLength * ratio);
            }

            totalDistance += segmentLength;
            lastX = coords[0];
            lastY = coords[1];
        }

        void handleQuadTo(double[] coords, int targetX, int targetY) {
            double lastQuadX = lastX;
            double lastQuadY = lastY;

            for (int i = 1; i <= DEFAULT_CURVE_STEPS; i++) {
                double t = i / (double)DEFAULT_CURVE_STEPS;
                double[] point = calculateQuadraticPoint(t, lastX, lastY,
                        coords[0], coords[1],
                        coords[2], coords[3]);
                double stepLength = Math.hypot(point[0] - lastQuadX, point[1] - lastQuadY);

                if (isPointNearSegment(targetX, targetY, lastQuadX, lastQuadY,
                        point[0], point[1])) {
                    double ratio = ((targetX - lastQuadX) * (point[0] - lastQuadX) +
                            (targetY - lastQuadY) * (point[1] - lastQuadY)) /
                            (stepLength * stepLength);
                    result = totalDistance + (stepLength * ratio);
                }

                totalDistance += stepLength;
                lastQuadX = point[0];
                lastQuadY = point[1];
            }

            lastX = coords[2];
            lastY = coords[3];
        }

        double getResult() {
            return result;
        }
    }

    private static class LengthCalculator {
        private double length = 0;
        private double lastX = 0, lastY = 0;
        private boolean first = true;

        private void processMoveTo(double[] coords) {
            if (first) {
                lastX = coords[0];
                lastY = coords[1];
                first = false;
            }
        }

        private void processLineTo(double[] coords) {
            length += Math.hypot(coords[0] - lastX, coords[1] - lastY);
            lastX = coords[0];
            lastY = coords[1];
        }

        private void processQuadTo(double[] coords) {
            double lastQuadX = lastX;
            double lastQuadY = lastY;

            for (int i = 1; i <= DEFAULT_CURVE_STEPS; i++) {
                double t = i / (double)DEFAULT_CURVE_STEPS;
                double[] point = calculateQuadraticPoint(t, lastX, lastY,
                        coords[0], coords[1],
                        coords[2], coords[3]);
                length += Math.hypot(point[0] - lastQuadX, point[1] - lastQuadY);
                lastQuadX = point[0];
                lastQuadY = point[1];
            }

            lastX = coords[2];
            lastY = coords[3];
        }

        double getLength() {
            return length;
        }
    }
}