package fr.app.view.camera;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;

import com.jme3.scene.control.CameraControl;
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
    private CameraNode cameraNode;

    // Offset caméra (par défaut : derrière + légèrement au-dessus)
    // develop utilisait (0,0,0) → on garde ton offset mais on permet de le changer
    private Vector3f cameraOffset = new Vector3f(0f, 1.2f, -3f);

    // Free-look : rotation manuelle
    private float cameraYaw = 0f;
    private float cameraPitch = 0f;

    // Pour la caméra arrière
    private final boolean isRearCam;

    // Limites du pitch
    private static final float MAX_PITCH = 1.2f;
    private static final float MIN_PITCH = -1.2f;

    // Vitesse de rotation du free-look
    private static final float ROTATION_SPEED = 1.5f;

    // Variables différentes de celles de fatou parce que je ne sais pas ce qu'elle a fait
    private final float rotationSpeed = 0.5f;
    private float maxPitch = 90f * FastMath.DEG_TO_RAD;
    private float maxYaw = 90f * FastMath.DEG_TO_RAD;

    public DroneCameraManager(Camera cam, Drone drone, boolean isRearCam) {
        this.cam = cam;
        this.drone = drone;
        this.isRearCam = isRearCam;

        this.cameraNode = new CameraNode("CameraNode",this.cam);
        this.cameraNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        drone.getDroneSpatial().attachChild(this.cameraNode);

        // Offset initial (compatible avec develop)
        this.cameraNode.setLocalTranslation(this.cameraOffset);
        applyRotation();
    }

    // --- Contrôles Free-Look ---
    public void rotateCameraLeft(float tpf) {
        cameraYaw += rotationSpeed * tpf;
        cameraYaw = Math.min(cameraYaw, maxYaw);
        applyRotation();
        //cameraNode.rotate(0, rotationSpeed * tpf, 0);
    }

    public void rotateCameraRight(float tpf) {
        cameraYaw -= rotationSpeed * tpf;
        cameraYaw = Math.max(cameraYaw, -maxYaw);
        applyRotation();
        //cameraNode.rotate(0, -rotationSpeed * tpf, 0);
    }

    public void rotateCameraUp(float tpf) {
        cameraPitch -= rotationSpeed * tpf;
        // Bloquer l'angle vers le haut
        cameraPitch = Math.max(cameraPitch, -maxPitch);
        applyRotation();
        //cameraNode.rotate(-rotationSpeed * tpf, 0, 0);
    }

    public void rotateCameraDown(float tpf) {
        cameraPitch += rotationSpeed * tpf;
        // Bloquer l'angle vers le bas
        cameraPitch = Math.min(cameraPitch, maxPitch);
        applyRotation();
        //cameraNode.rotate(rotationSpeed * tpf, 0, 0);
    }

    /**
     * Applique la rotation calculé par les méthodes précédentes sur la caméra
     */
    private void applyRotation(){
        float finalYaw = isRearCam ? cameraYaw + FastMath.PI : cameraYaw;
        Quaternion localRotation = new Quaternion().fromAngles(cameraPitch, cameraYaw, 0f);
        cameraNode.setLocalRotation(localRotation);
    }

    /**
     * Mise à jour de la caméra :
     * - Position = position du cameraNode (offset par rapport au drone)
     * - Rotation = rotation du drone + free-look
     */
    public void update(float tpf) {
/*
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
        cam.setRotation(finalRotation);*/
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

    public CameraNode getCameraNode(){
        return cameraNode;
    }

    /**
     * Active ou désactive le rendu à l'écran pour cette caméra
     */
    public void setActive(boolean active) {
        if (active) {
            // On ajoute la caméra au node et on l'active
            this.cameraNode.setCamera(this.cam);
            this.cameraNode.setEnabled(true);
        } else {
            // On retire la caméra du node et on la désactive
            this.cameraNode.setCamera(null);
            this.cameraNode.setEnabled(false);
        }
    }

    /**
     * Définit l'angle maximum que la caméra peut prendre en assiette.
     * @param angle valeur par côté en degrés, donc si on met 90, la caméra pourra aller de 90° en haut et 90 en bas
     */
    public void setMaxPitch(float angle){
        this.maxPitch = angle * FastMath.DEG_TO_RAD;
    }

    /**
     * Définit l'angle maximum que la caméra peut prendre en lacet.
     * @param angle valeur par côté en degrés, donc si on met 90, la caméra pourra aller de 90° à gauche et 90 à droite
     */
    public void setMaxYaw(float angle){
        this.maxYaw = angle * FastMath.DEG_TO_RAD;
    }
}
