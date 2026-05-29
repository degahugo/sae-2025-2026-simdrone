package fr.app.view;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.water.WaterFilter;
import fr.app.model.WorldView;

public class WaterFilterManager {

    FilterPostProcessor fpp;
    private WaterFilter waterFilter;
    /**
     * Initialize the water filter
     * @param fpp
     * @param viewPort
     * @param rootNode
     * @param model WorldView
     */

    public WaterFilterManager(FilterPostProcessor fpp, ViewPort viewPort, Node rootNode, WorldView model){
        this.fpp = fpp;
        this.waterFilter = new WaterFilter(rootNode, new Vector3f(-.5f,-.5f,-.5f));
        waterFilter.setWaterHeight(model.getWaterHeight());
        waterFilter.setSpeed(model.getWaterSpeed());
       // fpp.addFilter(waterFilter);

        // Crée un brouillard bleu/vert profond pour simuler l'opacité de l'eau
        FogFilter waterFog = new FogFilter();
        waterFog.setFogColor(new ColorRGBA(0.05f, 0.1f, 0.15f, 1.0f)); // Couleur de l'océan
        waterFog.setFogDistance(60f);                                // Visibilité max (60 unités)
        waterFog.setFogDensity(1.2f);                                // Densité de l'eau

// Ajoute ce filtre à ton processeur post-process (fpp)
        fpp.addFilter(waterFog);
        viewPort.addProcessor(fpp);
    }

    public void updateUnderwaterEffect(float depth) {
        float intensity = Math.max(0.01f, 1.0f - depth * 0.03f);
        waterFilter.setWaterColor(new ColorRGBA(0f, 0.1f * intensity, 0.2f * intensity, 1f));
        waterFilter.setDeepWaterColor(new ColorRGBA(0f, 0.03f * intensity, 0.08f * intensity, 1f));
    }
}
