package base.gameObjects.train;

import base.gameObjects.Passenger;
import base.gameObjects.shape.ShapeType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

class TrainPassengerCompartment {
    private static final int ROWS = 2;
    private static final int COLS = 3;
    private static final int MAX_PASSENGERS = ROWS * COLS;
    private static final int PASSENGER_SIZE = (int)(Train.TRAIN_HEIGHT * 0.4);
    private static final double SHAPE_ORIENTATION_OFFSET = Math.PI/2; // 90° offset as shapes point upward by default

    private final List<Passenger> passengers;
    private double previousAngle = Double.MIN_VALUE;

    public TrainPassengerCompartment() {
        this.passengers = new ArrayList<>();
    }

    public void draw(Graphics2D g2D, double trainX, double trainY, double trainAngle) {
        // Calculate the change in angle since the last update
        double angleChange = 0;
        if (previousAngle == Double.MIN_VALUE) {
            previousAngle = trainAngle;
            angleChange = trainAngle;
        }
        else if (trainAngle != previousAngle) {
            if (trainAngle > previousAngle) {
                angleChange = trainAngle - previousAngle;
            } else {
                angleChange = previousAngle - trainAngle;
            }
            previousAngle = trainAngle;
        }

        AffineTransform oldTransform = g2D.getTransform();

        // Move to train's center
        g2D.translate(trainX, trainY);

        // Calculate spacing between passengers
        int spacingX = (Train.TRAIN_WIDTH - (COLS * PASSENGER_SIZE)) / (COLS + 1);
        int spacingY = (Train.TRAIN_HEIGHT - (ROWS * PASSENGER_SIZE)) / (ROWS + 1);

        // Starting position (top-left corner of passenger grid)
        int startX = -Train.TRAIN_WIDTH/2 + spacingX;
        int startY = -Train.TRAIN_HEIGHT/2 + spacingY;

        for (int i = 0; i < passengers.size(); i++) {
            // Calculate grid position (right to left, top to bottom)
            int visualCol = COLS - 1 - (i / ROWS);
            int visualRow = i % ROWS;

            // Calculate actual pixel position
            int x = startX + visualCol * (PASSENGER_SIZE + spacingX);
            int y = startY + visualRow * (PASSENGER_SIZE + spacingY);

            // Store passenger's original state
            Passenger passenger = passengers.get(i);
            int originalX = passenger.x;
            int originalY = passenger.y;
            int originalSize = passenger.getSize();

            // Position passenger in train
            passenger.x = x + PASSENGER_SIZE/2;
            passenger.y = y + PASSENGER_SIZE/2;
            passenger.setSize(PASSENGER_SIZE);

            // Rotate passenger
            AffineTransform passengerTransform = g2D.getTransform();
            g2D.translate(passenger.x, passenger.y);
            g2D.rotate(angleChange + SHAPE_ORIENTATION_OFFSET);
            g2D.translate(-passenger.x, -passenger.y);

            passenger.draw(g2D);

            // Restore transform for next passenger
            g2D.setTransform(passengerTransform);

            // Restore passenger's original state
            passenger.x = originalX;
            passenger.y = originalY;
            passenger.setSize(originalSize);
        }

        g2D.setTransform(oldTransform);
    }

    public boolean addPassenger(Passenger passenger) {
        if (passengers.size() >= MAX_PASSENGERS) {
            return false;
        }
        passenger.setInTrain(true);
        passengers.add(passenger);
        return true;
    }

    public void unloadPassengersWithShape(ShapeType shapeType) {
        passengers.removeIf(passenger -> passenger.getShapeType() == shapeType);
    }

    public boolean isFull() {
        return passengers.size() >= MAX_PASSENGERS;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }
}