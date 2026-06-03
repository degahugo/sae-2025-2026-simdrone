package fr.app.model.sensor;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import fr.app.model.Drone;

/**
 * Capteur de lumière du drone.
 * <p>
 * Représente l'intensité du faisceau d'éclairage, exprimée comme la
 * <b>portée</b> (en mètres) jusqu'à laquelle le drone parvient à éclairer.
 * Cette portée est le produit d'une portée nominale réglable (le "niveau de
 * puissance" choisi par le pilote) et d'un facteur d'atténuation lié à la
 * profondeur : plus le drone est profond, plus l'eau absorbe la lumière et
 * réduit la portée utile.
 * <p>
 * La valeur {@link #getRange()} est lue par la {@code DroneLight} pour régler
 * la portée réelle de la SpotLight, et {@link #getLightLevel()} fournit une
 * version normalisée (0..1) pour l'affichage côté client.
 */
public class LightSensor implements Sensor {

    private final String name;

    // Portée nominale en surface (mètres), réglable par le pilote / la logique
    private float porteeNominale = 50f;
    // Bornes de réglage de la portée nominale
    private static final float PORTEE_MIN = 10f;
    private static final float PORTEE_MAX = 120f;

    // Atténuation de la lumière dans l'eau (fraction perdue par mètre de profondeur)
    private static final float ABSORPTION_PAR_METRE = 0.015f;
    // Portée maximale possible (pour normaliser getLightLevel)
    private static final float PORTEE_REFERENCE = PORTEE_MAX;

    // Portée utile calculée (mètres)
    private float range;

    public LightSensor(String name) {
        this.name = name;
        this.range = porteeNominale;
    }

    /**
     * Recalcule la portée utile en fonction de la profondeur du drone.
     *
     * @param drone Drone dont on lit la position
     */
    @Override
    public void update(Drone drone) {
        Vector3f position = drone.getDroneSpatial().getWorldTranslation();
        float profondeur = Math.max(0f, -position.y);

        // Atténuation exponentielle : la lumière perd une fraction par mètre
        float attenuation = (float) Math.exp(-ABSORPTION_PAR_METRE * profondeur);

        this.range = porteeNominale * attenuation;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Portée d'éclairage utile en mètres (lue par DroneLight).
     */
    public float getRange() {
        return range;
    }

    /**
     * Augmente la portée nominale du faisceau (palier en mètres).
     */
    public void augmenterPortee(float pas) {
        porteeNominale = FastMath.clamp(porteeNominale + pas, PORTEE_MIN, PORTEE_MAX);
    }

    /**
     * Diminue la portée nominale du faisceau (palier en mètres).
     */
    public void diminuerPortee(float pas) {
        porteeNominale = FastMath.clamp(porteeNominale - pas, PORTEE_MIN, PORTEE_MAX);
    }

    public void setPorteeNominale(float portee) {
        this.porteeNominale = FastMath.clamp(portee, PORTEE_MIN, PORTEE_MAX);
    }

    public float getPorteeNominale() {
        return porteeNominale;
    }

    /**
     * Niveau de lumière normalisé (0.0 à 1.0) pour l'affichage client.
     */
    public float getLightLevel() {
        return FastMath.clamp(range / PORTEE_REFERENCE, 0f, 1f);
    }
}