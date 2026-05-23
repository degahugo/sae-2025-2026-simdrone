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
 * - Compatible avec les offsets utilisés dans develop
 */
public class DroneCameraManager {

    private final Camera cam;
    private final Drone drone;
    private final Node cameraNode;

    // Offset caméra (par défaut : derrière + légèrement au-dessus)
    // develop utilisait (0,0,0) → on garde ton offset mais on permet de le changer
    private Vector3f cameraOffset = new Vector3f(0f, 1.2f, -3f);

    // Free-look : rotation manuelle
    private float cameraYaw = 0f;
    private float cameraPitch = 0f;

    // Limites du pitch
    private static final float MAX_PITCH = 1.2f;
    private static final float MIN_PITCH = -1.2f;

    // Vitesse de rotation du free-look
    private static final float ROTATION_SPEED = 1.5f;

    public DroneCameraManager(Camera cam, Drone drone) {
        this.cam = cam;
        this.drone = drone;

        this.cameraNode = new Node("CameraNode_" + drone.getId());
        drone.getDroneSpatial().attachChild(cameraNode);

        // Offset initial (compatible avec develop)
        cameraNode.setLocalTranslation(cameraOffset);
    }

    // --- Contrôles Free-Look ---
    public void rotateCameraLeft(float tpf) {
        cameraYaw += ROTATION_SPEED * tpf;
    }

    public void rotateCameraRight(float tpf) {
        cameraYaw -= ROTATION_SPEED * tpf;
    }

    public void rotateCameraUp(float tpf) {
        cameraPitch += ROTATION_SPEED * tpf;
        cameraPitch = Math.min(cameraPitch, MAX_PITCH);
    }

    public void rotateCameraDown(float tpf) {
        cameraPitch -= ROTATION_SPEED * tpf;
        cameraPitch = Math.max(cameraPitch, MIN_PITCH);
    }

    /**
     * Mise à jour de la caméra :
     * - Position = position du cameraNode (offset par rapport au drone)
     * - Rotation = rotation du drone + free-look
     */
    public void update(float tpf) {

        // Rotation complète du drone
        Quaternion droneRotation = drone.getDroneSpatial().getWorldRotation();

        // Rotation manuelle du free-look
        Quaternion manualRotation = new Quaternion()
                .fromAngles(cameraPitch, cameraYaw, 0f);

        // Rotation finale = drone + free-look
        Quaternion finalRotation = droneRotation.mult(manualRotation);

        // Position de base : node attaché au drone
        Vector3f basePosition = cameraNode.getWorldTranslation();

        cam.setLocation(basePosition);
        cam.setRotation(finalRotation);
    }

    /** Change l’offset de la caméra (compatible develop) */
    public void setCameraOffset(Vector3f offset){
        this.cameraOffset = offset;
        this.cameraNode.setLocalTranslation(offset);
        System.out.println("Set camera offset to x : " + offset.x + " y : " + offset.y + " z : " + offset.z);
    }

    /** Caméra JME3 utilisée par DroneFrameCapture */
    public Camera getCamera() {
        return cam;
    }
}
