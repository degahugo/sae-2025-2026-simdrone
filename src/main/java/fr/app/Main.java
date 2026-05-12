package fr.app;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.*;
import com.jme3.system.AppSettings;
import fr.app.model.PhysicsEngine;
import fr.app.model.WorldView;
import fr.app.view.CameraManager;
import fr.app.view.LightingManager;
import fr.app.view.WaterFilterManager;

/**
 * Main class to launch the simulation
 */
public class Main extends SimpleApplication {

    private WorldView model;
    private LightingManager lightingManager;
    private CameraManager cameraManager;

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

        this.cameraManager = new CameraManager(cam, flyCam);
        // Set false to disable flying camera
        this.cameraManager.configureCamera(true);

        // Creates scene
        Node sceneNode = physicsEngine.createTerrainScene();
        // Attach scene to node
        rootNode.attachChild(sceneNode);
        // Creates the water filter
        WaterFilterManager waterFilterManager = new WaterFilterManager(assetManager, viewPort, rootNode, model);
        // Add tests cubes to test collisions, should be commented or removed later
        physicsEngine.spawnTestCube(0, 15f, 0, ColorRGBA.Red);
        physicsEngine.spawnTestCube(0, 2f, 0, ColorRGBA.Yellow);
    }
}
