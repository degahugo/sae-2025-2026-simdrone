package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.*;
import com.jme3.system.AppSettings;
import com.jme3.water.WaterFilter;
import fr.app.controller.DroneCameraController;
import fr.app.model.environment.EnvironmentConditions;
import fr.app.model.environment.EnvironmentConditionsManager;
import fr.app.model.*;
import fr.app.view.*;
import fr.app.model.DroneManager;
import fr.app.model.PhysicsEngine;
import fr.app.model.WorldView;
import fr.app.view.camera.CameraManager;
import fr.app.view.GUIManager;
import fr.app.model.sensor.*;
import fr.app.model.sensor.Sensor;
import fr.app.model.sensor.TemperatureSensor;
import fr.app.view.LightingManager;
import fr.app.view.WaterFilterManager;
import fr.app.controller.DroneController;
import fr.app.model.Drone;
import fr.app.view.camera.DroneCameraManager;
import fr.app.view.WaterDistortionManager;
import fr.app.model.PilotCommand;
import fr.app.model.DroneStatus;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import fr.app.model.SimulationConfiguration;
import java.util.concurrent.atomic.AtomicReference;
import fr.app.view.DroneFrameCapture;
import java.util.concurrent.ConcurrentHashMap;


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
    private DroneCameraManager frontCameraManager;
    private DroneCameraManager rearCameraManager;
    private DroneCameraController droneCameraController;
    private WaterDistortionManager waterDistortionManager;
    private DroneManager droneManager;
    private FilterPostProcessor fpp;
    private Sensor sensor;
    private WaterFilterManager waterFilterManager;
    private WaterFilter waterFilter;
    private TemperatureSensor temperatureSensor;
    private EnvironmentConditionsManager environmentConditionsManager;

    // Variables pour le courant
    private CurrentMatrix currentMatrix;
    private CurrentDebugView currentDebugView; // On garde une référence globale
    private boolean showCurrentDebug = false; // Désactivé par défaut
    private float currentTimer = 0f;
    private final float TIME_BETWEEN_UPDATES = 5.0f;
    private EnvironmentConditions environmentConditions;
    private EnvironmentConditions environmentVisibility;

    private boolean showMeshWireframe = false; // Affiche le mesh
    private boolean loading = true; // Indique si la map a chargé
    private float lastTemperature = Float.NaN; // dernière température enregistrée
    private Camera pilotCamera;
    private volatile boolean initialized = false;
    private DepthSensor depthSensor;
    private PressureSensor pressureSensor;
    private SimulationConfiguration configuration;
    private CompassSensor compassSensor;
    // Statut mis à jour à chaque simpleUpdate() — thread-safe pour lecture externe
    private final AtomicReference<DroneStatus> dernierStatut = new AtomicReference<>(new DroneStatus());
    // Captures vidéo par droneId
    private final ConcurrentHashMap<String, DroneFrameCapture> frameCaptures = new ConcurrentHashMap<>();
    private SensorManager sensorManager;


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
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.05f, 1000f);
        inputManager.setCursorVisible(true);
        setPauseOnLostFocus(false);
        PhysicsEngine physicsEngine = new PhysicsEngine(this, this.model, this.guiManager, showMeshWireframe);

        int terrainId = 1;
        environmentConditions = EnvironmentConditions.NORMAL_CURRENT;
        environmentVisibility = EnvironmentConditions.VERY_GOOD_VISIBILITY;

        // On peut probablement faire plus propre avec un catch plutôt que rajouter 15 conditions
        if (configuration != null
                && configuration.getEnvironmentType() != null
                && configuration.getCondition() != null
                && configuration.getVisibility() != null) {
            terrainId = configuration.getEnvironmentType().getTerrainId();
            environmentConditions = configuration.getCondition();
            environmentVisibility = configuration.getVisibility();
        }
        System.out.println("Environnement choisi : " + terrainId);
        System.out.println("Conditions choisi : " + environmentConditions);
        System.out.println("Visibilité choisi : " + environmentVisibility);
        BitmapText testText = guiManager.CreateLIGUI("Environnement choisi : " + terrainId +
                                    "\nConditions choisi : " + environmentConditions +
                                    "\nVisibilité choisi : " + environmentVisibility,
                                    new Vector3f(0,200,0));
        // Generation des courants
        this.environmentConditionsManager = new EnvironmentConditionsManager();
        environmentConditionsManager.InitiateEnvironmentConditions(terrainId, environmentConditions, environmentVisibility);
        this.currentMatrix = environmentConditionsManager.getCurrentMatrix();

        // Configure scene
        this.lightingManager = new LightingManager(rootNode, cam, assetManager);
        this.lightingManager.initLighting();
        // Filter processor
        fpp = new FilterPostProcessor(assetManager);
        this.droneManager = new DroneManager(assetManager,rootNode,physicsEngine,inputManager,cam, model, currentMatrix);

        //this.cameraManager = new CameraManager(cam, flyCam);
        // Set false to disable flying camera
        //this.cameraManager.configureCamera(false);

        // Creates scene and with a callback add the rest of the physics objects
        Node sceneNode = physicsEngine.CreateTerrainScene(terrainId, () -> {            // Only executes when the terrain is ready
            // Add all physics objects here to avoid issues with the terrain not being finished
            System.out.println("Terrain ready : adding the CUBES");
            //physicsEngine.SpawnTestCube(rootNode, -4f, 15f, 0f, ColorRGBA.Red);
            //physicsEngine.SpawnTestCube(rootNode, 0f, 2f, 0f, ColorRGBA.Yellow);
            // Create a drone
            String droneName = "Guardian";

            if (configuration != null && configuration.getDroneType() != null) {
                droneName = configuration.getDroneType().getSimulatorName();
            }

            droneManager.CreateDrone(droneName);            // Gets the manager and controller te be used here
            this.frontCameraManager = droneManager.getFrontCameraManager();
            this.rearCameraManager = droneManager.getRearCameraManager();
            this.droneController = droneManager.getDroneController();
            this.droneCameraController = new DroneCameraController(frontCameraManager, rearCameraManager, droneController, droneManager);
            // Adding captors
            // Getting the drone
            this.drone = droneManager.getDrone();
            // Creates the water filter
            this.waterFilterManager = new WaterFilterManager(fpp, viewPort, rootNode, model);
            this.waterFilter = waterFilterManager.getWaterFilter();
            this.waterDistortionManager = new WaterDistortionManager(fpp, viewPort, droneManager.getDrone(), model);
            // Ajoute les filtres au viewport
            viewPort.addProcessor(fpp);
            // Then adding the sensors
            this.sensorManager = new SensorManager(drone, lightingManager, waterFilterManager);
            sensorManager.initSensors();
            // Affichage des flèches de courant
            this.currentDebugView = new CurrentDebugView(currentMatrix, assetManager, rootNode);
            currentDebugView.setVisible(showCurrentDebug);
            if(showCurrentDebug) currentDebugView.showCurrentGrid(0.5f);
            guiManager.RemoveLIGUI(testText);

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
    public void simpleUpdate(float tpf) {
        if (loading) {
            return;
        }
        if (droneController != null) {
            droneController.updateMovement(tpf);
        }
        if(droneCameraController != null){
            droneCameraController.update(tpf);
        }
        // --- Free-Look Camera --- de fatou, je ne sais pas ce que ça fait du coup j'ai commenté
        /*if (droneController != null && droneCameraManager != null) {

            if (droneController.isCamLeftIntention()) {
                droneCameraManager.rotateCameraLeft(tpf);
            }
            if (droneController.isCamRightIntention()) {
                droneCameraManager.rotateCameraRight(tpf);
            }
            if (droneController.isCamUpIntention()) {
                droneCameraManager.rotateCameraUp(tpf);
            }
            if (droneController.isCamDownIntention()) {
                droneCameraManager.rotateCameraDown(tpf);
            }
        }
        if (droneCameraManager != null) {
            droneCameraManager.update(tpf);
        }*/
        if (waterDistortionManager != null) {
            waterDistortionManager.update();
        }

        if (sensorManager != null) {
            sensorManager.updateSensors();}
        if (currentMatrix != null) {
            currentTimer += tpf; // On cumule le temps écoulé depuis la dernière frame
            if (currentTimer >= TIME_BETWEEN_UPDATES) {
                // Quand le temps est écoulé, on change les courants
                currentMatrix.generateRandomCurrents();
                // Si le mode debug est actif, on force la vue à redessiner les flèches de courants
                if (showCurrentDebug && currentDebugView != null) {
                    currentDebugView.showCurrentGrid(0.5f);
                }
                currentTimer = 0f; // On réinitialise le compte à rebours
            }
        }

        // Mettre à jour le statut en cache — thread-safe, lu par getDroneStatusDirect()
        if (drone != null && initialized) {
            DroneStatus s = new DroneStatus();
            com.jme3.math.Vector3f pos = drone.getDroneSpatial().getLocalTranslation();
            s.setX(pos.x);
            s.setY(pos.y);
            s.setZ(pos.z);
            s.setSpeed(0.0);
            s.setState("En vol");
            if (temperatureSensor != null) s.setTemperature(temperatureSensor.getTemperature());
            if (waterDistortionManager != null) {
                s.setDepth(waterDistortionManager.getProfondeur());
                s.setIntensity(waterDistortionManager.getIntensity());
            }
            if (drone.getBattery() != null) {
                s.setBattery(drone.getBattery().getPercentage());
            }
            if (pressureSensor != null) {
                s.setPressure(pressureSensor.getPressure());
            }
            if (compassSensor != null) {
                s.setHeading(compassSensor.getHeading());
                s.setYaw(compassSensor.getHeading());
            }

            s.setPitch(drone.getPitch());
            s.setYaw(drone.getYaw());
            s.setRoll(drone.getRoll());
            s.setHeading(drone.getHeading());

            s.setCollisionDetected(drone.isCollisionDetected());
            s.setCollisionMessage(drone.getCollisionMessage());


            s.setCollisionDetected(drone.isCollisionDetected());

            if (drone.isCollisionDetected()) {
                s.setCollisionMessage("Collision détectée");
            } else {
                s.setCollisionMessage("Aucune collision");
            }


            dernierStatut.set(s);
        }

        if (drone.getBattery() != null) {
            float consumption = 0.05f * tpf;
            //Donc plus le temps passe,plus la batterie diminue
            //La batterie commence à diminuer dès que l'on active le main
            //Donc lorsqu'on commence à voir les données un peu de batterie a été consommé lors du lancement de l'application
            drone.getBattery().consommation(consumption);
            //retire de l'énergie à la batterie
            //System.out.println("Batterie : " + drone.getBattery().getPercentage() + "%");
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

        if (compassSensor != null) {
            status.setHeading(compassSensor.getHeading());
            status.setYaw(compassSensor.getHeading());
        }

        if (pressureSensor != null) {
            status.setPressure(pressureSensor.getPressure());
        }

        if (drone.getBattery() != null) {
            status.setBattery(drone.getBattery().getPercentage());
        }

        if (configuration != null) {
            if (configuration.getEnvironmentType() != null) {
                status.setEnvironmentName(configuration.getEnvironmentType().toString());
            }

            if (configuration.getDroneType() != null) {
                status.setDroneType(configuration.getDroneType().toString());
            }
        }


        status.setPitch(drone.getPitch());
        status.setYaw(drone.getYaw());
        status.setRoll(drone.getRoll());
        status.setHeading(drone.getHeading());


        status.setCollisionDetected(drone.isCollisionDetected());
        status.setCollisionMessage(drone.getCollisionMessage());

        return status;
    }

    /**
     * Retourne le dernier statut mis en cache par simpleUpdate().
     * NON BLOQUANT — thread-safe via AtomicReference.
     */
    public DroneStatus getDroneStatusDirect(String droneId) {
        if (drone == null || !drone.getId().equals(droneId)) {
            DroneStatus s = new DroneStatus();
            s.setState(drone == null ? "En initialisation..." : "Drone inconnu");
            return s;
        }
        return dernierStatut.get();
    }

    /**
     * Crée le ViewPort off-screen pour la capture vidéo du drone.
     * DOIT être appelé depuis le thread JME3 (via enqueue).
     */
    public void creerViewPortOffScreen(String droneId) {
        if (frontCameraManager == null) {
            System.err.println("[Main] creerViewPortOffScreen : droneCameraManager null pour " + droneId);
            return;
        }
        if (frameCaptures.containsKey(droneId)) {
            System.out.println("[Main] ViewPort déjà créé pour " + droneId);
            return;
        }
        com.jme3.renderer.Camera cameraDrone = frontCameraManager.getCamera();
        DroneFrameCapture capture = new DroneFrameCapture(droneId, cameraDrone, renderManager, rootNode, guiNode,assetManager,model,drone);
        frameCaptures.put(droneId, capture);
        System.out.println("[Main] ViewPort off-screen créé pour " + droneId);
    }

    /**
     * Retourne la dernière frame JPEG — NON BLOQUANT (AtomicReference dans DroneFrameCapture).
     */
    public byte[] capturerFrameDrone(String droneId) {
        DroneFrameCapture capture = frameCaptures.get(droneId);
        if (capture == null && frameCaptures.size() == 1) {
            capture = frameCaptures.values().iterator().next();
        }
        return capture != null ? capture.capturerFrame() : null;
    }

    /** Stub de compatibilité avec UdpServeurTransport. */
    public void creerDrone(String droneId) {
        System.out.println("[Main] creerDrone stub pour " + droneId);
    }
    public void setConfiguration(SimulationConfiguration configuration) {
        this.configuration = configuration;
    }
}
