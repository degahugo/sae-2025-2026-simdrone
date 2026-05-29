package fr.app.view;

import com.jme3.material.Material;
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
    private final DynamicDistortionFilter distortionFilter;
    FilterPostProcessor fpp;
    private float profondeur=0f;
    private float intensity=0f;

    private float lastProfondeur = Float.NaN;
    private float lastIntensity = Float.NaN;

    public WaterDistortionManager(FilterPostProcessor fpp, ViewPort viewPort, Drone drone, WorldView world) {
        this.fpp = fpp;
        this.drone = drone;
        this.world = world;

        // DISTORTION MODEL

        distortionModel = new DistortionModel(0f, 0f, 0f, 0f, 0f);
        //K1 à K3 distorsion radiale   P1,P2 distorsion temporelle
        //Tout à 0, image normale sans distorsion
        distortionFilter = new DynamicDistortionFilter(distortionModel);
        //On donne le modèle au filtre

        // =====================================
        // FILTER POST PROCESSOR
        fpp.addFilter(distortionFilter);
        // N'ajouter le fpp au viewPort que s'il n'y est pas déjà
        if (!viewPort.getProcessors().contains(fpp)) {
            viewPort.addProcessor(fpp);
        }
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
            //test immersion, si immergée on continue

        this.profondeur = waterHeight - droneHeight;
        this.intensity = Math.min(profondeur * 0.03f, 0.35f);
        // test immersion, si immergée on continue
            //System.out.println("profondeur : " + profondeur);
            //System.out.println("intensity : "+intensity);
            if (Math.abs(profondeur - lastProfondeur) > 0.01f || Math.abs(intensity - lastIntensity) > 0.001f) {
                lastProfondeur = profondeur;
                lastIntensity = intensity;
                System.out.println("profondeur : " + profondeur);
                System.out.println("intensity : " + intensity);
            }
            // profondeur *0;03f car l'image ne doit pas être trop grande pour qu'on puisse la voir.
            // profondeur -> intensité visuelle
            // Distortion radiale
            // k1 grosse distorsion globale, k2 raffinement, k3 minime
            // on utilise que k1 et k2 pour notre drone
            // K1 négatif car sous l'eau l'effet est bombé (vers l'extérieur) positif -> vers le centre
            distortionFilter.setK1(-intensity); // k1 est le plus gros facteur, donc c'est lui qui sera négatif
            distortionFilter.setK2(intensity * 0.4f);
            // DISTORSION TANGENTIELLE
            distortionFilter.setP1(intensity * 0.01f);
            distortionFilter.setP2(intensity * 0.01f);
        } else {
            this.profondeur = 0f;
            this.intensity = 0f;
            // Reset distortion when out of water
            distortionFilter.setK1(0f);
            distortionFilter.setK2(0f);
            distortionFilter.setP1(0f);
            distortionFilter.setP2(0f);
        }
    }
    public float getProfondeur() {
        return profondeur;
    }

    public float getIntensity() {
        return intensity;
    }
}