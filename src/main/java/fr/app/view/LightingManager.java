package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.util.SkyFactory;

public class LightingManager {
    private final Node rootNode;
    private final Camera cam;
    private final AssetManager assetManager;

    private AmbientLight ambientLight;
    private DirectionalLight sunLight;

    public LightingManager(Node rootNode, Camera cam, AssetManager assetManager) {
        this.rootNode = rootNode;
        this.cam = cam;
        this.assetManager = assetManager;
    }

    /**
     * Configure the lighting
     */
    public void initLighting() {
        // Set up scene lighting
        PointLight lamp_light = new PointLight();
        lamp_light.setColor(ColorRGBA.White);
        lamp_light.setRadius(4f);
        lamp_light.setPosition(new Vector3f(0.0f, 0.0f, 2.0f));
        rootNode.addLight(lamp_light);

        // Set up the spotlight
        SpotLight spot = new SpotLight();
        spot.setSpotRange(100f);                           // distance
        spot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
        spot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
        spot.setColor(ColorRGBA.White);                    // light color
        spot.setPosition(cam.getLocation());               // shine from camera loc
        spot.setDirection(cam.getDirection());             // shine forward from camera loc
        rootNode.addLight(spot);

        // Set up the ambient light
        ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White);
        rootNode.addLight(ambientLight);
        //on peut maintenant modifier la lumière depuis n'importe quelle méthode

        // Set up the directionnal light
        sunLight = new DirectionalLight();
        sunLight.setColor(ColorRGBA.White);
        sunLight.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sunLight);

        rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    }



    public void updateUnderwaterLighting(float depth) {
        float intensity = Math.max(0.05f, 1.0f - (depth * 0.02f));

        ambientLight.setColor(ColorRGBA.White.mult(intensity));
        sunLight.setColor(ColorRGBA.White.mult(intensity * 0.8f));
    }
}
