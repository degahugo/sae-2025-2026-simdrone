package fr.app.model.sensor;

import com.jme3.math.Vector3f;
import fr.app.model.Drone;

/**
 * Capteur de profondeur permettant de mesurer la distance
 * entre le drone et la surface de l'eau. La profondeur est
 * calculée à partir de la position verticale du drone.
 */
public class DepthSensor implements Sensor {

    private float depth; // profondeur en mètres
    private String name;

    public DepthSensor(String name) {
        this.name = name;
        this.depth = 0f;
    }

    /**
     * Met à jour la profondeur en fonction de la position du drone.
     * La profondeur correspond à la valeur négative de l'axe Y.
     *
     * @param drone Drone dont on lit la position
     */
    @Override
    public void update(Drone drone) {

        // Récupère la position du drone dans la scène
        Vector3f position = drone.getDroneSpatial().getWorldTranslation();

        // Profondeur = distance sous la surface (Y négatif)
        this.depth = Math.max(0f, -position.y);

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
