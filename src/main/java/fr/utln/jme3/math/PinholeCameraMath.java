package fr.utln.jme3.math;

import java.util.ArrayList;
import java.util.List;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

/**
 * A class that provides mathematical calculations that are related to pinhole camera.<br>
 * Some methods of this class are JMonkey implementations of <a href="https://github.com/opencv/opencv/blob/4.x/modules/calib3d/">OpenCV calib3d module</a>.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 */
public class PinholeCameraMath {

	/**
	 * Compute tilt distortion matrices from &tau;<sub>x</sub> and &tau;<sub>y</sub> values. 
	 * In some cases, the image sensor may be tilted in order to focus an oblique plane in front of the camera (Scheimpflug principle). 
	 * This can be useful for particle image velocimetry (PIV) or triangulation with a laser fan.<br><br> 
	 * The tilt causes a perspective distortion of x′′ and y′′. This distortion can be modeled in the following way.
	 * 
	 * $$\begin{bmatrix} u \\ v \end{bmatrix} = \begin{bmatrix} f_x x''' + c_x \\ f_y y''' + c_y \end{bmatrix}$$
	 * where:
	 * $$s\vecthree{x'''}{y'''}{1} = \vecthreethree{R_{33}(\tau_x, \tau_y)}{0}{-R_{13}(\tau_x, \tau_y)} {0}{R_{33}(\tau_x, \tau_y)}{-R_{23}(\tau_x, \tau_y)} {0}{0}{1} R(\tau_x, \tau_y) \vecthree{x''}{y''}{1}$$
	 * and the matrix $R(\tau_x, \tau_y)$ is defined by two rotations with angular parameter &tau;<sub>x</sub> and &tau;<sub>y</sub>:
	 * $$R(\tau_x, \tau_y) = \vecthreethree{\cos(\tau_y)}{0}{-\sin(\tau_y)}{0}{1}{0}{\sin(\tau_y)}{0}{\cos(\tau_y)} \vecthreethree{1}{0}{0}{0}{\cos(\tau_x)}{\sin(\tau_x)}{0}{-\sin(\tau_x)}{\cos(\tau_x)} = \vecthreethree{\cos(\tau_y)}{\sin(\tau_y)\sin(\tau_x)}{-\sin(\tau_y)\cos(\tau_x)} {0}{\cos(\tau_x)}{\sin(\tau_x)} {\sin(\tau_y)}{-\cos(\tau_y)\sin(\tau_x)}{\cos(\tau_y)\cos(\tau_x)}$$
	 * <br>
	 * <br>
	 * H. Louhichi, T. Fournel, J. M. Lavest, and H. Ben Aissia. Self-calibration of scheimpflug cameras: an easy protocol. Meas. Sci. Technol., 18(8):2616–2622, 2007.
	 * <br>
	 * <br>
	 * This implementation is a JMonkey Java port of <a href="https://github.com/opencv/opencv/blob/4.x/modules/calib3d/src/distortion_model.hpp">OpenCV computeTiltProjectionMatrix</a> function (see {@link https://docs.opencv.org/4.x/d9/d0c/group__calib3d.html}).
	 * @param tauX the &tau;<sub>x</sub> parameter value
	 * @param tauY the &tau;<sub>y</sub> parameter value
	 * @param matTilt the tilt matrix (output)
	 * @param dMatTiltdTauX the derivative tilt X matrix (output)
	 * @param dMatTiltdTauY the derivative tilt X matrix (output)
	 * @param invMatTilt the tilt inverse matrix (output)
	 * 
	 */
	public static void computeTiltProjectionMatrix(float tauX, float tauY, Matrix3f matTilt, Matrix3f dMatTiltdTauX, Matrix3f dMatTiltdTauY, Matrix3f invMatTilt) {

		float cTauX = FastMath.cos(tauX);
		float sTauX = FastMath.sin(tauX);

		float cTauY = FastMath.cos(tauY);
		float sTauY = FastMath.sin(tauY);

		Matrix3f matRotX = new Matrix3f(1,0,0,0,cTauX,sTauX,0,-sTauX,cTauX);
		Matrix3f matRotY = new Matrix3f(cTauY,0,-sTauY,0,1,0,sTauY,0,cTauY);

		Matrix3f matRotXY = matRotY.mult(matRotX);
		Matrix3f matProjZ = new Matrix3f(matRotXY.get(2,2),                 0, -matRotXY.get(0,2),
				0, matRotXY.get(2,2), -matRotXY.get(1,2),
				0,                 0,                  1);

		if (matTilt != null) {

			// Matrix for trapezoidal distortion of tilted image sensor
			matTilt = matProjZ.mult(matRotXY);
		}

		if (dMatTiltdTauX != null) {

			// Derivative with respect to tauX
			Matrix3f dMatRotXYdTauX = matRotY.mult(new Matrix3f(0,      0,     0, 
					0, -sTauX, cTauX, 
					0, -cTauX, -sTauX));

			Matrix3f dMatProjZdTauX = new Matrix3f(dMatRotXYdTauX.get(2,2),                       0, -dMatRotXYdTauX.get(0,2), 
					0, dMatRotXYdTauX.get(2,2), -dMatRotXYdTauX.get(1,2),
					0,                       0,                        0);
			Matrix3f tmp1 = matProjZ.mult(dMatRotXYdTauX);
			Matrix3f tmp2 = dMatProjZdTauX.mult(matRotXY);

			dMatTiltdTauX = new Matrix3f(tmp1.get(0, 0)+tmp2.get(0, 0), tmp1.get(0, 1)+tmp2.get(0, 1), tmp1.get(0, 2)+tmp2.get(0, 2),
					tmp1.get(1, 0)+tmp2.get(1, 0), tmp1.get(1, 1)+tmp2.get(1, 1), tmp1.get(1, 2)+tmp2.get(1, 2),
					tmp1.get(2, 0)+tmp2.get(2, 0), tmp1.get(2, 1)+tmp2.get(2, 1), tmp1.get(2, 2)+tmp2.get(2, 2));

		}

		if (dMatTiltdTauY != null) {

			// Derivative with respect to tauY
			Matrix3f dMatRotXYdTauY = new Matrix3f(-sTauY, 0, -cTauY, 0, 0, 0, cTauY, 0, -sTauY).mult(matRotX);

			Matrix3f dMatProjZdTauY = new Matrix3f(dMatRotXYdTauY.get(2,2),                       0, -dMatRotXYdTauY.get(0,2),
					0, dMatRotXYdTauY.get(2,2), -dMatRotXYdTauY.get(1,2),
					0,                       0,                         0);

			Matrix3f tmp1 = matProjZ.mult(dMatRotXYdTauY);
			Matrix3f tmp2 = dMatProjZdTauY.mult(matRotXY);
			dMatTiltdTauY = new Matrix3f(tmp1.get(0, 0)+tmp2.get(0, 0), tmp1.get(0, 1)+tmp2.get(0, 1), tmp1.get(0, 2)+tmp2.get(0, 2),
					tmp1.get(1, 0)+tmp2.get(1, 0), tmp1.get(1, 1)+tmp2.get(1, 1), tmp1.get(1, 2)+tmp2.get(1, 2),
					tmp1.get(2, 0)+tmp2.get(2, 0), tmp1.get(2, 1)+tmp2.get(2, 1), tmp1.get(2, 2)+tmp2.get(2, 2));

		}

		if (invMatTilt != null) {

			float inv = 1.0f/matRotXY.get(2,2);
			Matrix3f invMatProjZ = new Matrix3f(inv,0,inv*matRotXY.get(0,2),0,inv,inv*matRotXY.get(1,2),0,0,1);
			invMatTilt = matRotXY.transpose().mult(invMatProjZ);

		}
	}

	/**
	 * Undistort the points that are given in parameters. This method return undistorted points expressed in normalized coordinates. 
	 * <br>
	 * <br>
	 * This implementation is a JMonkey Java port of <a href="https://github.com/opencv/opencv/blob/4.x/modules/calib3d/src/undistort.dispatch.cpp">OpenCV cvUndistortPointsInternal</a> function.
	 * @param distorted a list of 2D point affected by the distortion
	 * @param undistorted a list of 2D point that are not by the distortion (if equals to <code>distorted</code>, the points are undistorted in place)
	 * @param cameraMatrix the camera matrix
	 * @param distortionModel the distortion model
	 * @return the list of undistorted points (same reference as <code>undistorted</code>
	 * @see #undistortPoints(List, List, CameraMatrix, DistortionModel, Matrix3f, Matrix3f, TerminationCriteria)
	 */
	public static List<Vector2f> undistortPoints(List<Vector2f> distorted, List<Vector2f> undistorted, 
			CameraMatrix cameraMatrix, DistortionModel distortionModel){

		return undistortPoints(distorted, undistorted, cameraMatrix, distortionModel, null, null, null);

	}


	/**
	 * Undistort the points that are given in parameters. This method return undistorted points expressed with new camera matrix (obtained from {@link #computeOptimalCameraMatrix(CameraMatrix, DistortionModel, Size2i, float, Size2i, Rect2f, boolean)}). 
	 * <br>
	 * <br>
	 * This implementation is a JMonkey Java port of <a href="https://github.com/opencv/opencv/blob/4.x/modules/calib3d/src/undistort.dispatch.cpp">OpenCV cvUndistortPointsInternal</a> function.
	 * @param distorted a list of 2D point affected by the distortion
	 * @param undistorted a list of 2D point that are not by the distortion (if equals to <code>distorted</code>, the points are undistorted in place)
	 * @param cameraMatrix the camera matrix
	 * @param distortionModel the distortion model
	 * @param newCameraMatrix the new camera matrix (obtained from {@link #computeOptimalCameraMatrix(CameraMatrix, DistortionModel, Size2i, float, Size2i, Rect2f, boolean)})
	 * @return the list of undistorted points (same reference as <code>undistorted</code>
	 * @see #undistortPoints(List, List, CameraMatrix, DistortionModel, Matrix3f, Matrix3f, TerminationCriteria)
	 */
	public static List<Vector2f> undistortPoints(List<Vector2f> distorted, List<Vector2f> undistorted, 
			CameraMatrix cameraMatrix, DistortionModel distortionModel, CameraMatrix newCameraMatrix){

		return undistortPoints(distorted, undistorted, cameraMatrix, distortionModel, null, (newCameraMatrix != null)?newCameraMatrix.getMatrix():null, null);

	}
	
	/**
	 * Undistort the points that are given in parameters.
	 * <br>
	 * <br>
	 * This implementation is a JMonkey Java port of <a href="https://github.com/opencv/opencv/blob/4.x/modules/calib3d/src/undistort.dispatch.cpp">OpenCV cvUndistortPointsInternal</a> function.
	 * @param distorted a list of 2D point affected by the distortion
	 * @param undistorted a list of 2D point that are not by the distortion (if equals to <code>distorted</code>, the points are undistorted in place)
	 * @param cameraMatrix the camera matrix
	 * @param distortionModel the distortion model
	 * @param R the camera rotation matrix
	 * @param P the new camera matrix (computed from #)
	 * @param criteria the optimization termination criteria
	 * @return the list of undistorted points (same reference as <code>undistorted</code>
	 */
	public static List<Vector2f> undistortPoints(List<Vector2f> distorted, List<Vector2f> undistorted, 
			CameraMatrix cameraMatrix, DistortionModel distortionModel, 
			Matrix3f R, Matrix3f P, 
			TerminationCriteria criteria) {

		double k1 = distortionModel.getK1();
		double k2 = distortionModel.getK2();
		double p1 = distortionModel.getP1();
		double p2 = distortionModel.getP2();
		double k3 = distortionModel.getK3();
		double k4 = distortionModel.getK4();
		double k5 = distortionModel.getK5();
		double k6 = distortionModel.getK6();
		double s1 = distortionModel.getS1();
		double s2 = distortionModel.getS2();
		double s3 = distortionModel.getS3();
		double s4 = distortionModel.getS4();

		boolean distortionTilt = (distortionModel.getTx() != 0 || distortionModel.getTy() != 0);
		
		if (criteria == null) {
			criteria = new TerminationCriteria(0.01f, 5);
		}

		// Compute Tilt matrix
		Matrix3f invMatTilt = new Matrix3f();		
		Matrix3f matTilt = new Matrix3f();

		// Using double values
		double invMatTilt00 = 1.0d;
		double invMatTilt01 = 0.0d;
		double invMatTilt02 = 0.0d;
		double invMatTilt10 = 0.0d;
		double invMatTilt11 = 1.0d;
		double invMatTilt12 = 0.0d;
		double invMatTilt20 = 0.0d;
		double invMatTilt21 = 0.0d;
		double invMatTilt22 = 1.0d;
						
		double matTilt00 = 1.0d;
		double matTilt01 = 0.0d;
		double matTilt02 = 0.0d;
		double matTilt10 = 0.0d;
		double matTilt11 = 1.0d;
		double matTilt12 = 0.0d;
		double matTilt20 = 0.0d;
		double matTilt21 = 0.0d;
		double matTilt22 = 1.0d;
		
		if (distortionTilt) {
			computeTiltProjectionMatrix(distortionModel.getTx(), distortionModel.getTy(), matTilt, null, null, invMatTilt);
			
			invMatTilt00 = invMatTilt.get(0, 0);
			invMatTilt01 = invMatTilt.get(0, 1);
			invMatTilt02 = invMatTilt.get(0, 2);
			invMatTilt10 = invMatTilt.get(1, 0);
			invMatTilt11 = invMatTilt.get(1, 1);
			invMatTilt12 = invMatTilt.get(1, 2);
			invMatTilt20 = invMatTilt.get(2, 0);
			invMatTilt21 = invMatTilt.get(2, 1);
			invMatTilt22 = invMatTilt.get(2, 2);
			
			matTilt00 = matTilt.get(0, 0);
			matTilt01 = matTilt.get(0, 1);
			matTilt02 = matTilt.get(0, 2);
			matTilt10 = matTilt.get(1, 0);
			matTilt11 = matTilt.get(1, 1);
			matTilt12 = matTilt.get(1, 2);
			matTilt20 = matTilt.get(2, 0);
			matTilt21 = matTilt.get(2, 1);
			matTilt22 = matTilt.get(2, 2);
		}

		// Integrate projection matrix (in case of rectified images)		
		if (P != null) {

			if (R != null) {
				R.multLocal(P);
			} else {
				R = P;
			}
		}

		// Initialize undistorted points list if needed
		if (undistorted == null) {
			undistorted = new ArrayList<Vector2f>(distorted.size());
		}

		double fx = cameraMatrix.getFx();
		double fy = cameraMatrix.getFy();

		double ifx = 1.0/fx;
		double ify = 1.0/fy;

		double cx = cameraMatrix.getCx();
		double cy = cameraMatrix.getCy();

		int i = 0;
		for(Vector2f point : distorted) {

			double x = point.x;
			double y = point.y;
			double x0 = 0;
			double y0 = 0;
			double u;
			double v;

			u = x; 
			v = y;

			x = (x - cx)*ifx;
			y = (y - cy)*ify;

			// Compensate distortion
			if (distortionModel != null) {

				// 1. Compensate tilt distortion
				if (distortionTilt) {
	        	
		        	double vecUntiltX = invMatTilt00*x + invMatTilt01*y + invMatTilt02;
		        	double vecUntiltY = invMatTilt10*x + invMatTilt11*y + invMatTilt12;
		        	double vecUntiltZ = invMatTilt20*x + invMatTilt21*y + invMatTilt22;
		        	
		        	double invProj = vecUntiltZ != 0 ? 1.0/vecUntiltZ : 1.0;
		        	
		        	x0 = invProj * vecUntiltX;
		            y0 = invProj * vecUntiltY;
		            
		            x = x0;
		            y = y0;
		            				
				} else {
					x0 = x;
					y0 = y;
				}
				
				// 2. Compensate radial distortion
				double error = Double.MAX_VALUE;

				for( int j = 0; ; j++ ) {

					// Terminate the optimization process according the termination criteria
					if (((criteria.getType() == TerminationCriteria.TYPE_COUNT)||(criteria.getType() == TerminationCriteria.TYPE_COUNT_OR_EPSILON)) && j >= criteria.getIterationsCount())
						break;

					if (((criteria.getType() == TerminationCriteria.TYPE_EPSILON)||(criteria.getType() == TerminationCriteria.TYPE_COUNT_OR_EPSILON)) && error < criteria.getEpsilonValue())
						break;


					double r2 = x*x + y*y;
					double icdist = (1 + ((k6*r2 + k5)*r2 + k4)*r2)/(1 + ((k3*r2 + k2)*r2 + k1)*r2);

					if (icdist < 0) {  // test: undistortPoints.regression_14583

						x = (u - cx)*ifx;
						y = (v - cy)*ify;
						break;
					}

					double deltaX = 2*p1*x*y + p2*(r2 + 2*x*x)+ s1*r2+s2*r2*r2;
					double deltaY = p1*(r2 + 2*y*y) + 2*p2*x*y+ s3*r2+s4*r2*r2;

					x = (x0 - deltaX)*icdist;
					y = (y0 - deltaY)*icdist;

					if((criteria.getType() == TerminationCriteria.TYPE_EPSILON) || (criteria.getType() == TerminationCriteria.TYPE_COUNT_OR_EPSILON)) {
						double r4, r6, a1, a2, a3, cdist, icdist2;
						double xd, yd, xd0, yd0;

						r2 = x*x + y*y;
						r4 = r2*r2;
						r6 = r4*r2;
						a1 = 2*x*y;
						a2 = r2 + 2*x*x;
						a3 = r2 + 2*y*y;
						cdist = 1 + k1*r2 + k2*r4 + k3*r6;
						icdist2 = 1.0/(1 + k4*r2 + k5*r4 + k6*r6);
						xd0 = x*cdist*icdist2 + p1*a1 + p2*a2 + s1*r2+s2*r4;
						yd0 = y*cdist*icdist2 + p1*a3 + p2*a1 + s3*r2+s4*r4;

						if (distortionTilt) {
							double vecTiltX = matTilt00*xd0 + matTilt01*yd0 + matTilt02;
	                    	double vecTiltY = matTilt10*xd0 + matTilt11*yd0 + matTilt12;
	                    	double vecTiltZ = matTilt20*xd0 + matTilt21*yd0 + matTilt22;
	                    	
	                    	
		                    double invProj = (vecTiltZ != 0.0d) ? 1.0d/vecTiltZ : 1.0d;
		                    
		                    xd = invProj * vecTiltX;
		                    yd = invProj * vecTiltY;
		                    
						} else {
							xd = xd0;
							yd = yd0;
						}
						

						double x_proj = xd*fx + cx;
						double y_proj = yd*fy + cy;

						error = Math.sqrt((x_proj - u)*(x_proj - u) + (y_proj - v)*(y_proj - v));
					}
				}

			}

			// 3. Apply rotation / projection (in case of rectified images
			if( R != null) {
				double xx = R.get(0,0)*x + R.get(0,1)*y + R.get(0,2);
				double yy = R.get(1,0)*x + R.get(1,1)*y + R.get(1,2);
				double ww = 1.0d/(R.get(2,0)*x + R.get(2,1)*y + R.get(2,2));
				x = xx*ww;
				y = yy*ww;
			}

			// 4. Update the result
			if (undistorted == distorted) {
				point.set((float)x, (float)y); // Undidstort points in place
			} else {
				undistorted.add(new Vector2f((float)x, (float)y));
			}


			i = i+1;
		}

		return undistorted;

	}



	public static Rect2f[] computeUndistortRectangles(CameraMatrix cameraMatrix, DistortionModel _distCoeffs,
			Matrix3f R, CameraMatrix newCameraMatrix, Size2i imgSize, 
			Rect2f inner, Rect2f outer ) {

		int N = 9;

		List<Vector2f> pts = new ArrayList<Vector2f>();

		// generate a grid of points across the image to estimate the distortion deformation
		float stepX = (imgSize.getWidth() - 1.0f) / ((float)(N - 1));
		float stepY = (imgSize.getHeight() - 1.0f) / ((float)(N - 1));

		int x, y;
		for( y = 0; y < N; y++ ) {
			for( x = 0; x < N; x++ ) {
				if (x != 0 && x != N - 1 && y != 0 && y != N - 1) {
					// skip all points except those on the image border, because inner grid points
					// have no influence on the two deformation rectangles that are calculated below
					continue;
				}

				pts.add(new Vector2f(x * stepX, y * stepY));
			}
		}


		for(Vector2f point : pts) {
			System.out.println("Distorted Point: "+point);
		}

		undistortPoints(pts, pts, cameraMatrix, _distCoeffs, R, (newCameraMatrix != null) ? newCameraMatrix.getMatrix(): null, null);

		for(Vector2f point : pts) {
			System.out.println("Unistorted Point: "+point);
		}

		float iX0 = -Float.MAX_VALUE;
		float iX1 =  Float.MAX_VALUE;

		float iY0 = -Float.MAX_VALUE;
		float iY1 =  Float.MAX_VALUE;

		float oX0 =  Float.MAX_VALUE;
		float oX1 = -Float.MAX_VALUE;
		float oY0 =  Float.MAX_VALUE;
		float oY1 = -Float.MAX_VALUE;

		// find the inscribed rectangle.
		// the code will likely not work with extreme rotation matrices (R) (>45%)
		int k = 0;
		for( y = 0; y < N; y++ ) {
			for( x = 0; x < N; x++ ) {
				if (x != 0 && x != N - 1 && y != 0 && y != N - 1) {
					continue;
				}

				Vector2f p = pts.get(k);

				oX0 = Math.min(oX0, p.x);
				oX1 = Math.max(oX1, p.x);
				oY0 = Math.min(oY0, p.y);
				oY1 = Math.max(oY1, p.y);

				if( x == 0 )
					iX0 = Math.max(iX0, p.x);

				if( x == N-1 )
					iX1 = Math.min(iX1, p.x);

				if( y == 0 )
					iY0 = Math.max(iY0, p.y);

				if( y == N-1 )
					iY1 = Math.min(iY1, p.y);

				k = k + 1;

			}
		}

		System.out.println("");
		System.out.println("ix0: "+iX0);
		System.out.println("ix1: "+iX1);	
		System.out.println("iy0: "+iY0);
		System.out.println("iy1: "+iY1);
		System.out.println("");
		System.out.println("ox0: "+oX0);
		System.out.println("ox1: "+oX1);	
		System.out.println("oy0: "+oY0);
		System.out.println("oy1: "+oY1);
		
		if (inner == null) {
			inner = new Rect2f();
		}

		inner.setValues(iX0, iY0, iX1-iX0, iY1-iY0);

		if (outer == null) {
			outer = new Rect2f();
		}

		outer.setValues(oX0, oY0, oX1-oX0, oY1-oY0);

		System.out.println("");
		System.out.println("Inner: "+inner);
		System.out.println("Outer: "+outer);

		return new Rect2f[] {inner, outer};

	}

	/**
	 * Computes and returns a new camera intrinsic matrix based on the free scaling parameter. 
	 * This methods is widely used to return a camera matrix that correspond to undistorted images. 
	 * @param cameraMatrix The input {@link CameraMatrix camera matrix}
	 * @param distortionModel the camera {@link DistortionModel distortion model}
	 * @param imgSize the size of original (distorted) images
	 * @param alpha a free scaling parameter between <code>0</code> (when all the pixels in the undistorted image are valid) and <code>1</code> (when all the source image pixels are retained in the undistorted image).
	 * @param newImgSize the expected size of the undistorted images (by default equals to <code>imgSize</code>)
	 * @param validPixROI a rectangle that outlines all-good-pixels region in the undistorted image (output)
	 * @param centerPrincipalPoint a flag that indicates whether in the new camera intrinsic matrix the principal point should be at the image center (<code>true</code>) or not (<code>false</code>). By default, the principal point is chosen to best fit a subset of the source image (determined by alpha) to the corrected image
	 * @return the new camera intrinsic matrix based on the free scaling parameter
	 */
	public static CameraMatrix computeOptimalCameraMatrix(CameraMatrix cameraMatrix, DistortionModel distortionModel, Size2i imgSize,
			float alpha, Size2i newImgSize, Rect2f validPixROI, boolean centerPrincipalPoint) {


		Rect2f inner = new Rect2f();
		Rect2f outer = new Rect2f();

		CameraMatrix newCameraMatrix = null;

		if ((newImgSize == null) || (newImgSize.getWidth()*newImgSize.getHeight() == 0)){
			newImgSize = imgSize;
		} 

		if (centerPrincipalPoint) {
			float cx0 = cameraMatrix.getCx();
			float cy0 = cameraMatrix.getCy();
			float cx = (newImgSize.getWidth() - 1) * 0.5f;
			float cy = (newImgSize.getWidth() - 1) * 0.5f;

			computeUndistortRectangles(cameraMatrix, distortionModel, null, cameraMatrix, imgSize, inner, outer);

			float s0 = Math.max(Math.max(Math.max((float)cx/(cx0 - inner.getX()), 
					(float)cy/(cy0 - inner.getY())),
					(float)cx/(inner.getX() + inner.getWidth() - cx0)),
					(float)cy/(inner.getY() + inner.getHeight() - cy0));

			float s1 = Math.min(Math.min(Math.min((float)cx/(cx0 - outer.getX()), (float)cy/(cy0 - outer.getY())),
					(float)cx/(outer.getX() + outer.getWidth() - cx0)),
					(float)cy/(outer.getY() + outer.getHeight() - cy0));

			float s = s0 * (1 - alpha) + s1 * alpha;

			newCameraMatrix = new CameraMatrix(cameraMatrix.getFx() * s, cameraMatrix.getFy() * s, cx, cy);

			if( validPixROI != null) {
				inner = new Rect2f(((inner.getX() - cx0)*s + cx), ((inner.getY() - cy0)*s + cy), (inner.getWidth()*s), (inner.getHeight()*s));

				Rect2f r = new Rect2f(FastMath.ceil(inner.getX()), FastMath.ceil(inner.getY()), FastMath.floor(inner.getWidth()), FastMath.floor(inner.getHeight()));

				r = r.intersection(new Rect2f(0, 0, newImgSize.getWidth(), newImgSize.getHeight()));

				validPixROI.setValues(r.getX(), r.getY(), r.getWidth(), r.getHeight());
			}
		} else {

			// Get inscribed and circumscribed rectangles in normalized
			// (independent of camera matrix) coordinates
			computeUndistortRectangles(cameraMatrix, distortionModel, null, null, imgSize, inner, outer );

			System.out.println("Inner: "+inner);
			System.out.println("Outer: "+outer);


			// Projection mapping inner rectangle to viewport
			float fx0 = (newImgSize.getWidth()  - 1) / inner.getWidth();
			float fy0 = (newImgSize.getHeight() - 1) / inner.getHeight();
			float cx0 = -fx0 * inner.getX();
			float cy0 = -fy0 * inner.getY();

			// Projection mapping outer rectangle to viewport
			float fx1 = (newImgSize.getWidth()  - 1) / outer.getWidth();
			float fy1 = (newImgSize.getHeight() - 1) / outer.getHeight();
			float cx1 = -fx1 * outer.getX();
			float cy1 = -fy1 * outer.getY();

			// Interpolate between the two optimal projections
			newCameraMatrix = new CameraMatrix(fx0*(1 - alpha) + fx1*alpha, 
					fy0*(1 - alpha) + fy1*alpha, 
					cx0*(1 - alpha) + cx1*alpha, 
					cy0*(1 - alpha) + cy1*alpha);

			if (validPixROI != null) {
				computeUndistortRectangles(cameraMatrix, distortionModel, null, newCameraMatrix, imgSize, inner, outer );
				Rect2f r = inner;

				r = new Rect2f(0, 0, newImgSize.getWidth(), newImgSize.getHeight());

				validPixROI.setValues(r.getX(), r.getY(), r.getWidth(), r.getHeight());
			}
		}

		return newCameraMatrix;
	}
}
