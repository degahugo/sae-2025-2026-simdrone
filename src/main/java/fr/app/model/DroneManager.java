package fr.app.model;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import fr.app.controller.DroneController;
import fr.app.view.DroneCameraManager;

public class DroneManager {
    private AssetManager assetManager;
    private Node rootNode;
    private PhysicsEngine physicsEngine;
    private InputManager inputManager;
    private CurrentMatrix currentMatrix;
    private Camera cam;
    private WorldView model;

    private int droneCount=0;

    public DroneManager(AssetManager assetManager, Node rootNode, PhysicsEngine physicsEngine, InputManager inputManager, Camera cam, WorldView worldView, CurrentMatrix currentMatrix){
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.physicsEngine = physicsEngine;
        this.inputManager = inputManager;
        this.cam = cam;
        this.model = worldView;
        this.currentMatrix = currentMatrix;
    }

    //  Instance retournée à chaque création de drone
    public static class DroneInstance{
        public final Drone drone;
        public final DroneController controller;
        public final DroneCameraManager cameraManager;

        public DroneInstance(Drone drone, DroneController controller, DroneCameraManager cameraManager){
            this.drone = drone;
            this.controller = controller;
            this.cameraManager = cameraManager;
        }
    }

    public DroneInstance createDrone(String modelName,String droneId){
        String droneName;
        Vector3f camOffset;
        if("BR2R4".equals(modelName)){
            droneName = "bluerobotics/br2r4/br2-r4-vehicle.j3o";
            camOffset = new Vector3f(0f, 0f, 0.205f);
        } else{
            droneName = "subseatech/guardian/guardian-vehicle.j3o";
            camOffset = new Vector3f(0f, 0f, 0.4f);
        }

        // Décalage de spawn à chaque drone
        float spawnOffsetX=droneCount*3.0f;
        droneCount+=1;

        Node droneNode = new Node("Drone_"+droneId);
        Spatial droneModel = assetManager.loadModel("vehicle/" + droneName);
        // Empêche la caméra de voir le drone
        droneModel.setQueueBucket(Bucket.Opaque);
        droneNode.attachChild(droneModel);
        droneNode.move(spawnOffsetX, 1f, 0f);
        rootNode.attachChild(droneNode);

        Drone drone = new Drone(droneId, droneNode);
        physicsEngine.CreateDroneHitbox(drone, modelName);

        DroneController controller = new DroneController(inputManager, drone, model, currentMatrix);
        //this.droneController.setUpKeys();
        DroneCameraManager cameraManager = new DroneCameraManager(cam, drone);
        cameraManager.setCameraOffset(camOffset);

        System.out.println("[DroneManager] Drone créé : id=" + droneId
                + "  modèle=" + modelName + "  spawnX=" + spawnOffsetX);

        return new DroneInstance(drone, controller, cameraManager);
    }

}
