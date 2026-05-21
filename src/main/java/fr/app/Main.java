package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.*;
import com.jme3.system.AppSettings;
import fr.app.model.DroneManager;
import fr.app.model.PhysicsEngine;
import fr.app.model.WorldView;
import fr.app.view.CameraManager;
import fr.app.view.GUIManager;
import fr.app.view.CameraManager;
import fr.app.model.sensor.*;
import fr.app.view.LightingManager;
import fr.app.view.WaterFilterManager;
import fr.app.controller.DroneController;
import fr.app.model.Drone;
import fr.app.view.DroneCameraManager;
import fr.app.view.WaterDistortionManager;
import fr.app.model.PilotCommand;
import fr.app.model.DroneStatus;
import com.jme3.math.Vector3f;


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
    private Sensor sensor;
    private TemperatureSensor temperatureSensor;
    private boolean debugMode = false;
    private boolean loading = true;
    private float lastTemperature = Float.NaN; // dernière température enregistré

    private volatile boolean initialized = false;
    private DepthSensor depthSensor;
    private PressureSensor pressureSensor;


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

        flyCam.setEnabled(false);
        // Réduit la distance minimale d'affichage de la caméra pour ne plus voir à travers les murs ou le sol en s'approchant
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
        inputManager.setCursorVisible(true);
        setPauseOnLostFocus(false);

        PhysicsEngine physicsEngine = new PhysicsEngine(this, this.model, this.guiManager, debugMode);

        // Configure scene
        this.lightingManager = new LightingManager(rootNode, cam, assetManager);
        this.lightingManager.initLighting();
        // Filter processor
        fpp = new FilterPostProcessor(assetManager);
        this.droneManager = new DroneManager(assetManager,rootNode,physicsEngine,inputManager,cam, model);

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
            droneManager.CreateDrone("BR2R4");
            // Gets the manager and controller te be used here
            this.droneCameraManager = droneManager.getDroneCameraManager();
            this.droneController = droneManager.getDroneController();
            // Adding captors
            // Getting the drone
            this.drone = droneManager.getDrone();
            // Then adding the sensors
            this.temperatureSensor = new TemperatureSensor("TempSensor");
            this.drone.addSensor(temperatureSensor);
            this.depthSensor = new DepthSensor("DepthSensor");
            drone.addSensor(this.depthSensor);
            CompassSensor compassSensor = new CompassSensor("CompassSensor");
            drone.addSensor(compassSensor);
            this.pressureSensor = new PressureSensor("PressureSensor", this.depthSensor);
            drone.addSensor(this.pressureSensor);
            // Creates the water filter
            WaterFilterManager waterFilterManager = new WaterFilterManager(fpp, viewPort, rootNode, model);
            this.waterDistortionManager = new WaterDistortionManager(fpp, viewPort, droneManager.getDrone(), model);

            // End of the loading, will execute the simpleUpdate
            loading = false;
            initialized = true;
        });
        // Attach scene to node
        rootNode.attachChild(sceneNode);
        //this.initialized = true;
        //System.out.println("[Simulation] Initialisation terminée.");

    }
    public boolean isInitialized() {
        return initialized;
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

        for (Sensor sensor : drone.getSensors()) {
            sensor.update(drone);
            if (sensor instanceof TemperatureSensor) {
                TemperatureSensor ts = (TemperatureSensor) sensor;
                ts.affectDrone(drone);
                /*System.out.println(
                        "Température : " + ts.getTemperature()
                );*/
                float temp = ts.getTemperature();
                if (Math.abs(temp - lastTemperature) > 0.01f) {
                    lastTemperature = temp;
                    System.out.println("Température : " + temp);
                }

            }
            if (sensor instanceof DepthSensor) {
                DepthSensor ds = (DepthSensor) sensor;
                System.out.println("Profondeur : " + ds.getDepth() + " m");
            }
            if (sensor instanceof CompassSensor cs) {
                System.out.println("Cap : " + cs.getHeading() + "°");
            }
            if (sensor instanceof PressureSensor cs) {
                System.out.println("Pression : " + cs.getPressure() + "bar");
            }
            if (sensor instanceof TemperatureSensor ts) {
                System.out.println("Temperature: " + ts.getTemperature() + "°C");
            }
        }

        if (drone.getBattery() != null) {
            float consumption = 0.05f * tpf;
            //Donc plus le temps passe,plus la batterie diminue
            //La batterie commence à diminuer dès que l'on active le main
            //Donc lorsqu'on commence à voir les données un peu de batterie a été consommé lors du lancement de l'application


                drone.getBattery().consommation(consumption);
                //retire de l'énergie à la batterie
                System.out.println(
                        "Batterie : " + drone.getBattery().getPercentage() + "%"
                );

        }


    }

    public void executeNetworkCommand(String droneId, PilotCommand command) {

        if (drone == null || droneController == null) {
            return;
        }

        if (!drone.getId().equals(droneId)) {
            System.out.println("[Simulation] Drone inconnu : " + droneId);
            return;
        }

        droneController.executeCommand(command,true);

        System.out.println("[Simulation] Commande appliquée à " + droneId + " : " + command);
    }


    public DroneStatus getDroneStatus(String droneId) {

        DroneStatus status = new DroneStatus();

        if (drone == null || !drone.getId().equals(droneId)) {
            status.setState("Drone inconnu");
            return status;
        }

        Vector3f position = drone.getDroneSpatial().getLocalTranslation();

        status.setX(position.x);
        status.setY(position.y);
        status.setZ(position.z);
        status.setSpeed(0.0);
        status.setState("En vol");

        if (temperatureSensor != null) {
            status.setTemperature(temperatureSensor.getTemperature());
        }
        if (waterDistortionManager != null) {
            status.setDepth(waterDistortionManager.getProfondeur());
            status.setIntensity(waterDistortionManager.getIntensity());
        }

        return status;
    }

}
