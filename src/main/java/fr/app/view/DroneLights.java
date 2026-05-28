package fr.app.view;

import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class DroneLights {

    private final SpotLight spotlight;
    private final Node rootNode;
    private final Camera cam;

    private boolean enabled = true;

    public DroneLights(Node rootNode, Camera cam) {

        this.rootNode = rootNode;
        this.cam = cam;

        spotlight = new SpotLight();

        spotlight.setColor(ColorRGBA.White.mult(25f));

        spotlight.setSpotRange(120f);

        spotlight.setSpotInnerAngle(
                10f * FastMath.DEG_TO_RAD
        );

        spotlight.setSpotOuterAngle(
                25f * FastMath.DEG_TO_RAD
        );

        rootNode.addLight(spotlight);
    }

    public void update() {

        if (!enabled) {
            return;
        }

        spotlight.setPosition(
                cam.getLocation()
        );

        spotlight.setDirection(
                cam.getDirection()
        );
    }

    public void toggle() {

        enabled = !enabled;

        if (enabled) {
            rootNode.addLight(spotlight);
        } else {
            rootNode.removeLight(spotlight);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}