package base.gameObjects.station;

import base.gameObjects.Passenger;
import base.gameObjects.shape.ShapeType;
import base.main.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PassengerSpawner {
    private static final double BASE_SPAWN_INTERVAL = 5.0;
    private static final int MAX_PASSENGERS = 21;
    private static final int PASSENGERS_PER_ROW = 7;
    private static final int PASSENGER_SPACING = 1;
    private static final int ROW_SPACING = 1;
    private static final int OFFSET_X = Station.SIZE/2;         // Horizontal distance from the station
    private static final int OFFSET_Y = -Station.SIZE/2;        // Vertical distance from the station (negative = up)

    private final Station station;
    private final GamePanel gamePanel;
    private final Random random;
    private double timeSinceLastSpawn;
    private double spawnRateMultiplier;

    /**
     * Creates a new {@code PassengerSpawner} for the given station and game panel.
     * @param station The station at which passengers should spawn
     * @param gamePanel The game panel in which the station is located
     */
    public PassengerSpawner(Station station, GamePanel gamePanel) {
        this.station = station;
        this.gamePanel = gamePanel;
        this.random = new Random();
        this.timeSinceLastSpawn = 0;
        this.spawnRateMultiplier = 1.0;
    }

    public void update(double deltaTime, ArrayList<Passenger> passengers) {
        if (passengers.size() >= MAX_PASSENGERS) {
            return;
        }

        // Spawn a passenger if enough time has passed
        timeSinceLastSpawn += deltaTime;
        double currentSpawnInterval = BASE_SPAWN_INTERVAL * spawnRateMultiplier;

        if (timeSinceLastSpawn >= currentSpawnInterval) {
            spawnPassenger(passengers);
            timeSinceLastSpawn = 0;
        }
    }

    /**
     * Spawns a new passenger with a random allowed shape at the station of this {@code PassengerSpawner}.
     * @param passengers The list of passengers to which the new passenger should be added
     */
    private void spawnPassenger(ArrayList<Passenger> passengers) {
        ShapeType shape = getRandomAllowedShape();
        if (shape != null) {
            Point position = getSpawnPosition(passengers.size());
            Passenger passenger = new Passenger(position.x, position.y, shape, gamePanel);
            passengers.add(passenger);
        }
    }

    /**
     * Calculates the spawn position for a new passenger based on {@code PASSENGERS_PER_ROW}.
     * Passengers are arranged in a zigzag pattern, alternating between left-to-right and right-to-left rows.
     * The last passenger in each row is placed slightly lower to indicate the row's direction.
     * <p>
     * The pattern looks like this (P = passenger, p = lower -> = direction):
     * <pre>
     * -> P P P P P P p    (row 0: left to right, 'p' is lower)
     * <- p P P P P P      (row 1: right to left, 'p' is lower)
     *      P P P P P p    (row 2: left to right...)
     * </pre>
     *
     * @param passengerNumber The number of the passenger to be spawned (first passenger = 0).
     * @return A Point object containing the x and y coordinates where the passenger should spawn
     */
    private Point getSpawnPosition(int passengerNumber) {
        passengerNumber -= 1; // Allow one passenger more in the first row (the first P in the example above)
                                // Remove if first row should also have 7 passengers, like the other rows

        int row = passengerNumber / PASSENGERS_PER_ROW;
        int posInRow = passengerNumber % PASSENGERS_PER_ROW;

        int baseX = station.x + OFFSET_X;
        int baseY = station.y + OFFSET_Y;
        int x = baseX;
        int y = (int) (baseY + (Passenger.SIZE + ROW_SPACING) * row);

        boolean isEvenRow = row % 2 == 0;
        // Spawn direction for even rows: left to right
        if (isEvenRow) {
            int posInRowAdjusted = posInRow + 1; // Even rows start with an offset of 1 to the right
            x = baseX + (Passenger.SIZE + PASSENGER_SPACING) * posInRowAdjusted + (Passenger.SIZE + PASSENGER_SPACING);
            if (posInRow == PASSENGERS_PER_ROW - 1) {
                y += (Passenger.SIZE / 2) * (row + 1);
            }
        }
        // Spawn direction for odd rows: right to left
        else {
            int posInRowInverted = PASSENGERS_PER_ROW - posInRow;   // Odd rows are filled from right to left
            int posInRowAdjusted = posInRowInverted- 1;             // Odd rows start with an offset of 1 to the left
            x = baseX + (Passenger.SIZE + PASSENGER_SPACING) * posInRowAdjusted + (Passenger.SIZE + PASSENGER_SPACING);
            if (posInRowAdjusted == 0) {
                y += (Passenger.SIZE / 2) * row;
            }
        }
        return new Point(x, y);
    }

    /**
     * Returns a random shape that is allowed to spawn at the station of this {@code PassengerSpawner}. Allowed shapes are:
     * <ol>
     *  <li>shapes of existing stations<br></li>
     *  <li>different shapes than the station of this {@code PassengerSpawner}</li>
     * </ol>
     * @return Random shape or null if no shape is available
     */
    private ShapeType getRandomAllowedShape() {
        // Get all shapes of existing stations, excluding the shape of this station
        List<ShapeType> availableShapes = new ArrayList<>();
        for (Station station : gamePanel.getStations()) {
            ShapeType stationType = station.getCurrentShapeType();
            if (stationType != this.station.getCurrentShapeType() && !availableShapes.contains(stationType)) {
                availableShapes.add(stationType);
            }
        }

        // Return a random shape from the available shapes or null if no shape is available
        return availableShapes.isEmpty() ? null : availableShapes.get(random.nextInt(availableShapes.size()));
    }
}