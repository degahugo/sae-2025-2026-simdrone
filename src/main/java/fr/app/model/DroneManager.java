package fr.app.model;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import fr.app.controller.DroneController;
import fr.app.view.CameraManager;
import fr.app.view.DroneCameraManager;

public class DroneManager {
    private AssetManager assetManager;
    private Node rootNode;
    private PhysicsEngine physicsEngine;
    private InputManager inputManager;
    private DroneController droneController;
    private DroneCameraManager droneCameraManager;
    private Camera cam;
    private Drone drone;
    private WorldView model;

    public DroneManager(AssetManager assetManager, Node rootNode, PhysicsEngine physicsEngine, InputManager inputManager, Camera cam, WorldView worldView){
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.physicsEngine = physicsEngine;
        this.inputManager = inputManager;
        this.cam = cam;
        this.model = worldView;
    }

    public void CreateDrone(String name){
        String droneName;
        if(name=="BR2R4"){
            droneName = "bluerobotics/br2r4/br2-r4-vehicle.j3o";
        } else{
            droneName = "subseatech/guardian/guardian-vehicle.j3o";
        }
        Node droneNode = new Node("Drone");
        Spatial droneModel = assetManager.loadModel("vehicle/" + droneName);
        droneNode.attachChild(droneModel);
        droneNode.move(0f, 1f, 0f);
        rootNode.attachChild(droneNode);
        drone = new Drone("drone1", droneNode);
        this.physicsEngine.CreateDroneHitbox(drone, name);
        this.droneController = new DroneController(inputManager, drone, model);
        this.droneController.setUpKeys();
        this.droneCameraManager = new DroneCameraManager(cam, drone);
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
