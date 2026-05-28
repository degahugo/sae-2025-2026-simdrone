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
    private DroneCameraManager droneCameraManager;
    private CurrentMatrix currentMatrix;
    private Camera cam;
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
        Vector3f camOffset;
        float maxYaw;
        float maxPitch;
        // On met le bon nom de drone
        // On règle l'offset de la caméra pour bien la placer
        // On définit les limites de rotation de la caméra
        if(name=="BR2R4"){
            droneName = "bluerobotics/br2r4/br2-r4-vehicle.j3o";
            camOffset = new Vector3f(0f, 0f, 0.205f);
            maxYaw = 0f;
            maxPitch = 90f;
        } else{
            droneName = "subseatech/guardian/guardian-vehicle.j3o";
            camOffset = new Vector3f(0f, 0f, 0.4f);
            maxYaw = 90f;
            maxPitch = 90f;
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
        this.droneCameraManager = new DroneCameraManager(cam, drone);
        // On applique l'offset de la caméra pour bien la placer et ses limites de rotation
        this.droneCameraManager.setCameraOffset(camOffset);
        this.droneCameraManager.setMaxYaw(maxYaw);
        this.droneCameraManager.setMaxPitch(maxPitch);
    }

    public Drone getDrone(){
        return this.drone;
    }

    public DroneCameraManager getDroneCameraManager(){
        return this.droneCameraManager;
    }

    public DroneController getDroneController(){
        return this.droneController;
    }
}
