package base.gameObjects.trainline;

import base.gameObjects.AbstractGameObject;
import base.gameObjects.station.Station;
import base.main.GamePanel;
import base.util.GeometryUtils;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class TrainLineSegment extends AbstractGameObject {
    private Station startStation, endStation;
    private final TrainLine trainLine;
    private final GamePanel gamePanel;
    private double startX, startY;
    private double endX, endY;
    private double bendX, bendY;
    private double previousAngle1 = -1;
    public boolean hasBend, bendOrientationClockwise = false;
    private boolean hasLeftStation = false;

    /**
     * Create a new TrainLineSegment with a start station and an end station.
     * @param startStation the start station
     * @param endStation the end station
     * @param trainLine the train line that this segment is part of
     * @param gamePanel the game panel
     */
    public TrainLineSegment(Station startStation, Station endStation, TrainLine trainLine, GamePanel gamePanel) {
        super(GamePanel.TRAIN_LINE_Z_INDEX, gamePanel);

        this.trainLine = trainLine;
        this.gamePanel = gamePanel;
        setStartStation(startStation);
        setEndStation(endStation);
    }

    /**
     * Create a new TrainLineSegment with no end station. Used to create a selector segment,
     * a segment that follows the mouse and is used to select new stations for the train line (`trainLine`).
     * @param startStation the station where the selector starts
     * @param trainLine the train line that the selector is used for
     * @param gamePanel the game panel
     */
    public TrainLineSegment(Station startStation, TrainLine trainLine, GamePanel gamePanel) {
        this(startStation, null, trainLine, gamePanel);
    }

    @Override
    public void update(double deltaTime) {
        // If this segment is a station selector (has no end station): set end coordinates to mouse position
        if (endStation == null) {
            Point mousePosition = getMousePosition();
            setEndPoint(mousePosition.getX(), mousePosition.getY());
            // Check if mouse touches a station
            ArrayList<Station> stations = gamePanel.getStations();
            boolean isTouchingStation = false;
            for (Station station : stations) {
                if (station.containsPoint((int) mousePosition.getX(), (int) mousePosition.getY())) {
                    isTouchingStation = true;
                    if (hasLeftStation) {
                        // If touched station is not part of this line -> add
                        if (station != startStation && !trainLine.getStations().contains(station)) {
                            trainLine.addStationWithSelector(station);
                        }
                        // If touched station is the first station of this line -> add (allow ring lines)
                        else if (station != startStation && station == trainLine.getFirstStation()) {
                            trainLine.addStationWithSelector(station);
                            trainLine.setLeftPressed(false);
                        }
                        // If touched station is part of this line, and it's not the first station -> remove
                        else if (station == startStation && trainLine.getStations().size() > 1) {
                            trainLine.removeStationWithSelector(station);
                        }
                        hasLeftStation = false;
                    }
                    break;
                }
            }
            if (!isTouchingStation) {
                hasLeftStation = true;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        g2D.setStroke(new BasicStroke(TrainLine.LINE_THICKNESS, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2D.setColor(trainLine.getColor());
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D path = new GeneralPath();
        path.moveTo(startX, startY);

        if (hasBend) {
            // Draw line with rounded bend: (start -> bendStart) + (bendStart -> bendEnd) + (bendEnd -> end)

            double[] bendStart = GeometryUtils.shortenLine(startX, startY, bendX, bendY, (double) TrainLine.BEND_LENGTH / 2);
            double[] bendEnd = GeometryUtils.shortenLine(endX, endY, bendX, bendY, (double) TrainLine.BEND_LENGTH / 2);

            // Draw first segment (straight line): start -> bendStart
            path.lineTo(bendStart[0], bendStart[1]);

            // Third segment not required if the bend is too close to the end point (line ends with curve)
            boolean thirdSegmentNotRequired = Math.abs(endY - startY) + (double) TrainLine.LINE_THICKNESS /2 < (double) TrainLine.BEND_LENGTH / 2;

            if (thirdSegmentNotRequired){
                // Draw last segment (rounded corner): bendStart -> end
                path.quadTo(bendX, bendY, endX, endY);
            } else {
                // Draw second segment (rounded corner): bendStart -> bendEnd
                path.quadTo(bendX, bendY, bendEnd[0], bendEnd[1]);
                // Draw third segment (straight line): bendEnd -> end
                path.lineTo(endX, endY);
            }
        } else {
            // Draw single straight line without bend: start -> end
            path.lineTo(endX, endY);
        }

        // Draw the path
        g2D.draw(path);
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return false;
    }

    public void setEndPoint(double x, double y) {
        this.endX = x;
        this.endY = y;
        calculateBend();
    }

    private void calculateBend() {
        // Calculate angle from start to mouse position
        double directAngleRad = Math.atan2(endY - startY, endX - startX);
        double directAngle = Math.toDegrees(directAngleRad);
        if (directAngle < 0) {
            directAngle += 360;
        }

        // Find orientation of the bend
        // Calculate the normalized angle difference between directAngle and previousAngle1

        double angleDifference = directAngle - previousAngle1;
        if (angleDifference > 180) {
            angleDifference -= 360;
        } else if (angleDifference < -180) {
            angleDifference += 360;
        }

        // Update bendOrientationClockwise based on the angle difference
        // Don't update if previousAngle1 = -1, so the passed bendOrientationClockwise is not overwritten (if this is
        // the first time, this method is called)
        /*if (previousAngle1 != -1) {
            if (bendOrientationClockwise) {
                if (angleDifference < 0) {
                    bendOrientationClockwise = false;
                }
            } else {
                if (angleDifference > 0) {
                    bendOrientationClockwise = true;
                }
            }
        }*/
        if (bendOrientationClockwise) {
            if (angleDifference < 0) {
                bendOrientationClockwise = false;
            }
        } else {
            if (angleDifference > 0) {
                bendOrientationClockwise = true;
            }
        }

        // Find angle for the first segment
        double angle1;
        if (bendOrientationClockwise) {
            angle1 = GeometryUtils.roundAngleToNearest((int) directAngle, TrainLine.BEND_ANGLE, false);
        } else {
            angle1 = GeometryUtils.roundAngleToNearest((int) directAngle, TrainLine.BEND_ANGLE, true);
        }
        previousAngle1 = angle1;

        // Find angle for the second segment
        if (angle1 == directAngle) {
            hasBend = false;
        } else {
            hasBend = true;
            double angle2;
            if (bendOrientationClockwise) {
                angle2 = GeometryUtils.mirrorAngle((int) (angle1 + 45));
            } else {
                angle2 = GeometryUtils. mirrorAngle((int) (angle1 - 45));
            }

            // Calculate intersection point of the two segments
            double[] intersection = GeometryUtils.intersectLines(startX, startY, angle1, endX, endY, angle2);

            // Calculate bend point
            bendX = intersection[0];
            bendY = intersection[1];
        }
    }

    public Station getStartStation() {
        return startStation;
    }

    public void setStartStation(Station startStation) {
        this.startStation = startStation;
        this.startX = startStation.x;
        this.startY = startStation.y;
    }

    public Station getEndStation() {
        return endStation;
    }

    public void setEndStation(Station endStation) {
        this.endStation = endStation;
        if (endStation != null) {
            setEndPoint(endStation.x, endStation.y);
        }
        else {
            this.endX = startX;
            this.endY = startY;
        }
    }

    public Path2D getPath() {
        Path2D path = new Path2D.Double();
        path.moveTo(startX, startY);

        if (hasBend) {
            // Calculate bend points like in draw method
            double[] bendStart = GeometryUtils.shortenLine(startX, startY, bendX, bendY, (double) TrainLine.BEND_LENGTH / 2);
            double[] bendEnd = GeometryUtils.shortenLine(endX, endY, bendX, bendY, (double) TrainLine.BEND_LENGTH / 2);

            boolean thirdSegmentNotRequired = Math.abs(endY - startY) + (double) TrainLine.LINE_THICKNESS /2 < (double) TrainLine.BEND_LENGTH / 2;

            path.lineTo(bendStart[0], bendStart[1]);

            if (thirdSegmentNotRequired) {
                path.quadTo(bendX, bendY, endX, endY);
            } else {
                path.quadTo(bendX, bendY, bendEnd[0], bendEnd[1]);
                path.lineTo(endX, endY);
            }
        } else {
            path.lineTo(endX, endY);
        }

        return path;
    }
}