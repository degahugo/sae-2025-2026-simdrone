package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.water.WaterFilter;
import fr.app.model.WorldView;

public class WaterFilterManager {

    /**
     * Initialize the water filter
     * @param assetManager
     * @param viewPort
     * @param rootNode
     * @param model WorldView
     */
    public WaterFilterManager(AssetManager assetManager, ViewPort viewPort, Node rootNode, WorldView model){
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        WaterFilter waterFilter = new WaterFilter(rootNode, new Vector3f(-.5f,-.5f,-.5f));
        waterFilter.setWaterHeight(model.getWaterHeight());
        waterFilter.setSpeed(model.getWaterSpeed());
        fpp.addFilter(waterFilter);
        viewPort.addProcessor(fpp);
    }
}
