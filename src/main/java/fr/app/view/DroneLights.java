package fr.app.view;

import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class DroneLights {

    private final Node droneNode;
    private final Node rootNode;

    private SpotLight leftSpot;
    private SpotLight rightSpot;
    //2 spots avant

    private PointLight rearLight;
    //1 lumière arrière

    public DroneLights(Node droneNode, Node rootNode) {

        this.droneNode = droneNode;
        this.rootNode = rootNode;
        initLights();
    }

    private void initLights() {

        leftSpot = new SpotLight();
        leftSpot.setColor(ColorRGBA.Blue.mult(8f));
        //200f pour vérifier visuellement que le spotlight fonctionne bien
        leftSpot.setSpotRange(60f);
        //distance

        leftSpot.setSpotInnerAngle(12f * FastMath.DEG_TO_RAD);
        leftSpot.setSpotOuterAngle(25f * FastMath.DEG_TO_RAD);
        //grand cône pour être sûr de voir la lumière

        //Position relative au drone  x = gauche/droite   y = haut/bas   z = avant/arrière

        leftSpot.setPosition(droneNode.getWorldTranslation().add(-0.5f, -0.1f, 1.5f));
        //position volontairement éloignée du drone pour éviter que le spot soit caché dans le mesh

        leftSpot.setDirection(droneNode.getWorldRotation().mult(Vector3f.UNIT_Z.negate()));
        //beaucoup de modèles regardent vers -Z et pas +Z

        rootNode.addLight(leftSpot);
        //pas attachChild, les lumières ne sont pas des objets 3D

        System.out.println("LEFT SPOT ADDED");


        rightSpot = new SpotLight();
        rightSpot.setColor(ColorRGBA.Red.mult(8f));
        //couleur rouge pour distinguer facilement les deux spots

        rightSpot.setSpotRange(60f);
        rightSpot.setSpotInnerAngle(12f * FastMath.DEG_TO_RAD);
        rightSpot.setSpotOuterAngle(25f * FastMath.DEG_TO_RAD);

        rightSpot.setPosition(droneNode.getWorldTranslation().add(0.5f, -0.1f, 1.5f));
        rightSpot.setDirection(droneNode.getWorldRotation().mult(Vector3f.UNIT_Y.negate()));
        rootNode.addLight(rightSpot);
        System.out.println("RIGHT SPOT ADDED");

        rearLight = new PointLight();
        rearLight.setColor(ColorRGBA.Green.mult(200f));
        //vert très visible pour le test
        rearLight.setRadius(100f);
        //définit la portée de la lumière dans toutes les directions

        rearLight.setPosition(droneNode.getWorldTranslation().add(0f, 0f, -1f));
        rootNode.addLight(rearLight);
        System.out.println("REAR LIGHT ADDED");
    }


    public void update() {

        Vector3f dronePos = droneNode.getWorldTranslation();
        Vector3f forward = droneNode.getWorldRotation().mult(Vector3f.UNIT_Z.negate());
        //vecteur avant du drone

        leftSpot.setPosition(dronePos.add(droneNode.getWorldRotation().mult(new Vector3f(-0.5f, -0.1f, 1.5f))));
        //on garde le spot à gauche du drone même pendant les rotations

        rightSpot.setPosition(dronePos.add(droneNode.getWorldRotation().mult(new Vector3f(0.5f, -0.1f, 1.5f))));

        leftSpot.setDirection(forward);
        rightSpot.setDirection(forward);
        //les spots éclairent toujours devant le drone


        rearLight.setPosition(dronePos.add(droneNode.getWorldRotation().mult(new Vector3f(0f, 0f, -1f))));

        System.out.println(leftSpot.getPosition());
        //permet de vérifier que les spots bougent bien
    }

    public void updateUnderwaterIntensity(float depth) {

        float intensity =
                Math.max(0.2f, 1.0f - depth * 0.01f);
        //plus le drone descend, plus les lumières diminuent

        leftSpot.setColor(ColorRGBA.Blue.mult(200f * intensity));
        rightSpot.setColor(ColorRGBA.Red.mult(200f * intensity));
        rearLight.setColor(ColorRGBA.Green.mult(200f * intensity));
    }
}