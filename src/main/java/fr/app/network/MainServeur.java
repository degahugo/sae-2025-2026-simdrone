package fr.app.network;

import java.io.IOException;

public class MainServeur {

    public static void main(String[] args) throws IOException {
        Serveur serveur = new Serveur(62500);
        serveur.start();
    }
}