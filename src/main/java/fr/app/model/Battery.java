package fr.app.model;

public class Battery {
    private float niveau;
    private float maxNiveau;
    /*maxNiveau c'est le 100% de batterie que l'on connaît
    le minNiveau on l'a définit dans la méthode consommation quand on dit que la batterie n'est pas négative */

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
    /* niveau c'est le niveau de la batterie qu'on a avant chaque consommation
    ce niveau change entre 2 états
    soit on rajoute de la batterie au niveau avec rechargement soit on en enlève avec consommation */

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
    public float getMaxNiveau() {return maxNiveau;}
    public float getPercentage() {
        return (niveau / maxNiveau) * 100f;
    }

}