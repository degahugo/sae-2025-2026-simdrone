package fr.utln.jme3.math;

import com.jme3.math.Matrix3f;

/**
 * A camera matrix. This 3x3 matrix is used to represent the intrinsic parameters of a pinhole camera:<br>
 * [ f<sub>x</sub> 0 c<sub>x</sub> ]<br>
 * [ 0 f<sub>y</sub> c<sub>y</sub> ]<br>
 * [ 0 0 1 ]<br>
 * 
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 * @see fr.utln.jme3.math.DistortionModel
 */
public class CameraMatrix  {
	
	/**
	 * The underlying matrix:<br>
	 * [ f<sub>x</sub> 0 c<sub>x</sub> ]<br>
	 * [ 0 f<sub>y</sub> c<sub>y</sub> ]<br>
	 * [ 0 0 1 ]<br>
	 */
	private Matrix3f matrix;
	
	/**
	 * Create a new camera matrix according to the given values.
	 * @param fx the horizontal focal length (along X)
	 * @param fy the vertical focal length (along Y)
	 * @param cx the principal point coordinate along X
	 * @param cy the principal point coordinate along Y
	 */
	public CameraMatrix(float fx, float fy, float cx, float cy) {
		matrix = new Matrix3f();
		
		matrix.set(0, 0,   fx); matrix.set(0, 1, 0.0f); matrix.set(0, 2,   cx);
		matrix.set(1, 0, 0.0f);	matrix.set(1, 1,   fy); matrix.set(1, 2,   cy);
		matrix.set(2, 0, 0.0f); matrix.set(2, 1, 0.0f); matrix.set(2, 2, 1.0f);
	}
	
	/**
	 * Get the underlying {@link Matrix3f matrix}:<br>
	 * [ f<sub>x</sub> 0 c<sub>x</sub> ]<br>
	 * [ 0 f<sub>y</sub> c<sub>y</sub> ]<br>
	 * [ 0 0 1 ]<br>
	 * 
	 * @return the underlying matrix
	 */
	public Matrix3f getMatrix() {
		return matrix;
	}
	
	/**
	 * Get the horizontal focal length (along X).
	 * @return the horizontal focal length (along X)
	 * @see #getFy()
	 */
	public float getFx() {
		return matrix.get(0, 0);
	}
	
	/**
	 * Get the vertical focal length (along Y).
	 * @return the vertical focal length (along Y)
	 * @see #getFx()
	 */
	public float getFy() {
		return matrix.get(1, 1);
	}
	
	/**
	 * Get the principal point coordinate along X.
	 * @return the principal point coordinate along X
	 * @see #getCy()
	 */
	public float getCx() {
		return matrix.get(0, 2);
	}
	
	/**
	 * Get the principal point coordinate along Y.
	 * @return the principal point coordinate along Y
	 * @see #getCx()
	 */
	public float getCy() {
		return matrix.get(1, 2);
	}
	
}
