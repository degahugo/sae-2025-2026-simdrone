package fr.utln.dronsim.viewer.example;

import com.github.stephengold.wrench.LwjglAssetLoader;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.input.ChaseCamera;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.material.MatParam;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LightControl;
import com.jme3.system.AppSettings;

/**
 * An example that illustrate the <a href="https://github.com/stephengold/MonkeyWrench/">MonkeyWrench</a> library for asset loading.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a>
 */
public class AssetLoaderExample extends SimpleApplication {
	
	public void displaySpatialTree(Spatial s, String tab) {
		if (s == null) {
			System.out.println("null");
			return;
		}
		
		System.out.println(tab+"["+s.getClass().getSimpleName()+"] "+s.getName()+" ("+s.getWorldBound()+")");
		
		if (s instanceof Node) {
			for(Spatial sp : ((Node)s).getChildren()) {
				displaySpatialTree(sp, tab+"  ");
			}
		} if (s instanceof Geometry) {
			Geometry g = (Geometry)s;
			
			System.out.println(tab+"[Material]");
			for(MatParam param : g.getMaterial().getParams())
				System.out.println(tab+"  "+param.getName()+": "+param.getValueAsString());
			
			g.getMaterial().getParams();
			g.getMaterial().getTextureParam("");
		}
	}
	
	@Override
    public void simpleInitApp() {

		// Register the MonkeyWrench loader to the asset mlanager
		assetManager.registerLoader(LwjglAssetLoader.class,"3ds", "3mf", "blend", "bvh", "dae", "fbx", "glb", "gltf", "lwo", "meshxml", "mesh.xml", "obj", "ply", "stl");
		
		// Register the data/asset directory as an asset locator
		assetManager.registerLocator("data/asset", FileLocator.class);
		
    	
		// Load a GLTF asset
		//Spatial spatial = assetManager.loadModel("vehicle/subseatech/guardian/scene.gltf");
        //Spatial spatial = assetManager.loadModel("vehicle/buster/buster.gltf");
		Spatial spatial = assetManager.loadModel("vehicle/bluerobotics/br2r4/BLUEROV2-R4-EXTERNAL.gltf");
		spatial.scale(0.001f);

		displaySpatialTree(spatial, "");
		
		
        rootNode.attachChild(spatial);
        
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
        
        // Enable chase camera
        LightControl lightControl = new LightControl(lamp_light);
        
        CameraNode camNode = new CameraNode("Chase", cam);
        camNode.addControl(lightControl);
        
        
        flyCam.setEnabled(false);
        ChaseCamera chaseCam = new ChaseCamera(cam, spatial, inputManager);
        chaseCam.setZoomSensitivity(0.5f);

        
    }
	
	/**
	 * The main method
	 * @param args the main method arguments
	 */
	public static void main(String[] args) {
		
		AppSettings settings=new AppSettings(true);
    	
		AssetLoaderExample app = new AssetLoaderExample();
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start(); // start the game
	}
}