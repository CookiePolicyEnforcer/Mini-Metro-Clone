package base.util;

public class GeometryUtils {
    /**
     * Rounds an angle to the nearest multiple of a specified value.
     *
     * @param angle The angle to round (in degrees)
     * @param roundTo The value to round to (e.g., 45 for rounding to nearest 45 degrees)
     * @param roundUp If true, rounds up to the next multiple; if false, rounds down
     * @return The rounded angle in degrees
     */
    public static int roundAngleToNearest(int angle, int roundTo, boolean roundUp) {
        int remainder = angle % roundTo;

        if (roundUp) {
            // Round up to the next multiple of roundTo
            return (remainder == 0) ? angle : angle + (roundTo - remainder);
        } else {
            // Round down to the nearest multiple of roundTo
            return angle - remainder;
        }
    }

    /**
     * Mirrors an angle by adding 180 degrees and normalizing to 360 degrees.
     * Used to get the opposite direction of a line.
     *
     * @param angle The angle to mirror (in degrees)
     * @return The mirrored angle in degrees (0-359)
     */
    public static int mirrorAngle(int angle) {
        return (angle + 180) % 360;
    }

    /**
     * Calculates the intersection point of two lines defined by a point and an angle.
     * Handles special cases for vertical lines (angle = 90° or 270°).
     *
     * @param x1 X-coordinate of first line's point
     * @param y1 Y-coordinate of first line's point
     * @param angle1 Angle of first line (in degrees)
     * @param x2 X-coordinate of second line's point
     * @param y2 Y-coordinate of second line's point
     * @param angle2 Angle of second line (in degrees)
     * @return Array containing [x, y] coordinates of intersection point, or null if lines are parallel
     */
    public static double[] intersectLines(double x1, double y1, double angle1,
                                          double x2, double y2, double angle2) {
        // Check if either line is vertical
        boolean isVertical1 = (angle1 % 180 == 90);
        boolean isVertical2 = (angle2 % 180 == 90);

        if (isVertical1 && isVertical2) {
            return null; // Both lines are parallel and vertical
        } else if (isVertical1) {
            // First line is vertical, so intersection x is fixed at x1
            double m2 = Math.tan(Math.toRadians(angle2));
            double b2 = y2 - m2 * x2;
            double intersectionY = m2 * x1 + b2;
            return new double[]{x1, intersectionY};
        } else if (isVertical2) {
            // Second line is vertical, so intersection x is fixed at x2
            double m1 = Math.tan(Math.toRadians(angle1));
            double b1 = y1 - m1 * x1;
            double intersectionY = m1 * x2 + b1;
            return new double[]{x2, intersectionY};
        } else {
            // Calculate slopes for non-vertical lines
            double m1 = Math.tan(Math.toRadians(angle1));
            double m2 = Math.tan(Math.toRadians(angle2));

            // Check if lines are parallel
            if (m1 == m2) {
                return null;
            }

            // Calculate y-intercepts (b = y - mx)
            double b1 = y1 - m1 * x1;
            double b2 = y2 - m2 * x2;

            // Calculate intersection point
            double intersectionX = (b2 - b1) / (m1 - m2);
            double intersectionY = m1 * intersectionX + b1;

            return new double[]{intersectionX, intersectionY};
        }
    }

    /**
     * Calculates the length of a line between two points using the Pythagorean theorem.
     *
     * @param x1 X-coordinate of start point
     * @param y1 Y-coordinate of start point
     * @param x2 X-coordinate of end point
     * @param y2 Y-coordinate of end point
     * @return The length of the line
     */
    public static double getLineLength(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    /**
     * Shortens a line by a specified amount from its end point.
     * Maintains the line's direction while reducing its length.
     *
     * @param startX X-coordinate of start point
     * @param startY Y-coordinate of start point
     * @param endX X-coordinate of end point
     * @param endY Y-coordinate of end point
     * @param shortenBy Amount to shorten the line by
     * @return Array containing [x, y] coordinates of the new endpoint
     */
    public static double[] shortenLine(double startX, double startY, double endX, double endY, double shortenBy) {
        // Calculate the line's current length
        double length = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));

        // Calculate the scaling factor
        double factor = (length - shortenBy) / length;
        factor = Math.max(0, factor); // Prevent negative factor if shortenBy > line length

        // Calculate the new endpoint
        double newX = startX + factor * (endX - startX);
        double newY = startY + factor * (endY - startY);

        return new double[]{newX, newY};
    }
}