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
import com.jme3.scene.Spatial;
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
        Cylinder cone = new Cylinder(24, 24, 0.03f, 0.18f, 8f, true, false);

        leftBeam = new Geometry("LeftBeam", cone);

        //Matériau lumineux transparent
        Material leftMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        //Couleur bleu clair avec transparence
        leftMat.setColor("Color", new ColorRGBA(0.7f, 0.9f, 1f, 0.035f));
        leftMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        //activer les 2 faces pour la transparence
        leftMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        //ajout d'un glow plus réaliste
        leftMat.setBoolean("VertexColor", true);
        //texture plus douce
        leftMat.setTransparent(true);

        leftBeam.setMaterial(leftMat);

        leftBeam.setQueueBucket(RenderQueue.Bucket.Transparent);
        leftBeam.setCullHint(Spatial.CullHint.Never);
        leftBeam.rotate(0,FastMath.HALF_PI, 0);
        beamNode.attachChild(leftBeam);



        rightBeam = new Geometry("RightBeam", cone);
        Material rightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        rightMat.setColor("Color", new ColorRGBA(0.7f, 0.9f, 1f, 0.035f));
        rightMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        //Par défaut seule une face du mesh est visible
        rightMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        rightMat.setBoolean("VertexColor", true);
        rightMat.setTransparent(true);

        rightBeam.setMaterial(rightMat);
        rightBeam.setQueueBucket(RenderQueue.Bucket.Transparent);
        rightBeam.setCullHint(Spatial.CullHint.Never);
        rightBeam.rotate(0,FastMath.HALF_PI, 0);

        beamNode.attachChild(rightBeam);
    }

    public void update() {

        Vector3f dronePos = droneNode.getWorldTranslation();

     //rotation actuelle du drone
        Quaternion droneRot = droneNode.getWorldRotation();

        Vector3f leftOffset = droneRot.mult(new Vector3f(-0.35f, -0.08f, 3.2f));

        leftBeam.setLocalTranslation(dronePos.add(leftOffset));
        leftBeam.setLocalRotation(droneRot.mult(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0)));

        Vector3f rightOffset = droneRot.mult(new Vector3f(0.35f, -0.08f, 3.2f));

        rightBeam.setLocalTranslation(dronePos.add(rightOffset));
        rightBeam.setLocalRotation(droneRot.mult(new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0)));

    }
}