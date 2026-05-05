package fr.utln.dronsim.viewer.example;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.system.AppSettings;

import fr.utln.jme3.filter.DistortionFilter;
import fr.utln.jme3.math.DistortionModel;
import fr.utln.jme3.model.Chessboard;

/**
 * This class illustrates the use of a realistic optical distortion filter to a rendered scene.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 */
public class DistortionExample extends SimpleApplication {
	
	@Override
	public void simpleInitApp() {
	    
		// Create scene objects
		Chessboard chessboard = new Chessboard(assetManager, 6, 10, 1.0f, 0.1f);
		rootNode.attachChild(chessboard);

	    // Initialize lighting
	    initLighting();

	    // Set up camera
	    cam.setLocation(new Vector3f(chessboard.getCornersCols()*chessboard.getSize()/2.0f, chessboard.getCornersRows()*chessboard.getSize()/2.0f, 10.0f));

	    viewPort.setBackgroundColor(ColorRGBA.Gray);
	    
	    // Create a distortion filter that enables to a	apply realistic optical distortion
	    // to the viewport
	    //DistortionFilter distortionFilter = new DistortionFilter(-0.2672425f, 0.09802037f, -0.01952308f, 0.00066969f, 0.00050446f);
	    DistortionModel distortionModel = new DistortionModel(-0.1f, 0.0f, 0.0f, 0.0f, 0.0f);
	    DistortionFilter distortionFilter = new DistortionFilter(distortionModel);
	    
	    // Create a filter post processor
	    FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

	    // Add the distortion filter to the post processor
	    fpp.addFilter(distortionFilter);
	    
	    // Attach the distortion filter to the viewport
	    viewPort.addProcessor(fpp);
	}

	/**
	 * Initialize the global scene lighting
	 */
	private void initLighting() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);
	}

	
	/**
	 * The main method
	 * @param args the main method arguments
	 */
	public static void main(String[] args) {

		AppSettings settings=new AppSettings(true);

		settings.setWidth(1024);
		settings.setHeight(768);

		DistortionExample app = new DistortionExample();
		app.setShowSettings(false);
		app.setSettings(settings);
		app.start();
	}
}
