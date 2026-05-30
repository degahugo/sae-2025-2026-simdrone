package fr.app.model;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import fr.app.controller.DroneController;
import fr.app.view.camera.DroneCameraManager;

public class DroneManager {
    private AssetManager assetManager;
    private Node rootNode;
    private PhysicsEngine physicsEngine;
    private InputManager inputManager;
    private CurrentMatrix currentMatrix;
    private Camera cam;
    private WorldView model;

    private int droneCount = 0;

    public DroneManager(AssetManager assetManager, Node rootNode, PhysicsEngine physicsEngine,
                        InputManager inputManager, Camera cam, WorldView worldView, CurrentMatrix currentMatrix){
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.physicsEngine = physicsEngine;
        this.inputManager = inputManager;
        this.cam = cam;
        this.model = worldView;
        this.currentMatrix = currentMatrix;
    }

    /**
     * Instance retournée à chaque création de drone.
     *
     * Porte le drone, son contrôleur, sa caméra avant et (optionnellement) sa
     * caméra arrière. Le basculement avant/arrière est désormais géré PAR DRONE
     * (et non globalement) afin de rester correct en multi-client.
     */
    public static class DroneInstance {
        public final Drone drone;
        public final DroneController controller;
        public final DroneCameraManager cameraManager;       // caméra avant (nom conservé de feature)
        public final DroneCameraManager rearCameraManager;   // null si le drone n'a pas de caméra arrière

        private boolean showingFrontCam = true;

        public DroneInstance(Drone drone, DroneController controller,
                             DroneCameraManager cameraManager, DroneCameraManager rearCameraManager){
            this.drone = drone;
            this.controller = controller;
            this.cameraManager = cameraManager;
            this.rearCameraManager = rearCameraManager;
        }

        public boolean hasRearCamera(){ return rearCameraManager != null; }

        public boolean isShowingFrontCam(){ return showingFrontCam; }

        /** Caméra actuellement affichée pour ce drone. */
        public DroneCameraManager getActiveCameraManager(){
            return showingFrontCam ? cameraManager : rearCameraManager;
        }

        /** Bascule entre caméra avant et arrière (sans effet si pas de cam arrière). */
        public void switchCamera(){
            if (rearCameraManager == null){
                System.out.println("Pas de caméra arrière sur ce drone");
                return;
            }
            if (showingFrontCam){
                cameraManager.setActive(false);
                rearCameraManager.setActive(true);
                showingFrontCam = false;
                System.out.println("Passage en vue arrière");
            } else {
                cameraManager.setActive(true);
                rearCameraManager.setActive(false);
                showingFrontCam = true;
                System.out.println("Passage en vue avant");
            }
        }
    }

    /**
     * Crée un drone (usine multi-client) et renvoie son instance complète.
     *
     * @param modelName modèle du drone ("BR2R4", sinon traité comme Guardian)
     * @param droneId   identifiant unique du drone
     */
    public DroneInstance createDrone(String modelName, String droneId){
        String droneName;
        Vector3f camOffset;
        float maxYaw;
        float maxPitch;

        // Caméra arrière : uniquement pour le Guardian
        boolean isGuardian = !"BR2R4".equals(modelName);
        Vector3f rearCamOffset = new Vector3f(0f, 0f, 0f);
        float maxRearYaw   = 0f;
        float maxRearPitch = 0f;

        if (!isGuardian){
            // BR2R4 : caméra avant seule
            droneName = "bluerobotics/br2r4/br2-r4-vehicle.j3o";
            camOffset = new Vector3f(0f, 0f, 0.205f);
            maxYaw    = 0f;
            maxPitch  = 90f;
        } else {
            // Guardian : caméra avant + caméra arrière
            droneName = "subseatech/guardian/guardian-vehicle.j3o";
            camOffset = new Vector3f(0f, 0f, 0.4f);
            maxYaw    = 90f;
            maxPitch  = 90f;
            rearCamOffset = new Vector3f(0f, 0f, -0.4f);
            maxRearYaw    = 0f;
            maxRearPitch  = 90f;
        }

        // Décalage de spawn pour ne pas empiler les drones les uns sur les autres
        float spawnOffsetX = droneCount * 3.0f;
        droneCount += 1;

        Node droneNode = new Node("Drone_" + droneId);
        Spatial droneModel = assetManager.loadModel("vehicle/" + droneName);
        droneModel.setQueueBucket(Bucket.Opaque);
        droneNode.attachChild(droneModel);
        droneNode.move(spawnOffsetX, 1f, 0f);
        rootNode.attachChild(droneNode);

        Drone drone = new Drone(droneId, droneNode);
        physicsEngine.CreateDroneHitbox(drone, modelName);

        DroneController controller = new DroneController(inputManager, drone, model, currentMatrix);
        // NB : setUpKeys() n'est PAS appelé ici. En multi-client on ne veut pas que
        // chaque drone capture le clavier global. Main peut l'appeler sur le drone
        // piloté localement via instance.controller.setUpKeys().

        // Caméra avant
        DroneCameraManager frontCam = new DroneCameraManager(cam, drone, false);
        frontCam.setCameraOffset(camOffset);
        frontCam.setMaxYaw(maxYaw);
        frontCam.setMaxPitch(maxPitch);

        // Caméra arrière (Guardian uniquement)
        DroneCameraManager rearCam = null;
        if (isGuardian){
            rearCam = new DroneCameraManager(cam, drone, true);
            rearCam.setCameraOffset(rearCamOffset);
            rearCam.setMaxYaw(maxRearYaw);
            rearCam.setMaxPitch(maxRearPitch);
            rearCam.setActive(false);
        }

        System.out.println("[DroneManager] Drone créé : id=" + droneId
                + "  modèle=" + modelName + "  spawnX=" + spawnOffsetX
                + (isGuardian ? "  (avec caméra arrière)" : ""));

        return new DroneInstance(drone, controller, frontCam, rearCam);
    }
}