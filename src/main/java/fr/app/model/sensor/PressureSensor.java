package fr.app.model.sensor;

import fr.app.model.Drone;

public class PressureSensor implements Sensor {
    private float pressure;
    private String name;
    private DepthSensor depthSensor;

    public PressureSensor(String name, DepthSensor depthSensor) {
        this.name = name;
        this.depthSensor = depthSensor;
        // pression atmosphérique à la surface de la mer en bar
        this.pressure = 1.0f;
    }

    @Override
    public void update(Drone drone) {
        //on récupère la profondeur qui a déjà été calculé
        float depth = depthSensor.getDepth();

        //Pour le calcul de la pression à la surface on a 1 bar, on sait que dans l'eau on gagne 1 bar en
        //pression tous les 10 mètres
        this.pressure = 1.0f + (depth / 10.0f);

        if (this.pressure < 1.0f) {
            this.pressure = 1.0f;
        }
    }

    public void affectDrone(Drone drone) {
        // Cas où pression trop forte
        if (pressure > 8.0f) {
            drone.getState().setSpeedFactor(0.5f);

            System.out.println("⚠ Pression élevée");

        } else {
            drone.getState().setSpeedFactor(1.0f);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public float getPressure() {
        return pressure;
    }
}