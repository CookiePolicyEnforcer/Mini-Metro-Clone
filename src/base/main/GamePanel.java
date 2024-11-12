package base.main;

import base.gameObjects.*;
import base.gameObjects.shape.ShapeType;
import base.gameObjects.station.Station;
import base.gameObjects.station.StationExclusionCircle;
import base.gameObjects.train.Train;
import base.gameObjects.trainline.TrainLine;
import base.gameObjects.ui.ModeToggle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class GamePanel extends JPanel {
    final int FPS = 60;
    public static final Color BACKGROUND_COLOR = Color.WHITE;

    public static final int GRID_Z_INDEX = 0;
    public static final int STATION_EXCLUSION_CIRCLE_Z_INDEX = 1;
    public static final int TRAIN_LINE_Z_INDEX = 2;
    public static final int TRAIN_Z_INDEX = 3;
    public static final int STATION_Z_INDEX = 4;
    public static final int PASSENGER_Z_INDEX = 5;
    public static final int UI_Z_INDEX = 10;

    // Game objects
    private ScheduledExecutorService executorService;
    private final Grid grid;
    private final ArrayList<StationExclusionCircle> exclusionCircles;
    private final ArrayList<Station> stations;
    private final ArrayList<TrainLine> trainLines;
    private final ArrayList<Train> trains;

    // UI objects
    private final ModeToggle modeToggle;

    public GamePanel() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(BACKGROUND_COLOR);
        this.setDoubleBuffered(true);
        setFocusable(true);

        InputHandler inputHandler = new InputHandler(this);
        addKeyListener(inputHandler);
        addMouseListener(inputHandler);

        grid = new Grid(this);
        exclusionCircles = new ArrayList<>();
        stations = new ArrayList<>();
        trainLines = new ArrayList<>();
        trainLines.add(new TrainLine(Color.red, this));
        trains = new ArrayList<>();

        modeToggle = new ModeToggle(this);

        // Place three stations at the beginning of the game
        grid.addStation(150, 150);
        stations.get(0).setShapeType(ShapeType.CIRCLE);
        grid.addStation(450, 150);
        stations.get(1).setShapeType(ShapeType.SQUARE);
        grid.addStation(450, 450);
        stations.get(2).setShapeType(ShapeType.TRIANGLE);
    }

    public void startGameThread() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            update(1.0 / FPS);
            repaint();
        }, 0, 1000 / FPS, TimeUnit.MILLISECONDS);
    }

    public void stopGameThread() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    public void update(double deltaTime) {
        try {
            modeToggle.update(deltaTime);
            // Build-Mode-only updates
            if (modeToggle.isInBuildMode()) {
                grid.update(deltaTime);
            }
            // Game-Mode-only updates
            else {
                for (Train train : trains) {
                    train.update(deltaTime);
                }
            }
            // Always update
            for (Station station : stations) {
                station.update(deltaTime);
            }
            for (TrainLine trainLine : trainLines) {
                trainLine.update(deltaTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;

        // TODO: Implement drawing with z index (could lead to performance issues)

        if (modeToggle.isInBuildMode()) {
            grid.draw(g2D);
        }

        for (StationExclusionCircle exclusionCircle: exclusionCircles) {
            exclusionCircle.draw(g2D);
        }

        for (TrainLine trainLine : trainLines) {
            trainLine.draw(g2D);
        }

        for (Train train : trains) {
            train.draw(g2D);
        }

        for (Station station : stations) {
            station.draw(g2D);
        }

        // UI
        modeToggle.draw(g2D);
    }

    public boolean isInBuildMode() {
        return modeToggle.isInBuildMode();
    }

    public ArrayList<AbstractGameObject> getInteractableGameObjects() {
        // Combine all game objects that are pressable into a single list
        ArrayList<AbstractGameObject> gameObjects = new ArrayList<>();
        gameObjects.add(grid);
        gameObjects.addAll(stations);
        gameObjects.addAll(trainLines);
        gameObjects.addAll(trains);
        gameObjects.add(modeToggle);
        return gameObjects;
    }

    public ArrayList<AbstractGameObject> getGameObjects() {
        // Combine all game objects into a single list
        ArrayList<AbstractGameObject> gameObjects = new ArrayList<>();
        gameObjects.add(grid);
        gameObjects.addAll(exclusionCircles);
        gameObjects.addAll(stations);
        gameObjects.addAll(trainLines);
        gameObjects.addAll(trains);
        gameObjects.add(modeToggle);
        return gameObjects;
    }

    public ArrayList<StationExclusionCircle> getExclusionCircles() {
        return exclusionCircles;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public ArrayList<TrainLine> getTrainLines() {
        return trainLines;
    }

    public ArrayList<Train> getTrains() {
        return trains;
    }

    public Grid getGrid() {
        return grid;
    }
}
