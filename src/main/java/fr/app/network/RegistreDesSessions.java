package fr.app.network;

import javax.crypto.SecretKey;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetAddress;

// Registre qui mémorise les sessions clients actives.
// Il fait le lien entre la connexion TCP (qui échange la clé AES)
// et la communication UDP (qui utilise cette clé pour chiffrer/déchiffrer).
//
// Chaque session est identifiée par l'adresse IP du client.
// La clé AES est stockée dans un objet ProtocoleDeChiffrement.
public class RegistreDesSessions {

    private final ConcurrentHashMap<String, ProtocoleDeChiffrement> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> portsVideo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InetAddress> adressesObservees = new ConcurrentHashMap<>();
    // IP déclarée par le client via TCP — utilisée pour envoyer la vidéo
    private final ConcurrentHashMap<String, InetAddress> adressesVideo = new ConcurrentHashMap<>();

    //Enregistre une nouvelle session client.
    // Appelé par TcpSessionServeur après le handshake.

    public void enregistrer(String ipClient, int portUdpCommandes, int portUdpVideo, SecretKey cleAes) {
        ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();
        protocole.setAesKey(cleAes);

        //String cle = construireCle(ipClient, portUdp);
        sessions.put(ipClient, protocole);
        portsVideo.put(ipClient, portUdpVideo);
        try {
            adressesVideo.put(ipClient, InetAddress.getByName(ipClient));
        } catch (Exception e) {
            System.err.println("[Registre] Impossible de résoudre l'IP vidéo : " + ipClient);
        }

        System.out.println("[RegistreDesSessions] Session enregistrée : " + ipClient + "  vidéo port " + portUdpVideo);
    }

    public void enregistrer(String ipClient, int portUdp, SecretKey cleAes) {
        enregistrer(ipClient, portUdp, portUdp + 1, cleAes);
    }

    // Supprime la session d'un client à sa déconnexion TCP
    public void supprimer(String ipClient) {
        sessions.remove(ipClient);
        portsVideo.remove(ipClient);
        adressesObservees.remove(ipClient);
        adressesVideo.remove(ipClient);
        System.out.println("[RegistreDesSessions] Session supprimée pour : " + ipClient);
    }

    //Retourne le protocole AES associé à un client UDP.

    public ProtocoleDeChiffrement getProtocole(String ipClient, int portUdp) {
        ProtocoleDeChiffrement p = sessions.get(ipClient);
        if (p != null) return p;
        if (sessions.size() == 1) return sessions.values().iterator().next();
        return null;
    }

    // Récupère le portUdp correspondant à la video
    public int getPortUdpVideo(String ipClient) {
        Integer port = portsVideo.get(ipClient);
        if (port != null) return port;
        if (portsVideo.size() == 1) return portsVideo.values().iterator().next();
        return -1;
    }


    public void setAdresseUdpObservee(String ipClient, InetAddress adresse) {
        adressesObservees.put(ipClient, adresse);
    }

    public InetAddress getAdresseUdpObservee(String ipClient) {
        InetAddress a = adressesObservees.get(ipClient);
        if (a != null) return a;
        if (adressesObservees.size() == 1) return adressesObservees.values().iterator().next();
        return null;
    }

    public InetAddress getAdresseVideoClient(String ipClient) {
        InetAddress a = adressesVideo.get(ipClient);
        if (a != null) return a;
        if (adressesVideo.size() == 1) return adressesVideo.values().iterator().next();
        return null;
    }
}