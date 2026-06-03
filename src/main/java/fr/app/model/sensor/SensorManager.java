package fr.app.model.sensor;

import fr.app.model.Drone;
import fr.app.view.LightingManager;
import fr.app.view.WaterFilterManager;

public class SensorManager {
    private final Drone drone;
    private final LightingManager lightingManager;
    private final WaterFilterManager waterFilterManager;

    // WaterFilterManager du viewport offscreen (retour vidéo client).
    // Initialisé après la création du DroneFrameCapture via setOffscreenWaterFilterManager().
    private WaterFilterManager offscreenWaterFilterManager;

    public SensorManager(Drone drone, LightingManager lightingManager, WaterFilterManager waterFilterManager) {
        this.drone = drone;
        this.lightingManager = lightingManager;
        this.waterFilterManager = waterFilterManager;
    }

    /**
     * Branche le WaterFilterManager du viewport offscreen.
     * Doit être appelé après la création du DroneFrameCapture.
     */
    public void setOffscreenWaterFilterManager(WaterFilterManager offscreenWaterFilterManager) {
        this.offscreenWaterFilterManager = offscreenWaterFilterManager;
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

    public void updateSensors() {
        for (Sensor sensor : drone.getSensors()) {
            sensor.update(drone);

            if (sensor instanceof TemperatureSensor ts) {ts.affectDrone(drone);
                //System.out.println("Température : "+ ts.getTemperature()+ "°C");

            }
            if (sensor instanceof DepthSensor ds) {
                lightingManager.updateUnderwaterLighting(ds.getDepth());
                waterFilterManager.updateUnderwaterEffect(ds.getDepth());
                //System.out.println("Profondeur : "+ ds.getDepth()+ " m");

                // Met à jour le WaterFilter du retour vidéo client (offscreen).
                // Sans ça, la couleur/turbidité côté client ne change jamais.
                if (offscreenWaterFilterManager != null) {
                    offscreenWaterFilterManager.updateUnderwaterEffect(ds.getDepth());
                }
            }

            if (sensor instanceof PressureSensor ps) {ps.affectDrone(drone);
                //System.out.println("Pressure : "+ ps.getPressure()+ "bar");
            }

            if (sensor instanceof CompassSensor) {CompassSensor cs = (CompassSensor) sensor;
                //System.out.println("Cap : "+ cs.getHeading()+ "°");
            }

        }
    }
}