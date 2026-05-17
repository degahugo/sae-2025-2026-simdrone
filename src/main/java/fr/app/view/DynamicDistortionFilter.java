package fr.app.view;

import com.jme3.material.Material;
import fr.utln.jme3.filter.DistortionFilter;
import fr.utln.jme3.math.DistortionModel;

/**
 * Version dynamique du filtre de la fac permettant de modifier
 * les coefficients de distorsion en temps réel.
 */
public class DynamicDistortionFilter extends DistortionFilter {

    public DynamicDistortionFilter(DistortionModel distortionModel) {
        super(distortionModel);
    }

    // ON FORCE LE GETMATERIAL EN PUBLIC ICI
    @Override
    public Material getMaterial() {
        return super.getMaterial();
    }

    /**
     * Met à jour le coefficient K1 dans le modèle ET dans le shader
     */
    public void setK1(float k1) {
        getDistortionModel().setK1(k1);
        Material mat = getMaterial(); // Fonctionne car on est dans la classe enfant
        if (mat != null) {
            mat.setFloat("K1", k1);
        }
    }

    /**
     * Met à jour le coefficient K2 dans le modèle ET dans le shader
     */
    public void setK2(float k2) {
        getDistortionModel().setK2(k2);
        Material mat = getMaterial();
        if (mat != null) {
            mat.setFloat("K2", k2);
        }
    }

    /**
     * Met à jour le coefficient P1 dans le modèle ET dans le shader
     */
    public void setP1(float p1) {
        getDistortionModel().setP1(p1);
        Material mat = getMaterial();
        if (mat != null) {
            mat.setFloat("P1", p1);
        }
    }

    /**
     * Met à jour le coefficient P2 dans le modèle ET dans le shader
     */
    public void setP2(float p2) {
        getDistortionModel().setP2(p2);
        Material mat = getMaterial();
        if (mat != null) {
            mat.setFloat("P2", p2);
        }
    }
}