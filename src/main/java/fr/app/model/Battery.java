package fr.app.model;

public class Battery {
    private float niveau;
    private float maxNiveau;

    public Battery(float maxNiveau) {
        this.maxNiveau = maxNiveau;
        this.niveau = maxNiveau;
    }

    public void consommation(float quantiteconsomme) {
        niveau -= quantiteconsomme;
        if (niveau < 0f) {
            niveau = 0f;
        }
    }

    public void rechargement(float quantiterecharge) {
        niveau += quantiterecharge;
        if (niveau > maxNiveau) {
            niveau = maxNiveau;
        }
    }

    public boolean isEmpty() {
        return niveau <= 0f;
    }
    public float getNiveau() {
        return niveau;
    }
    public float getMaxNiveau() {
        return maxNiveau;
    }
    public float getPercentage() {
        return (niveau / maxNiveau) * 100f;
    }

}