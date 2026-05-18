package fr.app.network;

import javax.crypto.SecretKey;
import java.util.concurrent.ConcurrentHashMap;

// Registre qui mémorise les sessions clients actives.
// Il fait le lien entre la connexion TCP (qui échange la clé AES)
// et la communication UDP (qui utilise cette clé pour chiffrer/déchiffrer).
//
// Chaque session est identifiée par l'adresse IP du client.
// La clé AES est stockée dans un objet ProtocoleDeChiffrement.
public class RegistreDesSessions {

    private final ConcurrentHashMap<String, ProtocoleDeChiffrement> sessions = new ConcurrentHashMap<>();

    //Enregistre une nouvelle session client.
    // Appelé par TcpSessionServeur après le handshake.

    public void enregistrer(String ipClient, int portUdp, SecretKey cleAes) {
        ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();
        protocole.setAesKey(cleAes);

        String cle = construireCle(ipClient, portUdp);
        sessions.put(cle, protocole);
        System.out.println("[RegistreDesSessions] Session enregistrée : " + ipClient );
    }

    // Supprime la session d'un client à sa déconnexion TCP
    public void supprimer(String ipClient) {
        sessions.remove(ipClient);
        System.out.println("[RegistreDesSessions] Session supprimée pour : " + ipClient);
    }

    //Retourne le protocole AES associé à un client UDP.

    public ProtocoleDeChiffrement getProtocole(String ipClient, int portUdp) {
        return sessions.get(ipClient);
    }

    private String construireCle(String ip, int port) {
        return ip;
    }
}