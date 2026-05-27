package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

//beam effect = effet de faisceau
public class DroneBeamEffect {

    //Node principal du drone qui sert à récupérer sa position et sa rotation
    private final Node droneNode;
    // Node contenant les faisceaux lumineux
    private final Node beamNode;
    private Geometry leftBeam;
    private Geometry rightBeam;

    public DroneBeamEffect(Node droneNode, Node rootNode, AssetManager assetManager) {
        this.droneNode = droneNode;
        beamNode = new Node("BeamNode");
        createBeams(assetManager);
        rootNode.attachChild(beamNode);
    }

    private void createBeams(AssetManager assetManager) {

        //Forme des faisceaux
        Cylinder cone = new Cylinder(32, 32, 0.05f, 0.8f, 6f, true, false);

        leftBeam = new Geometry("LeftBeam", cone);

        /*
         * Matériau lumineux transparent
         */
        Material leftMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        /*
         * Couleur bleu clair avec transparence
         *
         * alpha = 0.18
         * -> rend le faisceau semi-transparent
         */
        leftMat.setColor(
                "Color",
                new ColorRGBA(0.7f, 0.9f, 1f, 0.18f)
        );

        /*
         * Active la transparence
         */
        leftMat.getAdditionalRenderState().setBlendMode(
                RenderState.BlendMode.Alpha
        );

        leftBeam.setMaterial(leftMat);

        /*
         * Le beam doit être rendu
         * dans la couche transparente
         */
        leftBeam.setQueueBucket(
                RenderQueue.Bucket.Transparent
        );

        /*
         * Rotation du cône
         *
         * Par défaut :
         * le cône pointe vers Y
         *
         * On le tourne vers l'avant
         */
        leftBeam.rotate(
                FastMath.HALF_PI,
                0,
                0
        );

        beamNode.attachChild(leftBeam);

        /*
         * =========================
         * FAISCEAU DROIT
         * =========================
         */

        rightBeam = new Geometry("RightBeam", cone);

        Material rightMat = new Material(
                assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md"
        );

        rightMat.setColor(
                "Color",
                new ColorRGBA(0.7f, 0.9f, 1f, 0.18f)
        );

        rightMat.getAdditionalRenderState().setBlendMode(
                RenderState.BlendMode.Alpha
        );

        rightBeam.setMaterial(rightMat);

        rightBeam.setQueueBucket(
                RenderQueue.Bucket.Transparent
        );

        rightBeam.rotate(
                FastMath.HALF_PI,
                0,
                0
        );

        beamNode.attachChild(rightBeam);
    }

    public void update() {

        /*
         * =========================
         * POSITION ACTUELLE DU DRONE
         * =========================
         */

        Vector3f dronePos =
                droneNode.getWorldTranslation();

        /*
         * Rotation actuelle du drone
         */
        Quaternion droneRot =
                droneNode.getWorldRotation();

        /*
         * =========================
         * FAISCEAU GAUCHE
         * =========================
         */

        /*
         * Position locale du phare gauche
         *
         * x = gauche/droite
         * y = haut/bas
         * z = avant/arrière
         */
        Vector3f leftOffset =
                droneRot.mult(
                        new Vector3f(-0.5f, -0.1f, 1.5f)
                );

        /*
         * Position du faisceau dans le monde
         */
        leftBeam.setLocalTranslation(
                dronePos.add(leftOffset)
        );

        /*
         * Le faisceau tourne avec le drone
         */
        leftBeam.setLocalRotation(droneRot);

        /*
         * =========================
         * FAISCEAU DROIT
         * =========================
         */

        Vector3f rightOffset =
                droneRot.mult(
                        new Vector3f(0.5f, -0.1f, 1.5f)
                );

        rightBeam.setLocalTranslation(
                dronePos.add(rightOffset)
        );

        rightBeam.setLocalRotation(droneRot);
    }
}