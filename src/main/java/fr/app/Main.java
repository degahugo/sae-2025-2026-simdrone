package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.*;
import com.jme3.system.AppSettings;
import fr.app.model.*;
import fr.app.view.*;
import fr.app.model.DroneManager;
import fr.app.model.DroneManager.DroneInstance;
import fr.app.model.PhysicsEngine;
import fr.app.model.WorldView;
//import fr.app.view.CameraManager;
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
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import fr.app.model.SimulationConfiguration;

import java.util.concurrent.atomic.AtomicReference;

import fr.app.view.DroneFrameCapture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Main class to launch the simulation
 */
public class Main extends SimpleApplication {

    private WorldView model;
    private LightingManager lightingManager;
    private Node cubesNode = new Node("CubesNode");
    private GUIManager guiManager;
    private DroneManager droneManager;
    private FilterPostProcessor fpp;
    private WaterFilterManager waterFilterManager;
    private PhysicsEngine physicsEngine;

    // Variables pour le courant
    private CurrentMatrix currentMatrix;
    private CurrentDebugView currentDebugView; // On garde une référence globale

    private boolean showCurrentDebug = false; // Désactivé par défaut
    private final float maxYawAngleChange = 30f;
    private final float maxPitchAngleChange = 10f;
    private final float currentForce = 0.1f; // Ne pas dépasser 0.5f sinon ça devient beaucoup trop fort
    private final int currentWidth = 100;
    private final int currentLenght = 100;
    private final float currentCellSize = 0.5f;
    private float currentTimer = 0f;
    private final float TIME_BETWEEN_UPDATES = 5.0f;

    private boolean showMeshWireframe = false; // Affiche le mesh
    private boolean loading = true; // Indique si la map a chargé
    private float lastTemperature = Float.NaN; // dernière température enregistrée
    private Camera pilotCamera;
    private volatile boolean initialized = false;
    private DepthSensor depthSensor;
    private SimulationConfiguration configuration;

    // Maps par droneId
    private final ConcurrentHashMap<String, Drone> drones = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DroneController> droneControllers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DroneCameraManager> cameraManagers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, SensorManager> sensorManagers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TemperatureSensor> tempSensors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PressureSensor> pressureSensors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompassSensor> compassSensors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WaterDistortionManager> distortions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicReference<DroneStatus>> statuts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DroneFrameCapture> frameCaptures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, DroneInstance> droneInstances = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> switchCamHeld = new ConcurrentHashMap<>();


    //  Queue thread-safe : réseau -> JME3
    private final ConcurrentLinkedQueue<PendingDrone> pendingDrones = new ConcurrentLinkedQueue<>();

    private static class PendingDrone {
        final String droneId;
        final String modelName;

        PendingDrone(String droneId, String modelName) {
            this.droneId = droneId;
            this.modelName = modelName;
        }
    }

    public static void main(String[] args) {

        AppSettings settings = new AppSettings(true);
        settings.setWidth(1024);
        settings.setHeight(768);
        settings.setVSync(false);
        settings.setFrameRate(-1);

        Main app = new Main();
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();
    }

    /**
     * Initialise la simulation JME3.
     * <p>
     * Cette méthode est appelée une seule fois au démarrage de l'application.
     * <p>
     * Elle crée :
     * - le modèle du monde (WorldView)
     * - les systèmes physiques (PhysicsEngine)
     * - l'éclairage de la scène
     * - la matrice des courants marins
     * - le DroneManager
     * - le terrain de simulation
     * <p>
     * Une fois le terrain chargé, la simulation est marquée comme initialisée
     * et prête à recevoir des drones via le réseau.
     */
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
        this.physicsEngine = new PhysicsEngine(this, this.model, this.guiManager, false);

        int terrainId = 1;

        if (configuration != null && configuration.getEnvironmentType() != null) {
            terrainId = configuration.getEnvironmentType().getTerrainId();
        }

        // Generation des courants
        this.currentMatrix = new CurrentMatrix(currentWidth, currentLenght, currentCellSize, maxYawAngleChange, maxPitchAngleChange, currentForce);

        // Configure scene
        this.lightingManager = new LightingManager(rootNode, cam, assetManager);
        this.lightingManager.initLighting();
        // Filter processor
        this.fpp = new FilterPostProcessor(assetManager);
        this.droneManager = new DroneManager(assetManager, rootNode, physicsEngine, inputManager, cam, model, currentMatrix);

        //this.cameraManager = new CameraManager(cam, flyCam);
        // Set false to disable flying camera
        //this.cameraManager.configureCamera(false);

        final int finalTerrainId = terrainId;
        // Creates scene and with a callback add the rest of the physics objects
        Node sceneNode = physicsEngine.CreateTerrainScene(finalTerrainId, () -> {            // Only executes when the terrain is ready
            // Add all physics objects here to avoid issues with the terrain not being finished
            System.out.println("Terrain ready : adding the CUBES");
            //physicsEngine.SpawnTestCube(rootNode, -4f, 15f, 0f, ColorRGBA.Red);
            //physicsEngine.SpawnTestCube(rootNode, 0f, 2f, 0f, ColorRGBA.Yellow);
            // Create a drone
            /*String droneName = "BR2R4";

            if (configuration != null && configuration.getDroneType() != null) {
                droneName = configuration.getDroneType().getSimulatorName();
            }

            droneManager.CreateDrone(droneName,droneId);            // Gets the manager and controller te be used here
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
            this.compassSensor = new CompassSensor("CompassSensor");
            drone.addSensor(this.compassSensor);
            this.pressureSensor = new PressureSensor("PressureSensor", this.depthSensor);
            drone.addSensor(this.pressureSensor);
            // Creates the water filter
            //waterFilter existe maintenant de partout
            this.waterFilterManager = new WaterFilterManager(fpp, viewPort, rootNode, model);
            this.waterDistortionManager = new WaterDistortionManager(fpp, viewPort, droneManager.getDrone(), model);

            this.sensorManager = new SensorManager(drone, lightingManager, waterFilterManager);
            sensorManager.initSensors();
            // Affichage des flèches de courant
            this.currentDebugView = new CurrentDebugView(currentMatrix, assetManager, rootNode);
            currentDebugView.setVisible(showCurrentDebug);
            if(showCurrentDebug) currentDebugView.showCurrentGrid(0.5f);
        */
            // End of the loading, will execute the simpleUpdate
            this.waterFilterManager = new WaterFilterManager(fpp, viewPort, rootNode, model);
            this.currentDebugView = new CurrentDebugView(currentMatrix, assetManager, rootNode);
            currentDebugView.setVisible(showCurrentDebug);
            loading = false;
            initialized = true;
            System.out.println("[Main] Simulation initialisée — en attente de drones.");
        });
        // Attach scene to node
        rootNode.attachChild(sceneNode);
        //this.initialized = true;
        //System.out.println("[Simulation] Initialisation terminée.");


    }

    /**
     * Indique si la simulation est entièrement initialisée.
     * <p>
     * Utilisé par les composants réseau pour attendre que le terrain,
     * les systèmes physiques et les gestionnaires soient prêts avant
     * de créer des drones ou d'envoyer des commandes.
     *
     * @return true si la simulation est prête.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Boucle principale de mise à jour exécutée à chaque frame.
     * <p>
     * Responsabilités :
     * - création des drones en attente provenant du réseau
     * - mise à jour des courants marins
     * - mise à jour des contrôleurs de drones
     * - mise à jour des caméras embarquées
     * - mise à jour des capteurs
     * - mise à jour des effets visuels liés à l'eau
     * - consommation de batterie
     * - génération des statuts envoyés aux clients
     *
     * @param tpf temps écoulé depuis la frame précédente.
     */
    @Override
    public void simpleUpdate(float tpf) {
        if (loading) {
            return;
        }

        // Créer les drones en attente dans le thread JME3
        PendingDrone pending;
        while ((pending = pendingDrones.poll()) != null) {
            creerDroneInterne(pending.droneId, pending.modelName);
        }

        // Courants
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

        // Mise à jour de chaque drone
        for (Map.Entry<String, Drone> entry : drones.entrySet()) {
            String droneId = entry.getKey();
            Drone drone = entry.getValue();

            drone.setCollisionDetected(false);
            drone.setCollisionMessage("Aucune collision");

            DroneController droneController = droneControllers.get(droneId);
            if (droneController != null) {
                droneController.updateMovement(tpf);
            }
            DroneInstance instance = droneInstances.get(droneId);
            DroneCameraManager droneCameraManager =
                    instance != null ? instance.getActiveCameraManager() : cameraManagers.get(droneId);
            // --- Free-Look Camera ---
            if (droneController != null && droneCameraManager != null) {

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

            // update() met à jour cam (fenêtre serveur) ET calcule la rotation
            // qui sera lue par DroneFrameCapture.preFrame() via getManualRotation()

            if (droneCameraManager != null) {
                droneCameraManager.update(tpf);
            }

            WaterDistortionManager waterDistortionManager = distortions.get(droneId);
            if (waterDistortionManager != null) {
                waterDistortionManager.update();
            }
            SensorManager sensorManager = sensorManagers.get(droneId);
            if (sensorManager != null) {
                sensorManager.updateSensors();
            }

            if (drone.getBattery() != null) {
                float consumption = 0.05f * tpf;
                //Donc plus le temps passe,plus la batterie diminue
                //La batterie commence à diminuer dès que l'on active le main
                //Donc lorsqu'on commence à voir les données un peu de batterie a été consommé lors du lancement de l'application
                drone.getBattery().consommation(consumption);
                //retire de l'énergie à la batterie
                //System.out.println("Batterie : " + drone.getBattery().getPercentage() + "%");
                mettreAJourStatut(droneId, drone);
            }
        }
    }

    /**
     * Crée complètement un drone dans le thread JME3.
     * <p>
     * Cette méthode :
     * - crée le drone physique via DroneManager
     * - ajoute tous les capteurs nécessaires
     * - initialise le SensorManager
     * - initialise les effets visuels du drone
     * - enregistre le drone dans toutes les structures de données
     * <p>
     * Cette méthode ne doit être appelée que depuis le thread JME3.
     *
     * @param droneId   identifiant unique du drone.
     * @param modelName modèle du drone à charger.
     */
    private void creerDroneInterne(String droneId, String modelName) {
        if (drones.containsKey(droneId)) {
            System.out.println("[Main] Drone déjà existant : " + droneId);
            return;
        }

        DroneInstance instance = droneManager.createDrone(modelName, droneId);
        Drone drone = instance.drone;

        TemperatureSensor ts = new TemperatureSensor("TempSensor_" + droneId);
        DepthSensor ds = new DepthSensor("DepthSensor_" + droneId);
        CompassSensor cs = new CompassSensor("CompassSensor_" + droneId);
        PressureSensor ps = new PressureSensor("PressureSensor_" + droneId, ds);
        drone.addSensor(ts);
        drone.addSensor(ds);
        drone.addSensor(cs);
        drone.addSensor(ps);

        SensorManager sm = new SensorManager(drone, lightingManager, waterFilterManager);
        sm.initSensors();

        WaterDistortionManager wdm = new WaterDistortionManager(fpp, viewPort, drone, model);

        drones.put(droneId, drone);
        droneInstances.put(droneId, instance);
        droneControllers.put(droneId, instance.controller);
        cameraManagers.put(droneId, instance.cameraManager);
        sensorManagers.put(droneId, sm);
        tempSensors.put(droneId, ts);
        pressureSensors.put(droneId, ps);
        compassSensors.put(droneId, cs);
        distortions.put(droneId, wdm);
        statuts.put(droneId, new AtomicReference<>(new DroneStatus()));

        System.out.println("[Main] Drone enregistré : " + droneId);
    }

    /**
     * Demande la création d'un nouveau drone.
     * <p>
     * La création réelle n'est pas effectuée immédiatement.
     * Le drone est placé dans une file d'attente thread-safe puis
     * créé lors du prochain simpleUpdate() dans le thread JME3.
     * <p>
     * Utilisé par les composants réseau.
     *
     * @param droneId   identifiant unique du drone.
     * @param modelName modèle à charger.
     */
    public void ajouterDrone(String droneId, String modelName) {
        if (drones.containsKey(droneId)) return;
        pendingDrones.add(new PendingDrone(droneId, modelName));
        System.out.println("[Main] Drone en file d'attente : " + droneId);
    }

    /**
     * Applique une commande de pilotage reçue depuis le réseau.
     * <p>
     * La commande est transférée au DroneController associé au drone.
     *
     * @param droneId identifiant du drone ciblé.
     * @param command commande reçue du client.
     */
    public void executeNetworkCommand(String droneId, PilotCommand command) {
        if (command == PilotCommand.SWITCHCAM) {

            // Évite de switcher 20 fois si le client envoie SWITCHCAM en boucle
            if (Boolean.TRUE.equals(switchCamHeld.get(droneId))) {
                return;
            }

            switchCamHeld.put(droneId, true);

            enqueue(() -> {
                DroneInstance instance = droneInstances.get(droneId);

                if (instance == null) {
                    System.err.println("[Simulation] SWITCHCAM impossible : instance introuvable pour " + droneId);
                    return null;
                }

                instance.switchCamera();

                DroneCameraManager activeCamera = instance.getActiveCameraManager();
                cameraManagers.put(droneId, activeCamera);

                DroneFrameCapture capture = frameCaptures.get(droneId);
                if (capture != null) {
                    capture.setDroneCameraManager(activeCamera);
                }

                System.out.println("[Simulation] Caméra active changée pour " + droneId);
                return null;
            });

            return;
        }

        // Dès qu'une autre commande arrive, on autorise un futur SWITCHCAM
        switchCamHeld.put(droneId, false);
        DroneController controller = droneControllers.get(droneId);

        if (controller == null) {
            return;
        }

        controller.executeCommand(command, true);

        System.out.println("[Simulation] Commande appliquée à " + droneId + " : " + command);
    }

    /**
     * Retourne le dernier état connu du drone.
     * <p>
     * Le statut est mis à jour dans simpleUpdate() puis stocké
     * dans une AtomicReference afin de permettre un accès
     * thread-safe depuis les threads réseau.
     * <p>
     * Cette méthode est non bloquante.
     *
     * @param droneId identifiant du drone.
     * @return dernier statut connu du drone.
     */
    public DroneStatus getDroneStatusDirect(String droneId) {
        AtomicReference<DroneStatus> ref = statuts.get(droneId);
        if (ref == null) {
            DroneStatus s = new DroneStatus();
            s.setState("En initialisation...");
            return s;
        }
        return ref.get();
    }

    /**
     * Crée la caméra de capture vidéo associée à un drone.
     * <p>
     * Cette caméra est indépendante de la vue principale du serveur
     * et sert à produire les images JPEG envoyées aux clients.
     * <p>
     * Un seul viewport est créé par drone.
     *
     * @param droneId identifiant du drone.
     */
    public void creerViewPortOffScreen(String droneId) {
        DroneCameraManager cameraManager = cameraManagers.get(droneId);
        Drone drone = drones.get(droneId);
        if (cameraManager == null || drone == null) {
            System.err.println("[Main] creerViewPortOffScreen : drone ou caméra introuvable pour " + droneId);
            return;
        }
        if (frameCaptures.containsKey(droneId)) {
            System.out.println("[Main] ViewPort déjà créé pour " + droneId);
            return;
        }

        DroneFrameCapture capture = new DroneFrameCapture(droneId, cameraManager.getCamera(), renderManager, rootNode, guiNode, assetManager, model, drone, cameraManager);
        frameCaptures.put(droneId, capture);
        System.out.println("[Main] ViewPort off-screen créé pour " + droneId);
    }

    /**
     * Retourne la dernière image capturée par la caméra du drone.
     * <p>
     * L'image est déjà compressée au format JPEG et stockée dans
     * une AtomicReference par DroneFrameCapture.
     * <p>
     * Cette méthode est non bloquante.
     *
     * @param droneId identifiant du drone.
     * @return tableau d'octets JPEG ou null si aucune image n'est disponible.
     */
    public byte[] capturerFrameDrone(String droneId) {
        DroneFrameCapture capture = frameCaptures.get(droneId);
        return capture != null ? capture.capturerFrame() : null;
    }

    /**
     * Définit la configuration de simulation à utiliser lors
     * de l'initialisation du terrain et des drones.
     * <p>
     * Cette configuration est généralement reçue depuis le client
     * avant le démarrage de la simulation.
     *
     * @param configuration configuration de simulation.
     */
    public void setConfiguration(SimulationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Construit un nouveau DroneStatus à partir de l'état courant
     * du drone puis le stocke dans le cache des statuts.
     * <p>
     * Les informations collectées comprennent :
     * - position
     * - orientation
     * - température
     * - pression
     * - profondeur
     * - cap
     * - batterie
     * - collisions
     * - configuration active
     * <p>
     * Le statut est ensuite utilisé par les composants réseau
     * pour répondre aux clients sans accéder directement aux objets JME3.
     *
     * @param droneId identifiant du drone.
     * @param drone   drone concerné.
     */
    private void mettreAJourStatut(String droneId, Drone drone) {
        AtomicReference<DroneStatus> ref = statuts.get(droneId);
        if (ref == null) return;

        DroneStatus s = new DroneStatus();
        com.jme3.math.Vector3f pos = drone.getDroneSpatial().getLocalTranslation();
        s.setX(pos.x);
        s.setY(pos.y);
        s.setZ(pos.z);
        s.setSpeed(0.0);
        s.setState("En vol");
        s.setCollisionDetected(drone.isCollisionDetected());
        s.setCollisionMessage(drone.getCollisionMessage());

        float[] angles = drone.getDroneSpatial().getWorldRotation().toAngles(null);
        s.setPitch(Math.toDegrees(angles[0]));
        s.setYaw(Math.toDegrees(angles[1]));
        s.setRoll(Math.toDegrees(angles[2]));

        // Gère l'envoie des données pour la caméra
        DroneInstance instance = droneInstances.get(droneId);
        boolean isShowingFrontCam = instance.isShowingFrontCam();
        s.setShowingFrontCamera(isShowingFrontCam);
        DroneCameraManager camManager = instance.getActiveCameraManager();
        if(camManager != null){
            float camPitch = camManager.getCameraPitch();
            float camYaw = camManager.getCameraYaw();
            if(isShowingFrontCam){
                s.setFrontCamPitch(camPitch);
                s.setFrontCamYaw(camYaw);
            }
            else {
                s.setRearCamPitch(camPitch);
                s.setRearCamYaw(camYaw);
            }
        }

        TemperatureSensor ts = tempSensors.get(droneId);
        if (ts != null) s.setTemperature(ts.getTemperature());

        PressureSensor ps = pressureSensors.get(droneId);
        if (ps != null) s.setPressure(ps.getPressure());

        CompassSensor cs = compassSensors.get(droneId);
        if (cs != null) {
            s.setHeading(cs.getHeading());
            s.setYaw(cs.getHeading());
        }

        WaterDistortionManager wdm = distortions.get(droneId);
        if (wdm != null) {
            s.setDepth(wdm.getProfondeur());
            s.setIntensity(wdm.getIntensity());
        }

        if (drone.getBattery() != null) s.setBattery(drone.getBattery().getPercentage());

        if (configuration != null) {
            if (configuration.getEnvironmentType() != null)
                s.setEnvironmentName(configuration.getEnvironmentType().toString());
            if (configuration.getDroneType() != null)
                s.setDroneType(configuration.getDroneType().toString());
        }

        ref.set(s);
    }
}
