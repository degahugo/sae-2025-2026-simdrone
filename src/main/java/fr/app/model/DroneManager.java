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
    private DroneController droneController;
    private DroneCameraManager frontCameraManager;
    private DroneCameraManager rearCameraManager;
    private CurrentMatrix currentMatrix;
    private Camera cam;
    private boolean showingFrontCam = true;
    private Drone drone;
    private WorldView model;

    public DroneManager(AssetManager assetManager, Node rootNode, PhysicsEngine physicsEngine, InputManager inputManager, Camera cam, WorldView worldView, CurrentMatrix currentMatrix){
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.physicsEngine = physicsEngine;
        this.inputManager = inputManager;
        this.cam = cam;
        this.model = worldView;
        this.currentMatrix = currentMatrix;
    }

    public void CreateDrone(String name){
        String droneName;
        // Camera avant
        Vector3f camOffset;
        float maxYaw;
        float maxPitch;
        // Camera arrière pour le Guardian
        Vector3f rearCamOffset;
        float maxRearYaw;
        float maxRearPitch;
        // On met le bon nom de drone
        // On règle l'offset de la caméra pour bien la placer
        // On définit les limites de rotation de la caméra
        if(name.equals("Guardian")){
            droneName = "subseatech/guardian/guardian-vehicle.j3o";
            camOffset = new Vector3f(0, 0, 0.4f);
            maxYaw = 90f;
            maxPitch = 90f;
            rearCamOffset = new Vector3f(0, 0, -0.4f);
            maxRearYaw = 0f;
            maxRearPitch = 90f;
        } else{
            droneName = "bluerobotics/br2r4/br2-r4-vehicle.j3o";
            camOffset = new Vector3f(0, 0, 0.205f);
            maxYaw = 0f;
            maxPitch = 90f;
            // Pas de camera arrière alors on initialise à 0
            // On doit initialiser pour que ça compile à cause de la suite du code
            rearCamOffset = new Vector3f(0, 0, 0);
            maxRearYaw = 0f;
            maxRearPitch = 0f;
        }
        Node droneNode = new Node("Drone");
        Spatial droneModel = assetManager.loadModel("vehicle/" + droneName);
        // Empêche la caméra de voir le drone
        droneModel.setQueueBucket(Bucket.Gui);
        droneNode.attachChild(droneModel);
        droneNode.move(0f, 1f, 0f);
        rootNode.attachChild(droneNode);
        drone = new Drone("drone1", droneNode);
        this.physicsEngine.CreateDroneHitbox(drone, name);
        this.droneController = new DroneController(inputManager, drone, model, currentMatrix);
        this.droneController.setUpKeys();
        this.frontCameraManager = new DroneCameraManager(cam, drone, false);
        // On applique l'offset de la caméra pour bien la placer et ses limites de rotation
        this.frontCameraManager.setCameraOffset(camOffset);
        this.frontCameraManager.setMaxYaw(maxYaw);
        this.frontCameraManager.setMaxPitch(maxPitch);
        if(name.equals("Guardian")){
            Camera rearCam = this.cam.clone();
            this.rearCameraManager = new DroneCameraManager(rearCam, drone, true);
            this.rearCameraManager.setCameraOffset(rearCamOffset);
            this.rearCameraManager.setMaxYaw(maxRearYaw);
            this.rearCameraManager.setMaxPitch(maxRearPitch);
            this.rearCameraManager.setActive(false);
        } else{
            this.rearCameraManager = null;
        }
    }

    public void switchCamera() {
        if (rearCameraManager == null) {
            System.out.println("Pas de caméra arrière sur ce drone");
            return;
        }
        if (showingFrontCam) {
            // On éteint la cam avant et on allume la cam arrière
            frontCameraManager.setActive(false);
            rearCameraManager.setActive(true);
            showingFrontCam = false;
            System.out.println("Passage en vue arrière");
        } else{
            // On allume la cam avant et on éteint la cam arrière
            frontCameraManager.setActive(true);
            rearCameraManager.setActive(false);
            showingFrontCam = true;
            System.out.println("Passage en vue avant");
        }
    }

    public Drone getDrone(){
        return this.drone;
    }

    public DroneCameraManager getFrontCameraManager(){ return this.frontCameraManager; }

    public DroneCameraManager getRearCameraManager(){ return this.rearCameraManager; }

    public boolean isShowingFrontCam(){ return showingFrontCam; }

    public DroneController getDroneController(){
        return this.droneController;
    }
}
