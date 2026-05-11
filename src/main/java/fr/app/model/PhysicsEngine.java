package fr.app.model;

import com.jme3.app.Application;
import fr.app.Main;

public class PhysicsEngine {

    private WorldView model;
    private Main view;

    public PhysicsEngine(WorldView model) {
        this.model = model;
    }

    protected void initialize(Application app) {
        this.view = (Main) app;
        // Ici, on pourrait lier des entrées clavier pour modifier le modèle
    }


    public void update(float tpf) {
        // Logique de mise à jour : synchroniser la vue sur le modèle
        // Si le modèle change, on met à jour le filtre d'eau de la vue
        if (view.getWaterFilter() != null) {
            view.getWaterFilter().setWaterHeight(model.getWaterHeight());
        }
    }

    protected void onEnable() {}
    protected void onDisable() {}
    protected void cleanup(Application app) {}
}