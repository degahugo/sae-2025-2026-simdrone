package fr.app.model.sensor;

import com.jme3.math.Vector3f;
import fr.app.model.Drone;

public class DepthSensor implements Sensor {

    private float depth;
    private String name;

    public DepthSensor(String name) {
        this.name = name;
        this.depth = 0f;
    }

    @Override
    public void update(Drone drone) {

        // Récupère la position du drone dans la scène
        Vector3f position = drone.getDroneSpatial().getWorldTranslation();

        // La profondeur est l'opposé de la coordonnée Y
        this.depth = -position.y;

        // Empêche les valeurs négatives (si le drone sort de l'eau)
        if (this.depth < 0) {
            this.depth = 0;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public float getDepth() {
        return depth;
    }
}

