package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import fr.app.model.CurrentMatrix;

public class CurrentDebugView {

    private final Node debugNode = new Node("CurrentDebugNode");
    private final CurrentMatrix currentMatrix;
    private final AssetManager assetManager;

    public CurrentDebugView(CurrentMatrix currentMatrix, AssetManager assetManager, Node rootNode) {
        this.currentMatrix = currentMatrix;
        this.assetManager = assetManager;
        // On attache notre node de debug au rootNode pour qu'il soit visible
        rootNode.attachChild(debugNode);
    }

    /**
     * Génère des flèches 3D pour visualiser le courant à la surface ou à une hauteur donnée.
     */
    public void showCurrentGrid(float arrowsHeight) {
        // On nettoie les anciennes flèches si on rappelle la méthode
        debugNode.detachAllChildren();
        int width = currentMatrix.getWidth();
        int length = currentMatrix.getLength();
        float startX = -currentMatrix.getHalfWidthInWorld();
        float startZ = -currentMatrix.getHalfLengthInWorld();
        float cellSize = currentMatrix.getCellSize();
        // Crée un matériau simple rouge pour les flèches
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Yellow);
        // On parcourt la grille pour afficher une flèche par case
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                // Calcul du centre de la case en coordonnées World (jME3)
                float worldX = startX + (x * cellSize) + (cellSize / 2f);
                float worldZ = startZ + (z * cellSize) + (cellSize / 2f);
                Vector3f position = new Vector3f(worldX, arrowsHeight, worldZ);
                // Récupération du courant à cet endroit
                Vector3f currentDirection = currentMatrix.getCurrentAt(position);
                // Si le courant est nul, on passe (inutile d'afficher une flèche invisible)
                if (currentDirection.lengthSquared() == 0) continue;
                // Création de la flèche 3D (Arrow prend la direction et la force en paramètre)
                Arrow arrowShape = new Arrow(currentDirection);
                Geometry arrowGeo = new Geometry("Arrow_" + x + "_" + z, arrowShape);
                arrowGeo.setMaterial(mat);
                // Positionnement de la flèche dans le monde
                arrowGeo.setLocalTranslation(position);
                // Attachement au node de debug
                debugNode.attachChild(arrowGeo);
            }
        }
    }

    /**
     * Permet de masquer/afficher facilement les flèches (pratique pour ton debugMode)
     */
    public void setVisible(boolean visible) {
        if (visible && debugNode.getParent() == null) {
            // Réattacher si nécessaire
        } else if (!visible) {
            debugNode.detachAllChildren();
        }
    }
}