package fr.app.model;

public class EnvironmentConditionsManager {
    private CurrentMatrix currentMatrix;

    // NE PAS MODIFIER CES VALEURS : VALEURS PAR DÉFAUT
    private float maxYawAngleChange = 30f;
    private float maxPitchAngleChange = 10f;
    private float currentForce = 0.1f; // Ne pas dépasser 0.5f sinon ça devient beaucoup trop fort
    private int currentWidth = 100;
    private int currentLenght = 100;
    private float currentCellSize = 0.5f;
    private int environmentConditions = 1;
    //NE PAS MODIFIER CES VALEURS : VALEURS PAR DÉFAUT

    public EnvironmentConditionsManager(){

    }

    /**
     * Créer les conditions environnementales de la simulation.
     * @param terrainId L'ID du terrain utilisé
     * @param environmentConditions Les conditions voulues.
     *                              Pour le moment c'est juste une multiplication
     */
    public void InitiateEnvironmentConditions(int terrainId, int environmentConditions){
        // Définition des valeurs pour chaque terrain
        switch (terrainId){
            // Map 1 : target-approach-line-terrain.j3o
            case 1 : currentWidth = 100;
                currentLenght = 100;
                currentCellSize = 0.5f;
                currentForce = 0.1f;
                maxYawAngleChange = 30f;
                maxPitchAngleChange = 10f; break;
            // Map 2 : target-approach-spiral-terrain.j3o
            case 2 : currentWidth = 100;
                currentLenght = 100;
                currentCellSize = 0.5f;
                currentForce = 0.1f;
                maxYawAngleChange = 30f;
                maxPitchAngleChange = 10f; break;
            // Map 3 : point_borloo
            case 3 : currentWidth = 100;
                currentLenght = 100;
                currentCellSize = 0.5f;
                currentForce = 0.1f;
                maxYawAngleChange = 30f;
                maxPitchAngleChange = 10f; break;
            default : currentWidth = 100;
                currentLenght = 100;
                currentCellSize = 0.5f;
                currentForce = 0.1f;
                maxYawAngleChange = 30f;
                maxPitchAngleChange = 10f; break;
        }
        // Changement un peu nul pour tester
        currentForce = 0.1f * environmentConditions;
        maxYawAngleChange = 30f * environmentConditions/2f;
        maxPitchAngleChange = 10f * environmentConditions/2f;
        /*// Changement plus précis et personnalisé
        switch (environmentConditions){
            case 0 : currentForce = 0.1f;
                maxYawAngleChange = 30f;
                maxPitchAngleChange = 10f; break;
        }*/

        this.currentMatrix = new CurrentMatrix(currentWidth, currentLenght, currentCellSize, maxYawAngleChange, maxPitchAngleChange, currentForce);

        // Gestion de la turbidité de l'eau : pas encore implémenté
    }

    /**
     * InitiateEnvironmentConditions doit avoir créé la matrice de courants avant.
     * @return La matrice de courants
     */
    public CurrentMatrix getCurrentMatrix() {
        return currentMatrix;
    }
}
