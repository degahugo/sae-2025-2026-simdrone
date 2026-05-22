package fr.app.model;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.util.Random;

public class CurrentMatrix {

    private final int width;
    private final int length;
    private final float halfWidthInWorld;
    private final float halfLengthInWorld;
    private final float cellSize; // Taille d'une case en unités jME3 (ex: 10f)
    private final Vector3f[][] grid;
    /**
     * Cap directeur
     */
    private float globalDirectionAngle;

    // Objet pour générer de l'aléatoire
    private final Random random = new Random();

    public CurrentMatrix(int width, int length, float cellSize) {
        this.width = width;
        this.length = length;
        this.cellSize = cellSize;
        this.halfWidthInWorld = (width * cellSize) / 2f;
        this.halfLengthInWorld = (length * cellSize) / 2f;
        this.grid = new Vector3f[width][length];
        // Choisi un cap aléatoire pour générer les courants au début
        //random.nextFloat() donne une valeur entre 0.0 et 1.0
        this.globalDirectionAngle = random.nextFloat() * 360f;

        // Initialisation par défaut (courant nul)
        generateRandomCurrents();
    }

    /**
     * Génère aléatoirement des courants
     */
    public void generateRandomCurrents() {
        // Le cap directeur va varier de 30 degré maximum
        float angleChange = (random.nextFloat() * 60f) - 30f;
        this.globalDirectionAngle = (this.globalDirectionAngle + angleChange) % 360f;
        if (this.globalDirectionAngle < 0) this.globalDirectionAngle += 360f;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                float localVariation = (random.nextFloat() * 40f) - 20f;
                float finalCaseAngle = (this.globalDirectionAngle + localVariation) % 360f;
                float radians = finalCaseAngle * FastMath.DEG_TO_RAD;
                float forceX = FastMath.cos(radians) * 0.5f;
                float forceZ = FastMath.sin(radians) * 0.5f;

                grid[x][z] = new Vector3f(forceX, 0f, forceZ);
            }
        }
    }

    /**
     * Récupère le vecteur de courant à une position 3D globale de la simulation.
     * Pour l'instant, le paramètre Y (hauteur) est ignoré, mais prêt pour le futur.
     */
    public Vector3f getCurrentAt(Vector3f position) {
        int gridX = (int) ((position.x + halfWidthInWorld) / cellSize);
        int gridZ = (int) ((position.z + halfLengthInWorld) / cellSize);
        // On évite les sorties de tableau en mettant un courant de 0
        if (gridX < 0 || gridX >= width || gridZ < 0 || gridZ >= length) {
            return Vector3f.ZERO.clone();
        }
        return grid[gridX][gridZ];
    }

    /**
     * Permet de modifier le courant d'une case de la grille plus tard
     * @param x Position X de la case
     * @param z Position Z de la case
     * @param current Nouveau vecteur de courant
     */
    public void setCurrentAtGrid(int x, int z, Vector3f current) {
        if (x >= 0 && x < width && z >= 0 && z < length) {
            this.grid[x][z] = current;
        }
    }

    /**
     * Donne la largeur de la matrice de courants.
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Donne la longueur de la matrice de courants.
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Donne la taille des cellules de la matrice de courants
     */
    public float getCellSize() {
        return this.cellSize;
    }

    /**
     * Donne l'offset x de la map
     */
    public float getHalfWidthInWorld() {
        return this.halfWidthInWorld;
    }

    /**
     * Donne l'offset z de la map
     */
    public float getHalfLengthInWorld() {
        return this.halfLengthInWorld;
    }
}
