package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.*;
import com.jme3.system.AppSettings;
import fr.app.model.PhysicsEngine;
import fr.app.model.WorldView;
//import fr.app.view.CameraManager;
import fr.app.model.sensor.*;
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
    //private CameraManager cameraManager;
    private Drone drone;
    private DroneController droneController;
    private DroneCameraManager droneCameraManager;
    private WaterDistortionManager waterDistortionManager;
    private Sensor sensor;
    private TemperatureSensor temperatureSensor;
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

        PhysicsEngine physicsEngine = new PhysicsEngine(this, this.model);

        // Configure scene
        this.lightingManager = new LightingManager(rootNode, cam, assetManager);
        this.lightingManager.initLighting();

        //this.cameraManager = new CameraManager(cam, flyCam);
        // Set false to disable flying camera
        //this.cameraManager.configureCamera(false);

        // Creates scene
        Node sceneNode = physicsEngine.createTerrainScene();
        // Attach scene to node
        rootNode.attachChild(sceneNode);


        Node droneNode = new Node("Drone");

        Spatial droneModel = assetManager.loadModel("vehicle/bluerobotics/br2r4/br2-r4-vehicle.j3o");

        droneNode.attachChild(droneModel);

        droneNode.move(0f, 1f, 0f);

        sceneNode.attachChild(droneNode);

        this.drone = new Drone(droneNode);
        // Capteur de température
        this.temperatureSensor = new TemperatureSensor("TempSensor");
        drone.addSensor(temperatureSensor);
        // Capteur de profondeur
        DepthSensor depthSensor = new DepthSensor("DepthSensor");
        drone.addSensor(depthSensor);
        // Capteur de compas
        CompassSensor compassSensor = new CompassSensor("CompassSensor");
        drone.addSensor(compassSensor);
        // Capteur de pression
        PressureSensor pressureSensor = new PressureSensor("PressureSensor", depthSensor);
        drone.addSensor(pressureSensor);




        this.droneController = new DroneController(inputManager, drone);
        this.droneController.setUpKeys();
        this.droneCameraManager = new DroneCameraManager(cam, drone);


        // Creates the water filter
        WaterFilterManager waterFilterManager = new WaterFilterManager(assetManager, viewPort, rootNode, model);
        this.waterDistortionManager = new WaterDistortionManager(assetManager, viewPort, drone, model);
        // Add tests cubes to test collisions, should be commented or removed later
        physicsEngine.spawnTestCube(0, 10f, 0, ColorRGBA.Red);
        physicsEngine.spawnTestCube(0, 2f, 0, ColorRGBA.Yellow);

    }

    @Override
    public void simpleUpdate(float tpf){

        if (droneController != null){
            droneController.updateMovement(tpf);
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
                System.out.println("Température : " + ts.getTemperature());
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

    }
}