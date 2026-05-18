package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.*;
import com.jme3.system.AppSettings;
import fr.app.model.DroneManager;
import fr.app.model.PhysicsEngine;
import fr.app.model.WorldView;
import fr.app.view.CameraManager;
import fr.app.view.GUIManager;
import fr.app.view.LightingManager;
import fr.app.view.WaterFilterManager;
import fr.app.controller.DroneController;
import fr.app.model.Drone;
import fr.app.view.DroneCameraManager;
import fr.app.view.WaterDistortionManager;


/**
 * Main class to launch the simulation
 */
public class Main extends SimpleApplication {

    private WorldView model;
    private LightingManager lightingManager;
    private CameraManager cameraManager;
    private Node cubesNode = new Node("CubesNode");
    private GUIManager guiManager;
    private Drone drone;
    private DroneController droneController;
    private DroneCameraManager droneCameraManager;
    private WaterDistortionManager waterDistortionManager;
    private DroneManager droneManager;
    private FilterPostProcessor fpp;
    private boolean debugMode = false;
    private boolean loading = true;


    public static void main(String[] args) {

        AppSettings settings=new AppSettings(true);
        settings.setWidth(1024);
        settings.setHeight(768);
        settings.setVSync(false);
        settings.setFrameRate(-1);

        Main app = new Main();
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Initialise model
        this.model = new WorldView();
        this.guiManager = new GUIManager(assetManager, settings, guiNode);

        PhysicsEngine physicsEngine = new PhysicsEngine(this, this.model, this.guiManager, debugMode);

        // Configure scene
        this.lightingManager = new LightingManager(rootNode, cam, assetManager);
        this.lightingManager.initLighting();
        // Filter processor
        this.fpp = new FilterPostProcessor(assetManager);
        this.droneManager = new DroneManager(assetManager,rootNode,physicsEngine,inputManager,cam);

        //this.cameraManager = new CameraManager(cam, flyCam);
        // Set false to disable flying camera
        //this.cameraManager.configureCamera(false);

        // Creates scene and with a callback add the rest of the physics objects
        Node sceneNode = physicsEngine.CreateTerrainScene(1, () -> {
            // Only executes when the terrain is ready
            // Add all physics objects here to avoid issues with the terrain not being finished
            System.out.println("Terrain ready : adding the CUBES");
            //physicsEngine.SpawnTestCube(rootNode, -4f, 15f, 0f, ColorRGBA.Red);
            //physicsEngine.SpawnTestCube(rootNode, 0f, 2f, 0f, ColorRGBA.Yellow);
            // Create a drone

/*
            Node droneNode = new Node("Drone");
            //String droneName = "bluerobotics/br2r4/br2-r4-vehicle.j3o";
            String droneName = "subseatech/guardian/guardian-vehicle.j3o";
            Spatial droneModel = assetManager.loadModel("vehicle/" + droneName);
            droneNode.attachChild(droneModel);
            droneNode.move(0f, 1f, 0f);
            rootNode.attachChild(droneNode);
            this.drone = new Drone(droneNode);
            physicsEngine.CreateDroneHitbox(drone, "Guardian");
            this.droneController = new DroneController(inputManager, drone);
            this.droneController.setUpKeys();
            this.droneCameraManager = new DroneCameraManager(cam, drone);
*/
            droneManager.CreateDrone("BR2R4");
            // Gets the manager and controller te be used here
            droneCameraManager = droneManager.getDroneCameraManager();
            droneController = droneManager.getDroneController();
            // Creates the water filter
            //WaterFilterManager waterFilterManager = new WaterFilterManager(fpp, viewPort, rootNode, model);
            //this.waterDistortionManager = new WaterDistortionManager(fpp, viewPort, drone, model);

            // End of the loading, will execute the simpleUpdate
            loading = false;
        });
        // Attach scene to node
        rootNode.attachChild(sceneNode);
    }

    @Override
    public void simpleUpdate(float tpf){
        if (loading) {
            return;
        }
        if (droneController != null){
            droneController.updateMovement();
        }
        if (droneCameraManager != null){
            droneCameraManager.update(tpf);
        }
        if (waterDistortionManager != null){
            waterDistortionManager.update();
        }
    }
}
