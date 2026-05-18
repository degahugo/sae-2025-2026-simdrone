package fr.app.model.sensor;

import com.jme3.math.Vector3f;
import fr.app.model.Drone;

public class TemperatureSensor implements Sensor {

    private float temperature;
    private String name;

    public TemperatureSensor(String name) {
        this.name = name;
        this.temperature = 20.0f;
    }

    @Override
    public void update(Drone drone) {

        Vector3f position =
                drone.getDroneSpatial().getWorldTranslation();

        float profondeur = -position.y;

        // température de surface
        float tempSurface = 25f;

        // baisse avec profondeur
        temperature = tempSurface - profondeur * 0.5f;

        // simulation courant marin
        float courant =
                (float)Math.sin(position.x * 0.1f) * 2f;

        temperature += courant;
    }

    public void affectDrone(Drone drone) {

        if (temperature > 25f) {

            drone.getState().setSpeedFactor(0.5f);

            System.out.println(
                    "Eau chaude : vitesse réduite"
            );

        } else {

            drone.getState().setSpeedFactor(1.0f);
        }
    }


    @Override
    public String getName() {
        return name;
    }

    public float getTemperature() {
        return temperature;
    }
}