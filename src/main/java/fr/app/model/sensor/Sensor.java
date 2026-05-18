package fr.app.model.sensor;

import fr.app.model.Drone;

public interface Sensor {
    void update(Drone drone);
    String getName();
}

