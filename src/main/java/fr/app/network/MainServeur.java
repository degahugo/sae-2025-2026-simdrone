package fr.app.network;

import java.io.IOException;

public class MainServeur {

    public static void main(String[] args) throws IOException {
        new Serveur(62500).start(); // 49152 - 65535   plage "privée/dynamique"
    }
}