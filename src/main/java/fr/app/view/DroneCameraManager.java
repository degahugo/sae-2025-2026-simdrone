package fr.app.view;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import fr.app.model.Drone;

/**
 * Gestion de la caméra embarquée du drone.
 * - Suit la rotation complète du drone (yaw, pitch, roll)
 * - Permet au pilote de regarder autour (free-look)
 */
public class DroneCameraManager {

    private final Camera cam;
    private final Drone drone;
    private final Node cameraNode;

    // Offset caméra : derrière + légèrement au-dessus
    private final Vector3f cameraOffset = new Vector3f(0f, 1.2f, -3f);

    // Free-look : rotation manuelle
    private float cameraYaw = 0f;
    private float cameraPitch = 0f;

    // Limites du pitch (évite de regarder derrière)
    private static final float MAX_PITCH = 1.2f;   // ~70°
    private static final float MIN_PITCH = -1.2f;  // ~-70°

    // Vitesse de rotation du free-look
    private static final float ROTATION_SPEED = 1.5f;

    /**
     * Constructeur du gestionnaire de caméra.
     *
     * @param cam   Caméra principale
     * @param drone Drone suivi
     */
    public DroneCameraManager(Camera cam, Drone drone) {
        this.cam = cam;
        this.drone = drone;

        this.cameraNode = new Node("CameraNode");
        drone.getDroneSpatial().attachChild(cameraNode);
    }

    // --- Contrôles Free-Look ---
    /**
     * Rotation manuelle vers la gauche.
     */
    public void rotateCameraLeft(float tpf) {
        cameraYaw += ROTATION_SPEED * tpf;
    }
    /**
     * Rotation manuelle vers la droite.
     */

    public void rotateCameraRight(float tpf) {
        cameraYaw -= ROTATION_SPEED * tpf;
    }
    /**
     * Rotation manuelle vers le haut.
     */

    public void rotateCameraUp(float tpf) {
        cameraPitch += ROTATION_SPEED * tpf;
        cameraPitch = Math.min(cameraPitch, MAX_PITCH);
    }
    /**
     * Rotation manuelle vers le haut.
     */

    public void rotateCameraDown(float tpf) {
        cameraPitch -= ROTATION_SPEED * tpf;
        cameraPitch = Math.max(cameraPitch, MIN_PITCH);
    }

    /**
     * Mise à jour de la caméra :
     * - Position = drone + offset transformé
     * - Rotation = rotation drone + free-look
     */
    public void update(float tpf) {

        // Rotation complète du drone
        Quaternion droneRotation = drone.getDroneSpatial().getWorldRotation();

        // Rotation manuelle du free-look
        Quaternion manualRotation = new Quaternion()
                .fromAngles(cameraPitch, cameraYaw, 0f);

        // Rotation finale = drone + free-look
        Quaternion finalRotation = droneRotation.mult(manualRotation);

        // Offset transformé par la rotation finale
        Vector3f rotatedOffset = finalRotation.mult(cameraOffset);

        // Position finale
        Vector3f cameraPosition =
                drone.getDroneSpatial().getWorldTranslation()
                        .add(rotatedOffset);

        cam.setLocation(cameraPosition);
        cam.setRotation(finalRotation);
    }
}
