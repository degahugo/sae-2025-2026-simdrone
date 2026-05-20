package fr.app.model.sensor;

import com.jme3.math.Vector3f;
import fr.app.model.Drone;

public class CompassSensor implements Sensor {

    private float heading; // orientation en degrés
    private String name;

    public CompassSensor(String name) {
        this.name = name;
        this.heading = 0f;
    }

    @Override
    public void update(Drone drone) {

        // Récupère la direction "avant" du drone
        Vector3f forward =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Z);

        // Calcul de l'angle en degrés (0 à 360)
        this.heading = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));

        if (heading < 0) {
            heading += 360f;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public float getHeading() {
        return heading;
    }
}

