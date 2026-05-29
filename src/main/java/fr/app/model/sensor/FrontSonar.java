package fr.app.model.sensor;
import fr.app.model.Drone;

public class FrontSonar implements Sensor {
    private float distance = -1f;

    @Override
    public void update(Drone drone) {
        // Calcul du sonar plus tard
        distance = 42f; // valeur test
    }

    @Override
    public String getName() {
        return "Front Sonar";
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

}

