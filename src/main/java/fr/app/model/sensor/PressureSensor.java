package fr.app.model.sensor;

import com.jme3.math.Vector3f;
import fr.app.model.Drone;

public class PressureSensor implements Sensor {

    // pression actuelle en bar
    private float pressure;
    private String name;
    private float depth;
    // seuil critique
    private float criticalPressure = 8.0f;

    public PressureSensor(String name) {
        this.name = name;
        // pression atmosphérique à la surface
        this.pressure = 1.0f;
    }

    @Override
    public void update(Drone drone) {

        /*
         * Position du drone
         */
        Vector3f position =
                drone.getDroneSpatial()
                        .getWorldTranslation();

        /*
         * Profondeur :
         * sous l'eau => y négatif
         */
        depth = -position.y;

        /*
         * Empêche profondeur négative
         */
        depth = Math.max(0f, depth);

        /*
         * Calcul pression :
         *
         * 1 bar à la surface
         * +1 bar tous les 10 mètres
         */
        pressure = 1.0f + (depth / 10.0f);

        /*
         * Sécurité :
         * pression minimum = 1 bar
         */
        pressure = Math.max(1.0f, pressure);
    }

    /*
     * Effets de la pression sur le drone
     */
    public void affectDrone(Drone drone) {

        /*
         * Pression trop forte
         */
        if (pressure > criticalPressure) {

            // ralentit le drone
            drone.getState()
                    .setSpeedFactor(0.4f);

            System.out.println(
                    "⚠ Pression élevée : "
                            + pressure
                            + " bars"
            );

        } else {

            // vitesse normale
            drone.getState()
                    .setSpeedFactor(1.0f);
        }
    }

    /*
     * Getters
     */

    @Override
    public String getName() {
        return name;
    }

    public float getPressure() {
        return pressure;
    }

    public float getDepth() {
        return depth;
    }

    public float getCriticalPressure() {
        return criticalPressure;
    }

    /*
     * Setters
     */

    public void setCriticalPressure(
            float criticalPressure
    ) {

        this.criticalPressure =
                criticalPressure;
    }
}