package fr.utln.jme3.renderer;

import com.jme3.renderer.Camera;

import fr.utln.jme3.math.CameraMatrix;
import fr.utln.jme3.math.DistortionModel;

/**
 * a JMonkey camera that render realistically using the pinhole camera model.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 */
public class PinholeCamera extends Camera {

	private CameraMatrix cameraMatrix = null;
	private DistortionModel distortionModel = null;
	
	private int imageWidth = 1;
	private int imageHeight = 1;
	
	private float pixelWidth = 1.0f;
	private float pixelHeight = 1.0f;
	
	
	PinholeCamera(int imageWidth, int imageHeight, float pixelWidth, float pixelHeight, CameraMatrix cameraMatrix, DistortionModel distortionModel) {
		super();
		
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
		
		this.cameraMatrix = cameraMatrix;
		this.distortionModel = distortionModel;
		
	}
}
