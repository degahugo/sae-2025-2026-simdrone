package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import fr.app.model.Drone;
import fr.app.view.camera.DroneCameraManager;

/**
 * Lampe d'éclairage embarquée propre à un drone.
 * <p>
 * Implémentée avec une unique {@link SpotLight} (projecteur), qui est la façon
 * réaliste d'éclairer en jME3 : la lumière éclaire les SURFACES qu'elle frappe
 * (fond marin, parois, objets). Il n'y a volontairement pas de "cône" dessiné
 * dans l'eau : un tel objet, dont l'origine est collée à la caméra, ne peut pas
 * rendre correctement (on le verrait de l'intérieur).
 * <p>
 * Conséquence importante : on ne voit la lampe QUE lorsqu'elle frappe une
 * surface. Pointée vers l'eau ouverte (l'horizon), elle n'éclaire rien de
 * visible — exactement comme une vraie lampe.
 * <p>
 * La SpotLight est attachée au rootNode partagé entre la fenêtre serveur et le
 * viewport off-screen, donc visible dans les deux rendus. Elle suit en
 * permanence la position et la direction du regard du drone.
 */
public class DroneLight {

    private final SpotLight spot;
    private final Node rootNode;
    private DroneCameraManager cameraManager;

    private boolean on = false;          // éteinte par défaut
    private float range = 50f;           // portée réelle (m), pilotée par le LightSensor

    // === RÉGLAGES (ajuste ici selon le rendu que tu vois) ===
    // Intensité : .mult(N). ↑ = plus lumineux, ↓ si la zone "crame" en blanc.
    private static final ColorRGBA COULEUR =
            new ColorRGBA(0.95f, 0.97f, 1.0f, 1.0f).mult(50f);
    // Cœur du faisceau à pleine puissance. Petit = dégradé doux sur tout le cône.
    private static final float ANGLE_INTERNE = 6f  * FastMath.DEG_TO_RAD;
    // Bord du cône (lumière nulle au-delà). Écart avec l'interne = douceur du dégradé.
    private static final float ANGLE_EXTERNE = 30f * FastMath.DEG_TO_RAD;

    public DroneLight(Node rootNode, Drone drone, DroneCameraManager cameraManager,
                      AssetManager assetManager) {
        this.rootNode = rootNode;
        this.cameraManager = cameraManager;

        this.spot = new SpotLight();
        this.spot.setSpotRange(range);
        this.spot.setSpotInnerAngle(ANGLE_INTERNE);
        this.spot.setSpotOuterAngle(ANGLE_EXTERNE);
        this.spot.setColor(COULEUR);
        this.spot.setPosition(Vector3f.ZERO);
        this.spot.setDirection(Vector3f.UNIT_Z);
        rootNode.addLight(spot);

        appliquerEtat();
    }

    /**
     * Repositionne la lampe sur le drone à chaque frame.
     * Doit être appelé depuis le thread JME3 (simpleUpdate).
     */
    public void update() {
        if (cameraManager == null) return;

        Node cameraNode = cameraManager.getCameraNode();
        Vector3f position = cameraNode.getWorldTranslation();

        // Direction du regard = rotation du drone * rotation manuelle de la caméra.
        // L'axe avant de la caméra jME correspond à rotation * UNIT_Z.
        Quaternion droneRotation = cameraManager.getDrone().getDroneSpatial().getWorldRotation();
        Quaternion manualRotation = cameraManager.getManualRotation();
        Quaternion finalRotation = droneRotation.mult(manualRotation);
        Vector3f direction = finalRotation.mult(Vector3f.UNIT_Z).normalizeLocal();

        spot.setPosition(position);
        spot.setDirection(direction);
    }

    /** Allume ou éteint la lampe. */
    public void setOn(boolean on) {
        this.on = on;
        appliquerEtat();
    }

    public boolean isOn() {
        return on;
    }

    /** Met à jour la caméra suivie (après un changement de vue avant/arrière). */
    public void setCameraManager(DroneCameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    /** Inverse l'état de la lampe (toggle pilote). */
    public void toggle() {
        setOn(!on);
    }

    /** Définit la portée réelle de la lampe en mètres (pilotée par le LightSensor). */
    public void setRange(float range) {
        this.range = Math.max(0f, range);
        appliquerEtat();
    }

    public float getRange() {
        return range;
    }

    /** Applique l'état courant (on/off + portée) à la SpotLight. */
    private void appliquerEtat() {
        if (on) {
            spot.setColor(COULEUR);
            spot.setSpotRange(range);
        } else {
            spot.setColor(ColorRGBA.BlackNoAlpha);
            spot.setSpotRange(0f);
        }
    }

    /** Retire la lampe de la scène (à la destruction du drone). */
    public void dispose() {
        rootNode.removeLight(spot);
    }
}