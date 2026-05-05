package fr.utln.jme3.math;

/**
 * A 2D size with integer coordinates. Such a size is defined an horizontal length (width) a vertical length (height).
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a> 
 */
public class Size2i {

	/**
	 * The horizontal length (width) of the size.
	 * @see #height
	 */
	private int width;
	
	/**
	 * The vertical length (height) of the size.
	 * @see #width
	 */
	private int height;
	
	/**
	 * Creates a new size with its dimensions (width, height) set to <code>0</code>.
	 */
	public Size2i(){
		this.width = 0;
		this.height = 0;
	}
	
	/**
	 * Creates a new size with the specified dimensions.
	 * @param width the horizontal length (width) of the size.
	 * @param height the vertical length (height) of the size.
	 */
	public Size2i(int width, int height){
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Get the horizontal length (width) of the size.
	 * @return the horizontal length (width) of the size.
	 * @see #setWidth(int)
	 * @see #getHeight()
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Set the horizontal length (width) of the size.
	 * @param width the horizontal length (width) of the size.
	 * @see #getWidth()
	 * @see #setHeight(int)
	 * @see #setSizes(int, int)
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	
	/**
	 * Get the vertical length (height) of the size.
	 * @return the vertical length (height) of the size.
	 * @see #setHeight(int)
	 * @see #getWidth()
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Set the vertical length (height) of the size.
	 * @param height the vertical length (height) of the size.
	 * @see #getHeight()
	 * @see #setWidth(int)
	 * @see #setSizes(int, int)
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Set the dimensions of the size.
	 * @param width the horizontal length (width) of the size.
	 * @param height the vertical length (height) of the size.
	 * @see #setWidth(int)
	 * @see #setHeight(int)
	 */
	public void setSizes(int width, int height){
		this.width = width;
		this.height = height;
	}
	
}
