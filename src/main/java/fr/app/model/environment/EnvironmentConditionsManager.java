package fr.app.model.environment;

import fr.app.model.CurrentMatrix;

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
     * À modifier pour utiliser l'enum EnvironmentConditions plus tard.
     * @param terrainId L'ID du terrain utilisé
     * @param environmentConditions Les conditions voulues.
     * @param environmentVisibility Visibilité voulues.
     */
    public void InitiateEnvironmentConditions(int terrainId, EnvironmentConditions environmentConditions, EnvironmentConditions environmentVisibility){
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
        System.out.print(" Initialisation des courrants : ");
        // Changement des courants en fonction de la valeur choisi
        switch (environmentConditions){
            case NO_CURRENT:
                System.out.println("pas de courant");
                currentForce = currentForce * 0f;
                maxYawAngleChange = maxYawAngleChange * 0f;
                maxPitchAngleChange = maxPitchAngleChange * 0f; break;
            case LIGHT_CURRENT:
                System.out.println("peu de courant");
                currentForce = currentForce * 0.5f;
                maxYawAngleChange = maxYawAngleChange * 0.5f;
                maxPitchAngleChange = maxPitchAngleChange * 0.5f; break;
            case STRONG_CURRENT:
                System.out.println("courant fort");
                currentForce = currentForce * 3f;
                maxYawAngleChange = maxYawAngleChange * 2f;
                maxPitchAngleChange = maxPitchAngleChange * 2f; break;
            case VERY_STRONG_CURRENT:
                System.out.println("courant très fort");
                currentForce = currentForce * 10f;
                maxYawAngleChange = maxYawAngleChange * 3f;
                maxPitchAngleChange = maxPitchAngleChange * 3f; break;
            case NORMAL_CURRENT:
            default:
                System.out.println("courant normal/par défaut");
                currentForce = currentForce * 1f;
                maxYawAngleChange = maxYawAngleChange * 1f;
                maxPitchAngleChange = maxPitchAngleChange * 1f; break;
        }
        //Visibilité : pas encore implémenté donc pas utilisé, on va utiliser la turbidité de l'eau
        System.out.println("Initialisation de la visibilité : ");
        switch (environmentVisibility){
            case VERY_GOOD_VISIBILITY:
                System.out.println("excellente");
                break;
            case GOOD_VISIBILITY:
                System.out.println("bonne");
                break;
            case BAD_VISIBILITY:
                System.out.println("mauvaise");
                break;
            case VERY_BAD_VISIBILITY:
                System.out.println("très mauvaise");
                break;
            case NO_VISIBILITY:
                System.out.println("extrêment mauvaise");
                break;
            case CORRECT_VISIBILITY:
            default:
                System.out.println("correcte/par défaut");
        }

        this.currentMatrix = new CurrentMatrix(currentWidth, currentLenght, currentCellSize, maxYawAngleChange, maxPitchAngleChange, currentForce);
    }

    /**
     * InitiateEnvironmentConditions doit avoir créé la matrice de courants avant.
     * @return La matrice de courants
     */
    public CurrentMatrix getCurrentMatrix() {
        return currentMatrix;
    }
}
