package fr.utln.jme3.filter;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

import fr.utln.jme3.math.DistortionModel;

/**
 * Apply an optical distortion to the rendered scene. The distortion is computed according to <a href="https://docs.opencv.org/4.x/d9/d0c/group__calib3d.html">OpenCV distortion model</a>
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a>
 */
public class DistortionFilter extends Filter {

	/**
	 * The underlying {@link DistortionModel distortion model}.
	 */
	private DistortionModel distortionModel = null;
	
	/**
	 * Create a new distortion filter with all coefficients set to <code>0.0</code>.
	 */
    public DistortionFilter() {
        super("DistortionFilter");
        distortionModel = new DistortionModel();
    }

	/**
	 * Create a new distortion filter with default OpenCV radial / tangential distortion coefficients.
	 * @param distortionModel the underlying distortion model
	 */
    public DistortionFilter(DistortionModel distortionModel) {
        super("DistortionFilter");
        this.distortionModel = distortionModel;
    }

    @Override
    protected Material getMaterial() {
        return material;
    }

    @Override
    protected void initFilter(AssetManager assetManager, RenderManager renderManager, ViewPort vp, int w, int h) {
        material = new Material(assetManager, "Common/MatDefs/Distortion/Distortion.j3md");
        material.setFloat("K1", distortionModel.getK1());
        material.setFloat("K2", distortionModel.getK2());
        material.setFloat("K3", distortionModel.getK3());
        material.setFloat("K4", distortionModel.getK4());
        material.setFloat("K5", distortionModel.getK5());
        material.setFloat("K6", distortionModel.getK6());
        	
        material.setFloat("P1", distortionModel.getP1());
        material.setFloat("P2", distortionModel.getP2());
        
        material.setFloat("S1", distortionModel.getS1());
        material.setFloat("S2", distortionModel.getS2());
        material.setFloat("S3", distortionModel.getS3());
        material.setFloat("S4", distortionModel.getS4());  
    }
    
    /**
     * Get the underlying distortion model.
     * @return the underlying distortion model
     */
    public DistortionModel getDistortionModel() {
    	return this.distortionModel;
    }
}
