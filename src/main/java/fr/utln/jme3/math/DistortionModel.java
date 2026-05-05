package fr.utln.jme3.math;

/**
 * This class represents a <a href="https://web.archive.org/web/20180312205006/https://www.asprs.org/wp-content/uploads/pers/1966journal/may/1966_may_444-462.pdf">Brown-Conrady distortion model</a>.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 * @see CameraMatrix
 */
public class DistortionModel {

	/**
	 * The K<sub>1</sub> radial distortion coefficient.
	 * @see #k2
	 * @see #k3
	 */
	private float k1 = 0.0f;

	/**
	 * The K<sub>2</sub> radial distortion coefficient.
	 * @see #k1
	 * @see #k3
	 */
	private float k2 = 0.0f;
	
	/**
	 * The K<sub>3</sub> radial distortion coefficient.
	 * @see #k1
	 * @see #k2
	 */
	private float k3 = 0.0f;
	
	/**
	 * The K<sub>4</sub> radial rational distortion coefficient.
	 * @see #k5
	 * @see #k6
	 */
	private float k4 = 0.0f;
	
	/**
	 * The K<sub>5</sub> radial rational distortion coefficient.
	 * @see #k4
	 * @see #k6
	 */
	private float k5 = 0.0f;
	
	/**
	 * The K<sub>6</sub> radial rational distortion coefficient.
	 * @see #k4
	 * @see #k5
	 */
	private float k6 = 0.0f;
	
	/**
	 * The P<sub>1</sub> tangential distortion coefficient.
	 * @see #p2
	 */
	private float p1 = 0.0f;
	
	
	/**
	 * The P<sub>2</sub> tangential distortion coefficient.
	 * @see #p1
	 */
	private float p2 = 0.0f;
	
	/**
	 * The S<sub>1</sub> thin prism distortion coefficient.
	 * @see #s2
	 * @see #s3
	 * @see #s4
	 */
	private float s1 = 0.0f;
	
	/**
	 * The S<sub>2</sub> thin prism distortion coefficient.
	 * @see #s1
	 * @see #s3
	 * @see #s4
	 */
	private float s2 = 0.0f;
	
	/**
	 * The S<sub>3</sub> thin prism distortion coefficient.
	 * @see #s1
	 * @see #s2
	 * @see #s4
	 */
	private float s3 = 0.0f;
	
	/**
	 * The S<sub>4</sub> thin prism distortion coefficient.
	 * @see #s1
	 * @see #s2
	 * @see #s3
	 */
	private float s4 = 0.0f;

	/**
	 * The T<sub>x</sub> tilt distortion coefficient.
	 * @see #ty
	 */
	private float tx = 0.0f;
	
	/**
	 * The T<sub>y</sub> tilt distortion coefficient.
	 * @see #tx
	 */
	private float ty = 0.0f;
	
	/**
	 * Create a new distortion model with all coefficients set to <code>0.0</code>.
	 */
    public DistortionModel() {
    }

	/**
	 * Create a new distortion model with default OpenCV radial / tangential distortion coefficients.
	 * @param k1 the K<sub>1</sub> radial distortion coefficient
	 * @param k2 the K<sub>2</sub> radial distortion coefficient
	 * @param p1 the P<sub>1</sub> tangential distortion coefficient
	 * @param p2 the P<sub>2</sub> tangential distortion coefficient
	 */
    public DistortionModel(float k1, float k2, float p1, float p2) {
        this.k1 = k1;
        this.k2 = k2;
        this.p1 = p1;
        this.p2 = p2;  
    }
    
	/**
	 * Create a new distortion model with OpenCV radial / tangential distortion coefficients.
	 * @param k1 the K<sub>1</sub> radial distortion coefficient
	 * @param k2 the K<sub>2</sub> radial distortion coefficient
	 * @param k3 the K<sub>3</sub> radial distortion coefficient
	 * @param p1 the P<sub>1</sub> tangential distortion coefficient
	 * @param p2 the P<sub>2</sub> tangential distortion coefficient
	 */
    public DistortionModel(float k1, float k2, float k3, float p1, float p2) {
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.p1 = p1;
        this.p2 = p2;  
    }
    
	/**
	 * Create a new distortion model with OpenCV rational radial / tangential distortion coefficients.
	 * @param k1 the K<sub>1</sub> radial distortion coefficient
	 * @param k2 the K<sub>2</sub> radial distortion coefficient
	 * @param k3 the K<sub>3</sub> radial distortion coefficient
	 * @param k4 the K<sub>4</sub> radial rational distortion coefficient
	 * @param k5 the K<sub>5</sub> radial rational distortion coefficient
	 * @param k6 the K<sub>6</sub> radial rational distortion coefficient
	 * @param p1 the P<sub>1</sub> tangential distortion coefficient
	 * @param p2 the P<sub>2</sub> tangential distortion coefficient
	 */
    public DistortionModel(float k1, float k2, float k3, float k4, float k5, float k6, float p1, float p2) {
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.k4 = k4;
        this.k5 = k5;
        this.k6 = k6;
        this.p1 = p1;
        this.p2 = p2;  
    }
    
	/**
	 * Create a new distortion model with OpenCV rational radial / tangential / thin prism distortion coefficients.
	 * @param k1 the K<sub>1</sub> radial distortion coefficient
	 * @param k2 the K<sub>2</sub> radial distortion coefficient
	 * @param k3 the K<sub>3</sub> radial distortion coefficient
	 * @param k4 the K<sub>4</sub> radial rational distortion coefficient
	 * @param k5 the K<sub>5</sub> radial rational distortion coefficient
	 * @param k6 the K<sub>6</sub> radial rational distortion coefficient
	 * @param p1 the P<sub>1</sub> tangential distortion coefficient
	 * @param p2 the P<sub>2</sub> tangential distortion coefficient
	 * @param s1 the S<sub>1</sub> thin prism distortion coefficient
	 * @param s2 the S<sub>2</sub> thin prism distortion coefficient
	 * @param s3 the S<sub>3</sub> thin prism distortion coefficient
	 * @param s4 the S<sub>4</sub> thin prism distortion coefficient
	 */
    public DistortionModel(float k1, float k2, float k3, float k4, float k5, float k6, float p1, float p2, float s1, float s2, float s3, float s4) {
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.k4 = k4;
        this.k5 = k5;
        this.k6 = k6;
        this.p1 = p1;
        this.p2 = p2;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
    }
  
	/**
	 * Create a new distortion model with OpenCV rational radial / tangential / thin prism and tilt distortion coefficients.
	 * @param k1 the K<sub>1</sub> radial distortion coefficient
	 * @param k2 the K<sub>2</sub> radial distortion coefficient
	 * @param k3 the K<sub>3</sub> radial distortion coefficient
	 * @param k4 the K<sub>4</sub> radial rational distortion coefficient
	 * @param k5 the K<sub>5</sub> radial rational distortion coefficient
	 * @param k6 the K<sub>6</sub> radial rational distortion coefficient
	 * @param p1 the P<sub>1</sub> tangential distortion coefficient
	 * @param p2 the P<sub>2</sub> tangential distortion coefficient
	 * @param s1 the S<sub>1</sub> thin prism distortion coefficient
	 * @param s2 the S<sub>2</sub> thin prism distortion coefficient
	 * @param s3 the S<sub>3</sub> thin prism distortion coefficient
	 * @param s4 the S<sub>4</sub> thin prism distortion coefficient
	 * @param tx the T<sub>x</sub> tilt distortion coefficient
	 * @param ty the T<sub>y</sub> tilt prism distortion coefficient
	 */
    public DistortionModel(float k1, float k2, float k3, float k4, float k5, float k6, float p1, float p2, float s1, float s2, float s3, float s4, float tx, float ty) {
        this.k1 = k1;
        this.k2 = k2;
        this.k3 = k3;
        this.k4 = k4;
        this.k5 = k5;
        this.k6 = k6;
        this.p1 = p1;
        this.p2 = p2;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
        this.tx = tx;
        this.ty = ty;
    }
 
    
	/**
	 * Get the K<sub>1</sub> radial distortion coefficient.
	 * @see #setK1(float)
	 * @see #getK2()
	 * @see #getK3()
	 */
	public float getK1() {
		return k1;
	}

	/**
	 * Set the K<sub>1</sub> radial distortion coefficient.
	 * @see #getK1()
	 * @see #setK2(float)
	 * @see #setK3(float)
	 */
	public void setK1(float k1) {
		this.k1 = k1;
	}

	/**
	 * Get the K<sub>2</sub> radial distortion coefficient.
	 * @see #setK2(float)
	 * @see #getK1()
	 * @see #getK3()
	 */
	public float getK2() {
		return k2;
	}

	/**
	 * Set the K<sub>2</sub> radial distortion coefficient.
	 * @see #getK2()
	 * @see #setK1(float)
	 * @see #setK3(float)
	 */
	public void setK2(float k2) {
		this.k2 = k2;
	}

	/**
	 * Get the K<sub>3</sub> radial distortion coefficient.
	 * @see #setK3(float)
	 * @see #getK1()
	 * @see #getK2()
	 */
	public float getK3() {
		return k3;
	}

	/**
	 * Set the K<sub>3</sub> radial distortion coefficient.
	 * @see #getK3()
	 * @see #setK1(float)
	 * @see #setK2(float)
	 */
	public void setK3(float k3) {
		this.k3 = k3;
	}

	/**
	 * Get the K<sub>4</sub> radial rational distortion coefficient.
	 * @see #setK4(float)
	 * @see #getK5()
	 * @see #getK6()
	 */
	public float getK4() {
		return k4;
	}

	/**
	 * Set the K<sub>4</sub> radial rational distortion coefficient.
	 * @see #getK4()
	 * @see #setK3(float)
	 * @see #setK6(float)
	 */
	public void setK4(float k4) {
		this.k4 = k4;
	}

	/**
	 * Get the K<sub>5</sub> radial rational distortion coefficient.
	 * @see #setK5(float)
	 * @see #getK4()
	 * @see #getK6()
	 */
	public float getK5() {
		return k5;
	}

	/**
	 * Set the K<sub>5</sub> radial rational distortion coefficient.
	 * @see #getK5()
	 * @see #setK4(float)
	 * @see #setK6(float)
	 */
	public void setK5(float k5) {
		this.k5 = k5;
	}

	/**
	 * Get the K<sub>6</sub> radial rational distortion coefficient.
	 * @see #setK6(float)
	 * @see #getK4()
	 * @see #getK5()
	 */
	public float getK6() {
		return k6;
	}

	/**
	 * Set the K<sub>6</sub> radial rational distortion coefficient.
	 * @see #getK6()
	 * @see #setK4(float)
	 * @see #setK5(float)
	 */
	public void setK6(float k6) {
		this.k6 = k6;
	}

	/**
	 * Get the P<sub>1</sub> tangential distortion coefficient.
	 * @see #setP1(float)
	 * @see #getP2()
	 */
	public float getP1() {
		return p1;
	}

	/**
	 * Set the P<sub>1</sub> tangential distortion coefficient.
	 * @see #getP1()
	 * @see #setP2(float)
	 */
	public void setP1(float p1) {
		this.p1 = p1;
	}

	/**
	 * Get the P<sub>2</sub> tangential distortion coefficient.
	 * @see #setP2(float)
	 * @see #getP1()
	 */
	public float getP2() {
		return p2;
	}

	/**
	 * Set the P<sub>2</sub> tangential distortion coefficient.
	 * @see #getP2()
	 * @see #setP1(float)
	 */
	public void setP2(float p2) {
		this.p2 = p2;
	}

	/**
	 * Get the S<sub>1</sub> thin prism distortion coefficient.
	 * @return the S<sub>1</sub> thin prism distortion coefficient
	 * @see #setS1(float)
	 * @see #getS2()
	 * @see #getS3()
	 * @see #getS4()
	 */
	public float getS1() {
		return s1;
	}

	/**
	 * Set the S<sub>1</sub> thin prism distortion coefficient.
	 * @param s1 the S<sub>1</sub> thin prism distortion coefficient
	 * @see #getS1(float)
	 * @see #setS2(float)
	 * @see #setS3(float)
	 * @see #setS4(float)
	 */
	public void setS1(float s1) {
		this.s1 = s1;
	}

	/**
	 * Get the S<sub>2</sub> thin prism distortion coefficient.
	 * @return the S<sub>2</sub> thin prism distortion coefficient
	 * @see #setS2(float)
	 * @see #getS1()
	 * @see #getS3()
	 * @see #getS4()
	 */
	public float getS2() {
		return s2;
	}

	
	/**
	 * Set the S<sub>2</sub> thin prism distortion coefficient.
	 * @param s1 the S<sub>2</sub> thin prism distortion coefficient
	 * @see #getS2(float)
	 * @see #setS1(float)
	 * @see #setS3(float)
	 * @see #setS4(float)
	 */
	public void setS2(float s2) {
		this.s2 = s2;
	}

	/**
	 * Get the S<sub>3</sub> thin prism distortion coefficient.
	 * @return the S<sub>3</sub> thin prism distortion coefficient
	 * @see #setS3(float)
	 * @see #getS1()
	 * @see #getS2()
	 * @see #getS4()
	 */
	public float getS3() {
		return s3;
	}

	/**
	 * Set the S<sub>3</sub> thin prism distortion coefficient.
	 * @param s1 the S<sub>3</sub> thin prism distortion coefficient
	 * @see #getS3(float)
	 * @see #setS1(float)
	 * @see #setS2(float)
	 * @see #setS4(float)
	 */
	public void setS3(float s3) {
		this.s3 = s3;
	}

	/**
	 * Get the S<sub>4</sub> thin prism distortion coefficient.
	 * @return the S<sub>4</sub> thin prism distortion coefficient
	 * @see #setS4(float)
	 * @see #getS1()
	 * @see #getS2()
	 * @see #getS3()
	 */
	public float getS4() {
		return s4;
	}

	/**
	 * Set the S<sub>4</sub> thin prism distortion coefficient.
	 * @param s1 the S<sub>4</sub> thin prism distortion coefficient
	 * @see #getS4(float)
	 * @see #setS1(float)
	 * @see #setS2(float)
	 * @see #setS3(float)
	 */
	public void setS4(float s4) {
		this.s4 = s4;
	}

	/**
	 * Get the T<sub>x</sub> tilt distortion coefficient.
	 * @return the T<sub>x</sub> tilt distortion coefficient
	 * @see #setTx(float)
	 * @see #getTy()
	 */
	public float getTx() {
		return tx;
	}

	/**
	 * Set the T<sub>x</sub> tilt distortion coefficient.
	 * @param tx the T<sub>x</sub> tilt distortion coefficient
	 * @see #getTx()
	 * @see #setTy(float)
	 */
	public void setTx(float tx) {
		this.tx = tx;
	}

	/**
	 * Get the T<sub>y</sub> tilt distortion coefficient.
	 * @return the T<sub>y</sub> tilt distortion coefficient
	 * @see #setTy(float)
	 * @see #getTx()
	 */
	public float getTy() {
		return ty;
	}

	/**
	 * Set the T<sub>y</sub> tilt distortion coefficient.
	 * @param ty the T<sub>y</sub> tilt distortion coefficient
	 * @see #getTy()
	 * @see #setTx(float)
	 */
	public void setTy(float ty) {
		this.ty = ty;
	}
}
