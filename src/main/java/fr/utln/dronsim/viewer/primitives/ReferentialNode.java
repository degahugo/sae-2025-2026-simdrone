package fr.utln.dronsim.viewer.primitives;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Line;

/**
 * A {@link Node JMonkey scene node} for representing a 3D referential.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a>
 */
public class ReferentialNode extends Node {

	  private Vector3f origin = null;
	  
	  private float unitSize  = 1.0f;
	  
	  private boolean direct  = true;
	  
	  private Geometry xgeom = null;
	  private Geometry ygeom = null;
	  private Geometry zgeom = null;
	  
	  /**
	   * The asset manager used for loading material definitions
	   */
	  private AssetManager assetManager    = null;
	  
	  /**
	   * Creates a new referential node with the given name. The unit size of the referential is set to <code>1.0</code>, its origin is set to <code>(0.0, 0.0, 0.0)</code>
	   * and it is considered as a direct referential.
	   * @param assetManager the {@link AssetManager asset manager} to use for loading {@link Material material}
	   */
	  public ReferentialNode(AssetManager assetManager){
	    this("", Vector3f.ZERO, 1.0f, true, assetManager);    
	  }
	  
	  /**
	   * Creates a new referential node with the given name. The unit size of the referential is set to <code>1.0</code>, its origin is set to <code>(0.0, 0.0, 0.0)</code>
	   * and it is considered as a direct referential.
	   * @param name the name of the referential (see {@link Node#getName() getName()})
	   * @param direct is the referential is direct (<code>true</code>) or indirect (<code>false</code>)
	   * @param assetManager the {@link AssetManager asset manager} to use for loading {@link Material material}
	   */
	  public ReferentialNode(String name, boolean direct, AssetManager assetManager){
	    this(name, Vector3f.ZERO, 1.0f, direct, assetManager);    
	  }

	  /**
	   * Creates a new referential node with the given name and the given origin. The unit size of the referential is set to <code>1.0</code> and it is considered as a direct referential.
	   * @param name the name of the referential (see {@link Node#getName() getName()})
	   * @param origin the origin of the referential
	   * @param direct is the referential is direct (<code>true</code>) or indirect (<code>false</code>)
	   * @param assetManager the {@link AssetManager asset manager} to use for loading {@link Material material}
	   */
	  public ReferentialNode(String name, Vector3f origin, boolean direct, AssetManager assetManager){
	    this(name, origin, 1.0f, direct, assetManager);
	  }
	  
	  /**
	   * Creates a new referential node with the given name, origin and unit size.
	   * @param name the name of the referential (see {@link Node#getName() getName()})
	   * @param origin the origin of the referential.
	   * @param unitSize the size on the referential unit in the world space.
	   * @param direct <code>true</code> if the referential is direct (right handed) or <code>false</code> if the referential is indirect (left handed).
	   * @param assetManager the {@link AssetManager asset manager} to use for loading {@link Material material}
	   */
	  public ReferentialNode(String name, Vector3f origin, float unitSize, boolean direct, AssetManager assetManager){
	    super(name);
	    
	    this.assetManager = assetManager;
	    
	    if (origin != null){
	      this.origin = origin;
	    }
	    
	    if (unitSize > 0){
	      this.unitSize = unitSize;
	    }
	    
	    this.direct = direct;
	     
	    createGeometry();
	  }
	  
	  /**
	   * Get the color used for X axis representation.
	   * @return the color used for X axis representation
	   */
	  public ColorRGBA getAxisXColor() {
		return (ColorRGBA)xgeom.getMaterial().getParam("Color").getValue();
	  }

	  /**
	   * Set the the color used for X axis representation.<br><br>
	   * This method change the underlying material and has to be called from the update thread.
	   * @param color the color used for X axis representation
	   */
	  public void setAxisXColor(ColorRGBA color) {
		xgeom.getMaterial().setColor("Color", color);
	  }

	  /**
	   * Get the color used for Y axis representation.
	   * @return the color used for Y axis representation
	   */
	  public ColorRGBA getAxisYColor() {
		return (ColorRGBA)ygeom.getMaterial().getParam("Color").getValue();
	  }

	  /**
	   * Set the the color used for Y axis representation.<br><br>
	   * This method change the underlying material and has to be called from the update thread.
	   * @param color the color used for Y axis representation
	   */
	  public void setAxisYColor(ColorRGBA color) {
		ygeom.getMaterial().setColor("Color", color);
	  }

	  /**
	   * Get the color used for Z axis representation.
	   * @return the color used for Z axis representation
	   */
	  public ColorRGBA getAxisZColor() {
		return (ColorRGBA)zgeom.getMaterial().getParam("Color").getValue();
	  }

	  /**
	   * Set the the color used for Z axis representation.<br><br>
	   * This method change the underlying material and has to be called from the update thread.
	   * @param color the color used for Z axis representation
	   */
	  public void setAxisZColor(ColorRGBA color) {
		zgeom.getMaterial().setColor("Color", color);
	  }

	  /**
	   * Create the geometry of the referential. 
	   */
	  private void createGeometry(){
	    
	    detachAllChildren();
	    
	    if (origin != null){
	      Vector3f origin3f = new Vector3f((float)origin.getX(), (float)origin.getY(), (float)origin.getZ());
	    
	      Line  xvector = new Line(origin3f, new Vector3f(origin3f.x+unitSize, origin3f.y, origin3f.z));
	      Line  yvector = new Line(origin3f, new Vector3f(origin3f.x, origin3f.y+unitSize, origin3f.z));
	      Line  zvector = null;

	      if (direct) {
	    	  zvector = new Line(origin3f, new Vector3f(origin3f.x, origin3f.y, origin3f.z+unitSize));
	      } else {
	    	  zvector = new Line(origin3f, new Vector3f(origin3f.x, origin3f.y, origin3f.z-unitSize));
	      }
	      
	      Material xmaterial = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	      xmaterial.setColor("Color", ColorRGBA.Red);
	      
	      xgeom = new Geometry(getName()+"_X", xvector);
	      xgeom.setMaterial(xmaterial);

	      Material ymaterial = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	      ymaterial.setColor("Color", ColorRGBA.Green);
	      ygeom = new Geometry(getName()+"_Y", yvector);
	      ygeom.setMaterial(ymaterial);
	      
	      Material zmaterial = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
	      zmaterial.setColor("Color", ColorRGBA.Blue);
	      zgeom = new Geometry(getName()+"_Z", zvector);
	      zgeom.setMaterial(zmaterial);
	      
	      attachChild(xgeom);
	      attachChild(ygeom);
	      attachChild(zgeom);
	     
	      updateModelBound();
	    }
	  }
	  
	  
}
