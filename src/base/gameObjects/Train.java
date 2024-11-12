package base.gameObjects;

import base.gameObjects.station.Station;
import base.gameObjects.trainline.TrainLine;
import base.gameObjects.trainline.TrainLineSegment;
import base.main.GamePanel;
import base.util.PathUtils;
import base.util.PathUtils.PathPosition;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

/**
 * Represents a train that moves along the segments ({@code TrainLineSegment}) of a {@code TrainLine},
 * picking up and dropping off passengers ({@code Passenger}) at stations ({@code Station}).
 */
public class Train extends AbstractGameObject {
    // Physical dimensions
    public static final int TRAIN_WIDTH = 50;
    public static final int TRAIN_HEIGHT = 30;

    // Movement parameters
    public static final double MAX_SPEED = 200.0;
    public static final double MIN_SPEED = 5.0;
    public static final double IDEAL_ACCELERATION_DISTANCE = 57.0;
    public static final double IDEAL_BRAKE_DISTANCE = 75.0;

    // Position and orientation
    private int x, y;
    private double angle;

    // Movement state
    private double currentSpeed;
    private boolean moving;
    private boolean movingForward = true;
    private double currentDistance = 0.0;
    private double totalPathLength;
    private double accelerationDistance;
    private double brakeDistance;

    // Train line information
    private final TrainLine trainLine;
    private TrainLineSegment currentSegment;
    private int currentSegmentIndex;

    // Passenger handling
    private final TrainPassengerCompartment passengerCompartment;

    /**
     * Creates a new train at the specified position on the given train line.
     *
     * @param x Initial x-coordinate
     * @param y Initial y-coordinate
     * @param trainLine The train line this train operates on
     * @param targetStation The initial target station
     * @param gamePanel Reference to the game panel
     */
    public Train(int x, int y, TrainLine trainLine, Station targetStation, GamePanel gamePanel) {
        super(GamePanel.TRAIN_Z_INDEX, gamePanel);
        this.x = x;
        this.y = y;
        this.trainLine = trainLine;
        this.moving = true;
        this.passengerCompartment = new TrainPassengerCompartment();

        initializeTrainPosition(x, y, targetStation);
    }

    /**
     * Updates the train's position, handles passenger boarding/unboarding, and manages
     * movement between track segments.
     *
     * @param deltaTime Time elapsed since last update
     */
    @Override
    public void update(double deltaTime) {
        if (!moving) return;

        currentSpeed = calculateSpeed(currentDistance);
        updateTrainPosition(deltaTime);

        updatePositionAndAngle();
        handlePassengerExchange();
    }

    /**
     * Renders the train and its passengers at the current position and orientation.
     *
     * @param g2D Graphics context
     */
    @Override
    public void draw(Graphics2D g2D) {
        AffineTransform oldTransform = g2D.getTransform();
        g2D.translate(x, y);
        g2D.rotate(angle);

        g2D.setColor(trainLine.getColor());
        g2D.fillRect(-TRAIN_WIDTH/2, -TRAIN_HEIGHT/2, TRAIN_WIDTH, TRAIN_HEIGHT);
        passengerCompartment.draw(g2D, 0, 0, angle);

        g2D.setTransform(oldTransform);
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return false;
    }

    /**
     * Initializes the train's position on the track and determines initial movement direction.
     */
    private void initializeTrainPosition(int x, int y, Station targetStation) {
        PathPosition startPos = findPositionOnLine(x, y);
        this.currentSegmentIndex = startPos.segmentIndex;
        this.currentSegment = trainLine.getCurrentSegment(currentSegmentIndex);
        this.currentDistance = startPos.distance;
        this.movingForward = shouldMoveForward(currentSegmentIndex, currentDistance, targetStation);
        initializePathMovement();
    }

    /**
     * Finds the closest position on the train line to the given coordinates.
     */
    private PathPosition findPositionOnLine(int x, int y) {
        ArrayList<TrainLineSegment> segments = trainLine.getSegments();

        for (int i = 0; i < segments.size(); i++) {
            double distance = PathUtils.findDistanceOnPath(segments.get(i).getPath(), x, y);
            if (distance >= 0) {
                return new PathPosition(i, distance);
            }
        }
        return new PathPosition(0, 0);
    }

    /**
     * Initializes path movement parameters including acceleration and braking distances.
     */
    private void initializePathMovement() {
        Path2D path = currentSegment.getPath();
        totalPathLength = PathUtils.calculatePathLength(path);
        currentDistance = movingForward ? 0 : totalPathLength;

        // Adjust acceleration and brake distances based on path length
        if (IDEAL_ACCELERATION_DISTANCE + IDEAL_BRAKE_DISTANCE > totalPathLength) {
            double ratio = IDEAL_ACCELERATION_DISTANCE / (IDEAL_ACCELERATION_DISTANCE + IDEAL_BRAKE_DISTANCE);
            accelerationDistance = totalPathLength * ratio;
            brakeDistance = totalPathLength * (1 - ratio);
        } else {
            accelerationDistance = IDEAL_ACCELERATION_DISTANCE;
            brakeDistance = IDEAL_BRAKE_DISTANCE;
        }
    }

    /**
     * Determines whether the train should move forward based on target station position.
     */
    private boolean shouldMoveForward(int segmentIndex, double distance, Station targetStation) {
        ArrayList<TrainLineSegment> segments = trainLine.getSegments();
        double targetDistance = -1;
        int targetSegmentIndex = -1;

        for (int i = 0; i < segments.size(); i++) {
            TrainLineSegment segment = segments.get(i);
            if (segment.getStartStation() == targetStation) {
                targetSegmentIndex = i;
                targetDistance = 0;
                break;
            }
            if (segment.getEndStation() == targetStation) {
                targetSegmentIndex = i;
                targetDistance = PathUtils.calculatePathLength(segment.getPath());
                break;
            }
        }

        if (targetSegmentIndex == segmentIndex) {
            return distance < targetDistance;
        }
        return targetSegmentIndex > segmentIndex;
    }

    /**
     * Calculates the current speed based on position, considering acceleration and braking phases.
     */
    private double calculateSpeed(double distance) {
        if (movingForward) {
            if (distance <= accelerationDistance) {
                // Acceleration phase
                double ratio = distance / accelerationDistance;
                return MIN_SPEED + (MAX_SPEED - MIN_SPEED) * ratio;
            } else if (distance >= totalPathLength - brakeDistance) {
                // Deceleration phase
                double ratio = (totalPathLength - distance) / brakeDistance;
                return MIN_SPEED + (MAX_SPEED - MIN_SPEED) * ratio;
            }
        } else {
            if (distance >= totalPathLength - accelerationDistance) {
                // Acceleration phase (reverse direction)
                double ratio = (totalPathLength - distance) / accelerationDistance;
                return MIN_SPEED + (MAX_SPEED - MIN_SPEED) * ratio;
            } else if (distance <= brakeDistance) {
                // Deceleration phase (reverse direction)
                double ratio = distance / brakeDistance;
                return MIN_SPEED + (MAX_SPEED - MIN_SPEED) * ratio;
            }
        }
        return MAX_SPEED;  // Constant maximum speed between phases
    }

    /**
     * Updates the train's position along the track, handling segment transitions
     * and direction changes.
     */
    private void updateTrainPosition(double deltaTime) {
        double moveDistance = currentSpeed * deltaTime;
        if (movingForward) {
            handleForwardMovement(moveDistance);
        } else {
            handleBackwardMovement(moveDistance);
        }
    }

    /**
     * Handles forward movement along the track, including segment transitions.
     */
    private void handleForwardMovement(double moveDistance) {
        double newDistance = currentDistance + moveDistance;
        if (newDistance >= totalPathLength) {
            TrainLineSegment nextSegment = trainLine.getNextSegment(currentSegmentIndex, true);
            if (nextSegment == null) {
                if (trainLine.isCircular()) {
                    // Return to first segment for circular lines
                    currentSegment = trainLine.getCurrentSegment(0);
                    currentSegmentIndex = 0;
                    currentDistance = 0;
                    initializePathMovement();
                } else {
                    // Reverse direction for regular lines
                    currentDistance = totalPathLength;
                    movingForward = false;
                }
            } else {
                // Move to next segment
                currentSegment = nextSegment;
                currentSegmentIndex++;
                currentDistance = 0;
                initializePathMovement();
            }
        } else {
            currentDistance = newDistance;
        }
    }

    /**
     * Handles backward movement along the track, including segment transitions.
     */
    private void handleBackwardMovement(double moveDistance) {
        double newDistance = currentDistance - moveDistance;
        if (newDistance <= 0) {
            TrainLineSegment nextSegment = trainLine.getNextSegment(currentSegmentIndex, false);
            if (nextSegment == null) {
                if (trainLine.isCircular()) {
                    // Move to last segment for circular lines
                    currentSegmentIndex = trainLine.getSegments().size() - 1;
                    currentSegment = trainLine.getCurrentSegment(currentSegmentIndex);
                    currentDistance = PathUtils.calculatePathLength(currentSegment.getPath());
                    initializePathMovement();
                } else {
                    // Reverse direction for regular lines
                    currentDistance = 0;
                    movingForward = true;
                }
            } else {
                // Move to previous segment
                currentSegment = nextSegment;
                currentSegmentIndex--;
                currentDistance = PathUtils.calculatePathLength(nextSegment.getPath());
                initializePathMovement();
            }
        } else {
            currentDistance = newDistance;
        }
    }

    /**
     * Updates the train's position and rotation angle based on the current path segment.
     */
    private void updatePositionAndAngle() {
        Path2D path = currentSegment.getPath();
        PathIterator pi = path.getPathIterator(null);
        double[] coords = new double[6];
        double remainingDistance = currentDistance;
        double segmentStartX = 0, segmentStartY = 0;
        boolean first = true;

        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);

            switch (type) {
                case PathIterator.SEG_MOVETO:
                    // Store initial coordinates for path traversal
                    if (first) {
                        segmentStartX = coords[0];
                        segmentStartY = coords[1];
                        first = false;
                    }
                    break;

                case PathIterator.SEG_LINETO:
                    double segmentLength = Math.sqrt(
                            Math.pow(coords[0] - segmentStartX, 2) +
                                    Math.pow(coords[1] - segmentStartY, 2)
                    );

                    if (remainingDistance <= segmentLength) {
                        // Calculate position along the current line segment
                        double ratio = remainingDistance / segmentLength;
                        x = (int) (segmentStartX + (coords[0] - segmentStartX) * ratio);
                        y = (int) (segmentStartY + (coords[1] - segmentStartY) * ratio);

                        // Calculate rotation angle for the train based on segment direction
                        double dx = coords[0] - segmentStartX;
                        double dy = coords[1] - segmentStartY;
                        angle = Math.atan2(dy, dx);
                        if (!movingForward) {
                            angle += Math.PI; // Rotate 180° when moving backwards
                        }
                        return;
                    }

                    remainingDistance -= segmentLength;
                    segmentStartX = coords[0];
                    segmentStartY = coords[1];
                    break;

                case PathIterator.SEG_QUADTO:
                    // Handle curved path segments using quadratic Bézier curves
                    double steps = 10;
                    double lastQuadX = segmentStartX;
                    double lastQuadY = segmentStartY;
                    double[] curvePoints = new double[(int) (steps * 2)];

                    // Pre-calculate points along the curve for smoother movement
                    for (int i = 0; i <= steps; i++) {
                        double t = i / steps;
                        // Calculate point on quadratic Bézier curve using parametric equation
                        double newX = Math.pow(1-t, 2) * segmentStartX +
                                2 * (1-t) * t * coords[0] +
                                Math.pow(t, 2) * coords[2];
                        double newY = Math.pow(1-t, 2) * segmentStartY +
                                2 * (1-t) * t * coords[1] +
                                Math.pow(t, 2) * coords[3];

                        // Store curve points for later angle calculation
                        if (i < steps) {
                            curvePoints[i*2] = newX;
                            curvePoints[i*2+1] = newY;
                        }

                        if (i > 0) {
                            double stepLength = Math.sqrt(
                                    Math.pow(newX - lastQuadX, 2) +
                                            Math.pow(newY - lastQuadY, 2)
                            );

                            if (remainingDistance <= stepLength) {
                                // Interpolate position along current curve segment
                                double ratio = remainingDistance / stepLength;
                                x = (int) (lastQuadX + (newX - lastQuadX) * ratio);
                                y = (int) (lastQuadY + (newY - lastQuadY) * ratio);

                                // Calculate rotation angle using next point on curve for smooth transitions
                                double nextX, nextY;
                                if (i < steps) {
                                    nextX = curvePoints[i*2];
                                    nextY = curvePoints[i*2+1];
                                } else {
                                    nextX = coords[2];
                                    nextY = coords[3];
                                }
                                angle = Math.atan2(nextY - lastQuadY, nextX - lastQuadX);
                                if (!movingForward) {
                                    angle += Math.PI; // Rotate 180° when moving backwards
                                }
                                return;
                            }

                            remainingDistance -= stepLength;
                        }

                        lastQuadX = newX;
                        lastQuadY = newY;
                    }

                    segmentStartX = coords[2];
                    segmentStartY = coords[3];
                    break;
            }

            pi.next();
        }
    }

    /**
     * Handles passenger exchange at stations, including boarding and unboarding.
     */
    private void handlePassengerExchange() {
        if (currentSegment == null) return;

        Station currentStation = findNearbyStation();
        if (currentStation == null) return;

        // First unload passengers with matching shape
        passengerCompartment.unloadPassengersWithShape(currentStation.getCurrentShapeType());

        // Then board new passengers if there's space
        if (!passengerCompartment.isFull()) {
            ArrayList<Passenger> stationPassengers = currentStation.getPassengers();
            ArrayList<Passenger> passengersToBoard = new ArrayList<>(stationPassengers);

            for (Passenger passenger : passengersToBoard) {
                if (passengerCompartment.addPassenger(passenger)) {
                    stationPassengers.remove(passenger);
                }
            }
        }
    }

    /**
     * Finds a station near the train's current position.
     * @return The nearby station or null if none found within proximity
     */
    private Station findNearbyStation() {
        double stationProximity = 0.1; // Distance threshold for passenger exchange

        if (Math.abs(x - currentSegment.getStartStation().x) < stationProximity &&
                Math.abs(y - currentSegment.getStartStation().y) < stationProximity) {
            return currentSegment.getStartStation();
        }

        if (Math.abs(x - currentSegment.getEndStation().x) < stationProximity &&
                Math.abs(y - currentSegment.getEndStation().y) < stationProximity) {
            return currentSegment.getEndStation();
        }

        return null;
    }
}