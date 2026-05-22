package fr.app.model;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.util.Random;

public class CurrentMatrix {

    private float maxYawAngleChange;
    private float maxPitchAngleChange;
    private float currentForce;
    private final int width;
    private final int length;
    private final float halfWidthInWorld;
    private final float halfLengthInWorld;
    private final float cellSize; // Taille d'une case en unités jME3 (ex: 10f)
    private final Vector3f[][] grid;
    /**
     * Cap directeur
     */
    private float globalYawAngle;
    /**
     * Angle global
     */
    private float globalPitchAngle;

    // Objet pour générer de l'aléatoire
    private final Random random = new Random();
    // Graines pour notre bruit
    private float seedYaw;
    private float seedPitch;
    // Échelle du bruit : plus c'est petit, plus les "zones" sont grandes et douces.
    private final float noiseScale = 0.1f; // entre 0.05 et 0.2 c'est pas trop mal

    public CurrentMatrix(int width, int length, float cellSize, float maxYawAngleChange, float maxPitchAngleChange, float currentForce) {
        this.maxYawAngleChange = maxYawAngleChange;
        this.maxPitchAngleChange = maxPitchAngleChange;
        this.width = width;
        this.length = length;
        this.cellSize = cellSize;
        this.halfWidthInWorld = (width * cellSize) / 2f;
        this.halfLengthInWorld = (length * cellSize) / 2f;
        this.grid = new Vector3f[width][length];
        // Choisi un cap aléatoire pour générer les courants au début
        //random.nextFloat() donne une valeur entre 0.0 et 1.0
        this.globalYawAngle = random.nextFloat() * 360f; //Entre 0 et 360 degrés
        this.globalPitchAngle = (random.nextFloat() * 10f) - 5f; // Entre 5 et -5 degrés
        this.currentForce = currentForce;
        // Génération des courants
        generateRandomCurrents();
    }

    /**
     * Génère aléatoirement des courants
     */
    public void generateRandomCurrents() {
        // Le cap directeur va varier de 30 degré maximum
        float yawChange = (random.nextFloat() * maxYawAngleChange * 2f) - maxYawAngleChange;
        this.globalYawAngle = (this.globalYawAngle + yawChange) % 360f;
        if (this.globalYawAngle < 0) this.globalYawAngle += 360f;
        // L'inclinaison du courant va varier de 2 degrés et on le bloque à 5 max
        float pitchChange = (random.nextFloat() * maxPitchAngleChange * 2f) - maxPitchAngleChange;
        this.globalPitchAngle = FastMath.clamp(this.globalPitchAngle + pitchChange, -20f, 20);
        // Nouvelles graines
        this.seedYaw = random.nextFloat() * 1000f;
        this.seedPitch = random.nextFloat() * 1000f;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                // Calcul des cap
                float noiseYaw = getSmoothNoise(seedYaw + x * noiseScale, seedYaw + z * noiseScale);
                float localYawVar = noiseYaw * 30f;
                float finalYaw = (this.globalYawAngle + localYawVar) % 360f;
                // Calcul des inclinaisons
                float noisePitch = getSmoothNoise(seedPitch + x * noiseScale, seedPitch + z * noiseScale);
                float localPitchVar = noisePitch * 3f;
                float finalPitch = FastMath.clamp(this.globalPitchAngle + localPitchVar, -7f, 7f);

                float yawRad = finalYaw * FastMath.DEG_TO_RAD;
                float pitchRad = finalPitch * FastMath.DEG_TO_RAD;

                float forceX = FastMath.cos(yawRad) * currentForce;
                float forceZ = FastMath.sin(yawRad) * currentForce;
                float forceY = FastMath.sin(pitchRad) * currentForce;

                grid[x][z] = new Vector3f(forceX, forceY, forceZ);
            }
        }
    }

    /**
     * Un vrai algorithme de Bruit de Perlin 2D autonome.
     * Retourne une valeur fluide et texturée entre -1.0 et 1.0.
     */
    private float getSmoothNoise(float x, float z) {
        int X = (int) Math.floor(x) & 255;
        int Z = (int) Math.floor(z) & 255;

        float xf = x - (float) Math.floor(x);
        float zf = z - (float) Math.floor(z);

        // Fonctions de lissage (Fade curve)
        float u = xf * xf * xf * (xf * (xf * 6 - 15) + 10);
        float v = zf * zf * zf * (zf * (zf * 6 - 15) + 10);

        // Table de permutation pseudo-aléatoire pour les gradients
        int[] p = new int[512];
        java.util.Random r = new java.util.Random(12345); // Seed fixe pour la cohérence spatiale
        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 0; i < 256; i++) {
            int target = r.nextInt(256);
            int tmp = p[i]; p[i] = p[target]; p[target] = tmp;
            p[i + 256] = p[i];
        }

        // Hash des coordonnées des coins de la cellule
        int a = p[X] + Z;
        int b = p[X + 1] + Z;

        // Interpolation linéaire entre les gradients des 4 coins
        // Par ces trois lignes (formule standard du Lerp : v1 + t * (v2 - v1))
        float x1 = grad2d(p[a], xf, zf) + u * (grad2d(p[b], xf - 1, zf) - grad2d(p[a], xf, zf));
        float x2 = grad2d(p[a + 1], xf, zf - 1) + u * (grad2d(p[b + 1], xf - 1, zf - 1) - grad2d(p[a + 1], xf, zf - 1));

        return x1 + v * (x2 - x1);
    }

    /**
     * Fonction utilitaire de produit scalaire pour le bruit de Perlin
     */
    private float grad2d(int hash, float x, float z) {
        int h = hash & 7;
        float u = h < 4 ? x : z;
        float v = h < 4 ? z : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v * 2.0f : -v * 2.0f);
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

    /**
     * Change la valeur maximale de variation de cap du courant.
     * @param x Une valeur trop grande rendra le courant très imprévisible voir irréaliste.
     */
    public void setMaxYawAngleChange(float x){
        this.maxYawAngleChange = x;
    }

    /**
     * Renvoie la variation maximale du cap.
     * @return
     */
    public float getMaxYawAngleChange(){
        return this.maxYawAngleChange;
    }

    /**
     * Change la valeur maximale de variation d'inclinaison du courant.
     * @param x Une valeur trop grande rendra le courant très imprévisible voir irréaliste.
     */
    public void setMaxPitchAngleChange(float x){
        this.maxPitchAngleChange = x;
    }

    /**
     * Renvoie la variation maximale de l'angle d'inclinaison.
     */
    public float getMaxPitchAngleChange(){
        return this.maxPitchAngleChange;
    }

    /**
     * Définit la force du courant.
     * @param x Une valeur au dela de 0.5f va rendre le courant extrêmement puissant.
     *          Si une valeur négative est passée, elle sera convertie en valeur positive.
     */
    public void setCurrentForce(float x){
        // Pas de courant négatif
        if(x<0f){
            this.currentForce = -x;
        } else{
            this.currentForce = x;
        }
    }

    /**
     * Renvoie la force du courant.
     */
    public float getCurrentForce(){
        return this.currentForce;
    }
}
