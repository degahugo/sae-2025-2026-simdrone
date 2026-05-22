package fr.app.model.sensor;

import fr.app.model.Drone;
import fr.app.view.LightingManager;
import fr.app.view.WaterFilterManager;

public class SensorManager {
    private final Drone drone;
    private final LightingManager lightingManager;
    private final WaterFilterManager waterFilterManager;

    public SensorManager(Drone drone, LightingManager lightingManager, WaterFilterManager waterFilterManager) {
        this.drone = drone;
        this.lightingManager = lightingManager;
        this.waterFilterManager = waterFilterManager;
    }

    public void initSensors() {
        TemperatureSensor temperatureSensor = new TemperatureSensor("TempSensor");
        drone.addSensor(temperatureSensor);

        DepthSensor depthSensor = new DepthSensor("DepthSensor");
        drone.addSensor(depthSensor);

        CompassSensor compassSensor = new CompassSensor("CompassSensor");
        drone.addSensor(compassSensor);

        PressureSensor pressureSensor = new PressureSensor("PressureSensor", depthSensor);
        drone.addSensor(pressureSensor);
    }

    /**
     * Met à jour tous les capteurs
     */
    public void updateSensors() {

        for (Sensor sensor : drone.getSensors()) {

            /*
             * Update capteur
             */
            sensor.update(drone);

            /*
             * Température
             */
            if (sensor instanceof TemperatureSensor ts) {

                ts.affectDrone(drone);
            }

            /*
             * Profondeur
             */
            if (sensor instanceof DepthSensor ds) {

                /*
                 * Lumière
                 */
                lightingManager
                        .updateUnderwaterLighting(
                                ds.getDepth()
                        );

                /*
                 * Eau
                 */
                waterFilterManager
                        .updateUnderwaterEffect(
                                ds.getDepth()
                        );
            }

            /*
             * Pression
             */
            if (sensor instanceof PressureSensor ps) {

                ps.affectDrone(drone);
            }
        }
    }
}