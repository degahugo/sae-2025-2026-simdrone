package fr.utln.dronsim.viewer.example;

import com.github.stephengold.wrench.LwjglAssetLoader;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.water.WaterFilter;

import fr.utln.dronsim.viewer.primitives.ReferentialNode;

import fr.app.controller.DroneController;
import fr.app.model.Drone;

/**
 * An example that illustrate a complete scene with water simulation.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a>
 */
public class WaterFilterExample extends SimpleApplication {

	/**
	 * The filter post processor
	 */
	private FilterPostProcessor fpp = null;

    private Drone drone;
    private DroneController droneController;

	@Override
	public void simpleInitApp() {

		// Create scene objects
		Node scene = createScene();

		// ******************************************************
		// ** Rendering water need Y vector to be the vertical **
		scene.rotate(-FastMath.HALF_PI, 0.0f, 0.0f);
		// ******************************************************

		rootNode.attachChild(scene);

		// Initialize lighting
		initLighting();

		initFilter();

		// Set up camera
		cam.setLocation(new Vector3f(4.00f, 4.40f, 2.0f));
		cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
		flyCam.setUpVector(Vector3f.UNIT_Y);

		// Add a sky
		rootNode.attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));

	}

    @Override
    public void simpleUpdate(float tpf) {

        droneController.updateMovement();

        Vector3f dronePosition =
                drone.getDroneSpatial()
                        .getWorldTranslation();

        Vector3f droneForward =
                drone.getDroneSpatial()
                        .getWorldRotation()
                        .mult(Vector3f.UNIT_Z);

        Vector3f cameraOffset =
                droneForward.negate().mult(6f)
                        .add(0f, 3f, 2f);

        cam.setLocation(
                dronePosition.add(cameraOffset)
        );

        cam.lookAt(
                dronePosition,
                Vector3f.UNIT_Y
        );
    }

	/**
	 * Create the main 3D scene
	 */
	private Node createScene() {

		Node scene = new Node("Main scene");

		// Register the MonkeyWrench loader to the asset manager
		assetManager.registerLoader(LwjglAssetLoader.class,"3ds", "3mf", "blend", "bvh", "dae", "fbx", "glb", "gltf", "lwo", "meshxml", "mesh.xml", "obj", "ply", "stl");

		// Register the data/asset directory as an asset locator
		assetManager.registerLocator("data/asset", FileLocator.class);

		// Create a referential node
		ReferentialNode refNode = new ReferentialNode(assetManager);
		scene.attachChild(refNode);

		// Create a first vehicle
		Node vehicleANode = new Node("Vehicle A"); // The node that holds the vehicle

		//   - Load the vehicle model from an asset
		Spatial vehicleAModel = assetManager.loadModel("vehicle/bluerobotics/br2r4/br2-r4-vehicle.j3o");

		//   - Attach the vehicle model to the vehicle node
		vehicleANode.attachChild(vehicleAModel);

		//   - Transform the vehicle node in order to place it within the 3D space
		vehicleANode.rotate(FastMath.PI/2.0f, 0.0f, 0.0f);
		vehicleANode.move(3.0f, 0.0f, 0.0f);

		//   - Attach the vehicle node to the main scene
		scene.attachChild(vehicleANode);

        drone = new Drone(vehicleANode);

        //droneController = new DroneController(inputManager, drone);

        droneController.setUpKeys();

		// Create a second vehicle
		Node vehicleBNode = new Node("Vehicle B");

		//   - Load the vehicle model from an asset
		Spatial vehicleBModel = assetManager.loadModel("vehicle/subseatech/guardian/guardian-vehicle.j3o");

		//   - Attach the vehicle model to the vehicle node
		vehicleBNode.attachChild(vehicleBModel);

		//   - Transform the vehicle node in order to place it within the 3D space
		vehicleBNode.rotate(FastMath.PI/2.0f, 0.0f, 0.0f);
		vehicleBNode.move(-3.0f, 0.0f, 0.0f);

		//   - Attach the vehicle node to the main scene
		scene.attachChild(vehicleBNode);

		// Load a terrain

		// If the terrain use texture, the asset manager has to be updated.
		assetManager.registerLocator("data/asset/terrain/military", FileLocator.class);

		Spatial terrain = assetManager.loadModel("terrain/military/target-approach-line-terrain.j3o");
		scene.attachChild(terrain);

		return scene;

	}

	/**
	 * Init the water filter
	 */
	private void initFilter() {
		//ExtendedWaterFilter waterFilter = new ExtendedWaterFilter(Vector3f.UNIT_Z, 0.0f);
		//ExtendedWaterFilter waterFilter = new ExtendedWaterFilter();
		WaterFilter waterFilter = new WaterFilter();

		waterFilter.setWaterHeight(3.0f);
		waterFilter.setSpeed(0.5f);

		fpp = new FilterPostProcessor(assetManager);

        fpp.addFilter(waterFilter);

        viewPort.addProcessor(fpp);
	}

	/**
	 * Initialize the global scene lighting
	 */
	private void initLighting() {
		// Set up scene lighting
		PointLight lamp_light = new PointLight();
		lamp_light.setColor(ColorRGBA.White);
		lamp_light.setRadius(4f);
		lamp_light.setPosition(new Vector3f(0.0f, 0.0f, 2.0f));
		rootNode.addLight(lamp_light);

		SpotLight spot = new SpotLight();
		spot.setSpotRange(100f);                           // distance
		spot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
		spot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
		spot.setColor(ColorRGBA.White);         // light color
		spot.setPosition(cam.getLocation());               // shine from camera loc
		spot.setDirection(cam.getDirection());             // shine forward from camera loc
		rootNode.addLight(spot);

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
		rootNode.addLight(sun);
	}

	/**
	 * The main method
	 * @param args the main method arguments
	 */
	public static void main(String[] args) {

		AppSettings settings=new AppSettings(true);

		settings.setWidth(1024);
		settings.setHeight(768);

		WaterFilterExample app = new WaterFilterExample();
		app.setShowSettings(false);
		app.setSettings(settings);
		app.start(); // start the game
	}

}
