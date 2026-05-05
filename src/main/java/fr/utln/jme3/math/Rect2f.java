package fr.utln.jme3.math;

import com.jme3.math.Vector2f;

/**
 * A 2D rectangle with float coordinates. Such a rectangle is defined by its upper left point (x, y), an horizontal length (width, towards right) and a vertical length (height, towards bottom).
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 */
public class Rect2f {

	/**
	 * The x coordinate of the upper left point of the rectangle.
	 * @see #y
	 */
	private float x;
	
	/**
	 * The y coordinate of the upper left point of the rectangle.
	 * @see #x
	 */
	private float y;
	
	/**
	 * The horizontal length (width, towards right) of the rectangle.
	 * @see #height
	 */
	private float width;
	
	/**
	 * The vertical length  vertical length (height, towards bottom) of the rectangle.
	 * @see #width
	 */
	private float height;
	
	/**
	 * Creates a new rectangle with its origin set at <code>0</code> and its dimensions (with, length) set to <code>0</code>.
	 */
	public Rect2f(){
		this.x = 0.0f;
		this.y = 0.0f;
		
		this.width = 0.0f;
		this.height = 0.0f;
	}
	
	/**
	 * Creates a new rectangle with the specified origin and dimensions.
	 * @param x the x coordinate of the upper left point of the rectangle.
	 * @param y the y coordinate of the upper left point of the rectangle.
	 * @param width the horizontal length (width, towards right) of the rectangle.
	 * @param height the vertical length  vertical length (height, towards bottom) of the rectangle.
	 */
	public Rect2f(float x, float y, float width, float height){
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return "Rect2f[("+x+", "+y+", w:"+width+", h:"+height+"]";
	}
	
	/**
	 * Get the x coordinate of the upper left point of the rectangle.
	 * @return the x coordinate of the upper left point of the rectangle
	 * @see #setX(float)
	 * @see #setValues(float, float, float, float)
	 * @see #getY()
	 */
	public float getX() {
		return x;
	}
	
	/**
	 * Set the x coordinate of the upper left point of the rectangle.
	 * @param x the x coordinate of the upper left point of the rectangle
	 * @see #getX()
	 * @see #setValues(float, float, float, float)
	 */
	public void setX(float x) {
		this.x = x;
	}
	
	/**
	 * Get the y coordinate of the upper left point of the rectangle.
	 * @return the y coordinate of the upper left point of the rectangle
	 * @see #setY(float)
	 * @see #setValues(float, float, float, float)
	 * @see #getX()
	 */
	public float getY() {
		return y;
	}
	
	/**
	 * Set the y coordinate of the upper left point of the rectangle.
	 * @param y the y coordinate of the upper left point of the rectangle
	 * @see #getY()
	 * @see #setValues(float, float, float, float)
	 */
	public void setY(float y) {
		this.y = y;
	}
	
	/**
	 * Get the horizontal length (width, towards right) of the rectangle.
	 * @return the horizontal length (width, towards right) of the rectangle
	 * @see #setWidth(float)
	 * @see #setValues(float, float, float, float)
	 * @see #getHeight()
	 */
	public float getWidth() {
		return width;
	}
	
	/**
	 * Set the horizontal length (width, towards right) of the rectangle.
	 * @param width the horizontal length (width, towards right) of the rectangle
	 * @see #getWidth()
	 * @see #setValues(float, float, float, float)
	 */
	public void setWidth(float width) {
		this.width = width;
	}
	
	/**
	 * Get the vertical length  vertical length (height, towards bottom) of the rectangle.
	 * @return the vertical length  vertical length (height, towards bottom) of the rectangle
	 * @see #setHeight(float)
	 * @see #setValues(float, float, float, float)
	 * @see #getWidth()
	 */
	public float getHeight() {
		return height;
	}
	
	/**
	 * Set the vertical length  vertical length (height, towards bottom) of the rectangle.
	 * @param height the vertical length  vertical length (height, towards bottom) of the rectangle
	 * @see #getHeight()
	 * @see #setValues(float, float, float, float)
	 */
	public void setHeight(float height) {
		this.height = height;
	}
	
	/**
	 * Set the origin and dimensions of the rectangle.
	 * @param x the x coordinate of the upper left point of the rectangle.
	 * @param y the y coordinate of the upper left point of the rectangle.
	 * @param width the horizontal length (width, towards right) of the rectangle.
	 * @param height the vertical length  vertical length (height, towards bottom) of the rectangle.
	 */
	public void setValues(float x, float y, float width, float height){
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Check if the given <code>point</code> is inside the rectangle.
	 * @param point the point to check
	 * @return <code>true</code> if the point is contained by the rectangle and <code>false</code> otherwise (also if <code>point</code> is <code>null</code>)
	 */
	public boolean contains(Vector2f point) {
		if (point != null) {
			return x <= point.x && point.x < x + width && y <= point.y && point.y < y + height;
		}
		
		return false;
	}
	
	/**
	 * Return the intersection of this rectangle with the one given in parameter. If the rectangles are not intersecting, <code>null</code> is returned.
	 * If <code>rect</code> is <code>null</code>, then a new rectangle that is equals to this one is returned.
	 * @param rect a potential intersecting rectangle 
	 * @return the intersection of this rectangle with the one given in parameter or <code>null</code> if there is no intersection
	 */
	public Rect2f intersection(Rect2f rect) {
		
		if (rect == null) {
	        return new Rect2f(this.x, this.y, this.width, this.height);
	    }
		
		Rect2f Rx_min = (this.x < rect.x) ? this : rect;
		Rect2f Rx_max = (this.x < rect.x) ? rect : this;
		Rect2f Ry_min = (this.y < rect.y) ? this : rect;
		Rect2f Ry_max = (this.y < rect.y) ? rect : this;
		
	    // Looking at the formula below, we will compute Rx_min.width - (Rx_max.x - Rx_min.x)
	    // but we want to avoid overflows. Rx_min.width >= 0 and (Rx_max.x - Rx_min.x) >= 0
	    // by definition so the difference does not overflow. The only thing that can overflow
	    // is (Rx_max.x - Rx_min.x). And it can only overflow if Rx_min.x < 0.
	    // Let us first deal with the following case.
	    if ((Rx_min.x < 0 && Rx_min.x + Rx_min.width < Rx_max.x) ||
	        (Ry_min.y < 0 && Ry_min.y + Ry_min.height < Ry_max.y)) {
	    	
	        return null;
	    }
		
	    // We now know that either Rx_min.x >= 0, or
	    // Rx_min.x < 0 && Rx_min.x + Rx_min.width >= Rx_max.x and therefore
	    // Rx_min.width >= (Rx_max.x - Rx_min.x) which means (Rx_max.x - Rx_min.x)
	    // is inferior to a valid int and therefore does not overflow.
	    Rect2f result = new Rect2f(Rx_max.x, 
	    						   Ry_max.y, 
	    						   Math.min(Rx_min.width - (Rx_max.x - Rx_min.x), Rx_max.width), 
	    						   Math.min(Ry_min.height - (Ry_max.y - Ry_min.y), Ry_max.height));


	    // Check if the intersection is not empty
	    if (result.width * result.height == 0)
	        return null;

		return result;
	}
}
