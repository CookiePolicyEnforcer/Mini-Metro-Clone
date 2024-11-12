package base.gameObjects.trainline;

import base.gameObjects.AbstractGameObject;
import base.gameObjects.Train;
import base.gameObjects.station.Station;
import base.main.GamePanel;

import java.awt.*;
import java.util.ArrayList;

public class TrainLine extends AbstractGameObject {
    // Settings
    public static final int LINE_THICKNESS = 15;
    public static final int BEND_ANGLE = 45;
    public static final int BEND_LENGTH = 10;

    private ArrayList<TrainLineSegment> segments;
    private ArrayList<Station> stations;
    private TrainLineSegment stationSelector;
    private ArrayList<Train> trains;
    private final GamePanel gamePanel;
    private final Color lineColor;

    public TrainLine(Color color, GamePanel gamepanel) {
        super(GamePanel.TRAIN_LINE_Z_INDEX, gamepanel);

        this.setPressable(true);
        this.lineColor = color;
        this.gamePanel = gamepanel;
        this.segments = new ArrayList<>();
        this.stations = new ArrayList<>();
        this.trains = new ArrayList<>();
    }

    @Override
    public void update(double deltaTime) {
        // Add a train if the line has at least one segment and no trains
        if (!gamePanel.isInBuildMode() && !segments.isEmpty() && trains.isEmpty() && !isLeftPressed()) {
            TrainLineSegment firstSegment = segments.getFirst();
            addTrain(new Train(firstSegment.getStartStation().x, firstSegment.getStartStation().y, this, firstSegment.getEndStation(), gamePanel));
        }
        // Remove all trains if the line has no segments
        if (segments.isEmpty() && !trains.isEmpty()) {
            removeAllTrains();
        }

        // Make stations selectable only in Play Mode
        if (!gamePanel.isInBuildMode()) {
            // Check if a station is pressed -> yes: mark station as selected and create a station selector
            ArrayList<Station> stationsToCheck = new ArrayList<>(stations);
            if (stationsToCheck.isEmpty()) {
                stationsToCheck = gamePanel.getStations();
            }

            for (Station station : stationsToCheck) {
                if (station.isLeftPressed()) {
                    if (!this.isLeftPressed()) {
                        if (!stations.isEmpty()) {
                            if (station != stations.getFirst() && station != stations.getLast()) {
                                continue;
                            }
                        }

                        this.setLeftPressed(true);
                    }

                    station.setSelected(true, lineColor);
                    if (stationSelector == null) {
                        stationSelector = new TrainLineSegment(station, this, gamePanel);
                        if (!stations.contains(station)) {
                            stations.add(station);
                        }
                    }
                    else {
                        stationSelector.update(deltaTime);
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics2D g2D) {
        segments.forEach((segment) -> {
            segment.draw(g2D);
        });

        if (stationSelector != null) {
            stationSelector.draw(g2D);
        }
    }

    @Override
    public boolean containsPoint(int x, int y) {
        return false;
    }


    public void addStationWithSelector(Station newStation) {
        if (stationSelector != null) {
            stationSelector.setEndStation(newStation);
            segments.add(stationSelector);
            stations.add(newStation);

            Station oldStation = stationSelector.getStartStation();
            oldStation.setLeftPressed(false);
            oldStation.setSelected(true, lineColor);
            newStation.setLeftPressed(true);
            newStation.setSelected(true, lineColor);
            stationSelector = null;
        }
    }

    public void removeStationWithSelector(Station station) {
        if (station == stationSelector.getStartStation()) {
            stationSelector.getStartStation().setLeftPressed(false);
            stationSelector.getStartStation().setSelected(false, null);
            segments.removeLast();
            stations.removeLast();

            // Select the new last station of the line
            if (!stations.isEmpty()) {
                stations.getLast().setLeftPressed(true);
                stations.getLast().setSelected(true, lineColor);
            }
            stationSelector = null;
        }
    }

    public void addTrain(Train train) {
        trains.add(train);
        gamePanel.getTrains().add(train);
    }

    public void removeTrain(Train train) {
        trains.remove(train);
        gamePanel.getTrains().remove(train);
    }

    public void removeAllTrains() {
        gamePanel.getTrains().removeAll(trains);
        trains.clear();
    }

    /**
     * Checks if this line is a circular line (first and last station are the same)
     */
    public boolean isCircular() {
        return !segments.isEmpty() &&
                segments.getFirst().getStartStation() == segments.getLast().getEndStation();
    }

    @Override
    public void setLeftPressed(boolean pressed) {
        // Allow line interaction only in Play Mode
        if (!gamePanel.isInBuildMode()) {
            super.setLeftPressed(pressed);
            if (pressed) {
                for (Station station : stations) {
                    station.setSelected(true, lineColor);
                }
            } else {
                for (Station station : stations) {
                    station.setSelected(false, null);
                    station.setLeftPressed(false); // Removes stationSelector when creating a circular line
                }
                if (stations.size() == 1) {
                    stations.removeFirst();
                }
                stationSelector = null;
            }
        }
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public Station getFirstStation() {
        if (stations.isEmpty()) {
            return null;
        }
        return stations.getFirst();
    }

    public Station getLastStation() {
        if (stations.isEmpty()) {
            return null;
        }
        return stations.getLast();
    }

    public ArrayList<TrainLineSegment> getSegments() {
        return segments;
    }

    /**
     * Gibt das aktuelle Segment basierend auf dem Index und der Bewegungsrichtung zurück
     */
    public TrainLineSegment getCurrentSegment(int currentSegmentIndex) {
        return segments.get(currentSegmentIndex);
    }

    /**
     * Ermittelt das nächste Segment basierend auf aktuellem Index und Bewegungsrichtung.
     * Gibt null zurück, wenn das Ende der Linie erreicht ist.
     */
    public TrainLineSegment getNextSegment(int currentSegmentIndex, boolean movingForward) {
        int nextIndex;
        if (movingForward) {
            nextIndex = currentSegmentIndex + 1;
            if (nextIndex >= segments.size()) {
                return null; // Ende der Linie erreicht
            }
        } else {
            nextIndex = currentSegmentIndex - 1;
            if (nextIndex < 0) {
                return null; // Anfang der Linie erreicht
            }
        }
        return segments.get(nextIndex);
    }

    /**
     * Gibt den Index eines Segments in der Liste zurück
     */
    public int getSegmentIndex(TrainLineSegment segment) {
        return segments.indexOf(segment);
    }

    public Color getColor() {
        return lineColor;
    }
}