package fr.app.model.sensor;

import com.jme3.math.Vector3f;
import fr.app.model.Drone;

/**
 * Capteur de compas permettant de déterminer l'orientation du drone
 * en degrés (0° à 360°). Le cap est calculé à partir de la direction
 * avant du drone dans l'espace 3D.
 */

public class CompassSensor implements Sensor {

    private float heading; // orientation en degrés
    private String name;

    public CompassSensor(String name) {
        this.name = name;
        this.heading = 0f;
    }

    /**
     * Met à jour l'orientation du drone en calculant l'angle
     * entre la direction avant et l'axe Nord.
     *
     * @param drone Drone dont on lit la rotation
     */
    @Override
    public void update(Drone drone) {

        // Récupère la direction "avant" du drone
        Vector3f forward =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Z);

        // Calcul de l'angle en degrés (0 à 360)
        this.heading = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));
        // Normalisation entre 0° et 360°
        if (heading < 0) {
            heading += 360f;
        }
    }

    /**
     * @return Nom du capteur
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return Orientation du drone en degrés (0° à 360°)
     */
    public float getHeading() {
        return heading;
    }
}

