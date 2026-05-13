package fr.app.view;

import com.jme3.asset.AssetManager;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.ViewPort;

import fr.app.model.Drone;
import fr.app.model.WorldView;

import fr.utln.jme3.filter.DistortionFilter;
import fr.utln.jme3.math.DistortionModel;

public class WaterDistortionManager {

    private final Drone drone;
    private final WorldView world;
    private final DistortionModel distortionModel;
    private final DistortionFilter distortionFilter;

    public WaterDistortionManager(AssetManager assetManager, ViewPort viewPort, Drone drone, WorldView world) {

        this.drone = drone;
        this.world = world;

        // =====================================
        // DISTORTION MODEL
        // =====================================

        distortionModel = new DistortionModel(0f, 0f, 0f, 0f, 0f);
        //K1 à K3 distorsion radiale   P1,P2 distorsion temporelle
        //Tout à 0, image normale sans distorsion

        distortionFilter = new DistortionFilter(distortionModel);
        //On donne le modèle au filtre

        // =====================================
        // FILTER POST PROCESSOR
        // =====================================

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);

        fpp.addFilter(distortionFilter);

        viewPort.addProcessor(fpp);
    }

    /**
     * Mise à jour distorsion eau
     */
    public void update() {

        float droneHeight = drone.getDroneSpatial().getWorldTranslation().y;

        float waterHeight = world.getWaterHeight();

        // =====================================
        // DRONE SOUS L'EAU
        // =====================================

        if (droneHeight < waterHeight) {
        //test immersion, si immergée on continue
            float profondeur = waterHeight - droneHeight;


            float intensity = Math.min(profondeur * 0.03f, 0.35f);
            // profondeur *0;03f car l'image ne doit pas être trop grande pour qu'on puisse la voir.
            //profondeur -> intensité visuelle

            // =====================================
            // DISTORSION RADIALE
            // =====================================
            //k1 grosse distorsion globale
            //k2 raffinement
            //k3 minime
            //d'où le fait qu'on s'intersse que pour notre drône à k1 et k2.

            distortionModel.setK1(-intensity);
            //K1 négatif car sous l'eau l'effet est bombé 'va vers l'extérieur'
            //alors que K1 positif entraîne une image aspirée vers le centre
            // pour la distortion c'est le signe de K1 qui nous intéresse car c'est le plus gros facteur k

            distortionModel.setK2(intensity * 0.4f);

            // =====================================
            // DISTORSION TANGENTIELLE
            // =====================================

            distortionModel.setP1(intensity * 0.01f);

            distortionModel.setP2(intensity * 0.01f);

        } else {

            // =====================================
            // RESET DISTORTION
            // =====================================

            distortionModel.setK1(0f);
            distortionModel.setK2(0f);
            distortionModel.setP1(0f);
            distortionModel.setP2(0f);}}}

