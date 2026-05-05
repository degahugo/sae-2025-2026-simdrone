package fr.utln.jme3.model;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * A Camera calibration chessboard.
 * @author Julien Seinturier - Universit&eacute; de Toulon / LIS umr CNRS 7020 - <a href="http://web.seinturier.fr">http://web.seinturier.fr</a>
 */
public class Chessboard extends Node{

	/**
	 * The number of squares in a column.
	 * @see #cols
	 */
    private int rows = 5; 
    
    /**
     * The number of squares in a row.
     * @see #rows
     */
    private int cols = 9;
    
    /**
	 * The size (length) of a square.
	 */
    private float size = 1.0f;
	
    /**
	 * The thickness of the chessboard.
	 */
    private float thickness = 0.1f;
    
    /**
     * Create a chessboard with the given number of rows and columns and the given size.	
     * @param assetManager the JMonkey application asset manager (for material loading)
     * @param rows the number of corners in a column
     * @param cols the number of corners in a row
     * @param size the size (length) of a square	
     */
    public Chessboard(AssetManager assetManager, int rows, int cols, float size, float thickness) {
    	
    	this.rows = rows;
    	
    	this.cols = cols;
    	
    	this.size = size;
    	
    	this.thickness = thickness;
    	
        createChessBoard(assetManager);
    }

    /**
	 * Get the number of square rows.
	 * @return the number of square rows
	 * @see #getCols()
	 * @see #getSize()
	 */
    public int getRows() {
    	return rows;
    }
    
    /**
	 * Get the number of square columns
	 * @return the number of square rows
	 * @see #getRows()
	 * @see #getSize()
	 */
    public int getCols() {
    	return cols;
    }
    
    /**
     * Get the size (length) of a square that compose the chessboard.
     * @return the size (length) of a square that compose the chessboard
     * @see #getDimensions()
     */
    public float getSize() {
		return size;
	}
    
    /**
	 * Get the number of corners of the chessboard.
	 * @return the number of corners of the chessboard
	 */
    public int getCornersCount() {
    	return (cols - 1)*(rows-1);
    }
    
    /**
	 * Get the number of corners rows.
	 * @return the number of corners corners rows
	 * @see #getCornersCols()
	 */
    public int getCornersRows() {
    	return rows-1;
    }
    
    /**
	 * Get the number of corners columns.
	 * @return the number of corners columns
	 * @see #getCornersRows()
	 */
    public int getCornersCols() {
    	return cols-1;
    }
    
    private void createChessBoard(AssetManager assetManager) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Create a box for each tile
                Box box = new Box(size / 2, size / 2, this.thickness);
                Geometry tile = new Geometry("Tile "+row+","+col, box);

                // Set the material color based on the position
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                if ((row + col) % 2 == 0) {
                    mat.setColor("Color", ColorRGBA.White);
                } else {
                    mat.setColor("Color", ColorRGBA.Black);
                }
                tile.setMaterial(mat);

                // Position the tile
                tile.setLocalTranslation(col * size, row * size, 0);

                // Attach the tile to the chessboard node
                this.attachChild(tile);
            }
        }
    }
    
}
