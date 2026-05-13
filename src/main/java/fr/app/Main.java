package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.*;
import com.jme3.system.AppSettings;
import fr.app.model.PhysicsEngine;
import fr.app.model.WorldView;
import fr.app.view.CameraManager;
import fr.app.view.GUIManager;
import fr.app.view.LightingManager;
import fr.app.view.WaterFilterManager;

/**
 * Main class to launch the simulation
 */
public class Main extends SimpleApplication {

    private WorldView model;
    private LightingManager lightingManager;
    private CameraManager cameraManager;
    private Node cubesNode = new Node("CubesNode");
    private GUIManager guiManager;

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

        PhysicsEngine physicsEngine = new PhysicsEngine(this, this.model, this.guiManager);

        // Configure scene
        this.lightingManager = new LightingManager(rootNode, cam, assetManager);
        this.lightingManager.initLighting();

        this.cameraManager = new CameraManager(cam, flyCam);
        // Set false to disable flying camera
        this.cameraManager.configureCamera(true);

        // Creates scene and with a callback add the rest of the physics objects
        Node sceneNode = physicsEngine.createTerrainScene(2, () -> {
            // Only executes when the terrain is ready
            // Add all physics objects here to avoid issues with the terrain not being finished
            System.out.println("Terrain ready : adding the CUBES");
            physicsEngine.spawnTestCube(rootNode, -4f, 15f, 0f, ColorRGBA.Red);
            physicsEngine.spawnTestCube(rootNode, 0f, 2f, 0f, ColorRGBA.Yellow);
        });
        // Attach scene to node
        rootNode.attachChild(sceneNode);
        // Creates the water filter
        WaterFilterManager waterFilterManager = new WaterFilterManager(assetManager, viewPort, rootNode, model);
    }
}
