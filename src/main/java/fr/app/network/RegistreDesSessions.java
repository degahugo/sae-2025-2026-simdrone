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
/**
 * Registre central des sessions clientes.
 *
 * Cette classe maintient toutes les informations associées
 * à un client connecté :
 *
 * - protocole de chiffrement AES ;
 * - ports UDP utilisés pour les commandes et la vidéo ;
 * - adresse UDP réellement observée ;
 * - adresse utilisée pour l'envoi vidéo ;
 * - drone associé à la session.
 *
 * Elle sert de pont entre :
 * - la connexion TCP (handshake et échange de clé AES) ;
 * - les échanges UDP (commandes, statuts, vidéo).
 *
 * Toutes les structures sont thread-safe afin de permettre
 * un accès simultané depuis les threads TCP, UDP et vidéo.
 */
public class RegistreDesSessions {

    private final ConcurrentHashMap<String, ProtocoleDeChiffrement> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> portsVideo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> portsUdpDeclares= new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InetAddress> adressesObservees = new ConcurrentHashMap<>();
    // IP déclarée par le client via TCP — utilisée pour envoyer la vidéo
    private final ConcurrentHashMap<String, InetAddress> adressesVideo = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> droneIds= new ConcurrentHashMap<>();



    /**
     * Enregistre une nouvelle session client.
     *
     * Cette méthode est appelée après le handshake TCP lorsque
     * la clé AES a été échangée avec succès.
     *
     * Elle crée le protocole de chiffrement associé au client
     * puis mémorise :
     * - les ports UDP déclarés ;
     * - les ports vidéo ;
     * - l'adresse utilisée pour les flux vidéo.
     *
     * @param ipClient adresse IP déclarée par le client.
     * @param portUdpCommandes port UDP utilisé pour les commandes.
     * @param portUdpVideo port UDP utilisé pour la vidéo.
     * @param cleAes clé AES négociée durant le handshake.
     */
    public void enregistrer(String ipClient, int portUdpCommandes, int portUdpVideo, SecretKey cleAes) {
        ProtocoleDeChiffrement protocole = new ProtocoleDeChiffrement();
        protocole.setAesKey(cleAes);

        //String cle = construireCle(ipClient, portUdp);
        sessions.put(ipClient, protocole);
        portsVideo.put(ipClient, portUdpVideo);
        portsUdpDeclares.put(ipClient, portUdpCommandes);
        try {
            adressesVideo.put(ipClient, InetAddress.getByName(ipClient));
        } catch (Exception e) {
            System.err.println("[Registre] Impossible de résoudre l'IP vidéo : " + ipClient);
        }

        System.out.println("[RegistreDesSessions] Session enregistrée : " + ipClient + "  cmdPort=" + portUdpCommandes+ "  vidéo port " + portUdpVideo);
    }

    /**
     * Version simplifiée de l'enregistrement.
     *
     * Le port vidéo est automatiquement calculé à partir
     * du port de commande.
     *
     * @param ipClient adresse IP du client.
     * @param portUdp port UDP des commandes.
     * @param cleAes clé AES associée à la session.
     */
    public void enregistrer(String ipClient, int portUdp, SecretKey cleAes) {
        enregistrer(ipClient, portUdp, portUdp + 1, cleAes);
    }

    /**
     * Supprime complètement une session cliente.
     *
     * Appelée lors de la déconnexion TCP afin de libérer
     * toutes les ressources associées au client :
     *
     * - protocole AES ;
     * - ports UDP ;
     * - adresses observées ;
     * - drone associé.
     *
     * @param ipClient client à supprimer.
     */
    public void supprimer(String ipClient) {
        sessions.remove(ipClient);
        portsVideo.remove(ipClient);
        portsUdpDeclares.remove(ipClient);
        adressesObservees.remove(ipClient);
        adressesVideo.remove(ipClient);
        droneIds.remove(ipClient);
        System.out.println("[RegistreDesSessions] Session supprimée pour : " + ipClient);
    }

    /**
     * Tente d'associer un paquet UDP reçu à une session cliente.
     *
     * Cette méthode est nécessaire car l'adresse UDP observée
     * peut être différente de celle utilisée lors du handshake TCP
     * (cas WSL, NAT, Docker ou plusieurs clients sur une même machine).
     *
     * Ordre de résolution :
     * 1. correspondance directe par IP ;
     * 2. correspondance via le port UDP déclaré ;
     * 3. fallback si un seul client est connecté.
     *
     * @param ipUdp adresse IP observée sur le paquet UDP.
     * @param portUdp port source observé.
     * @return identifiant de session utilisé par le registre.
     */
    public String resoudreIpClient(String ipUdp, int portUdp) {
        // 1. Match direct par IP
        if (sessions.containsKey(ipUdp)) return ipUdp;

        // 2. Match par port UDP déclaré lors du handshake TCP
        for (java.util.Map.Entry<String, Integer> entry : portsUdpDeclares.entrySet()) {
            if (entry.getValue() == portUdp) {
                System.out.println("[Registre] Résolution port " + portUdp + " → " + entry.getKey());
                return entry.getKey();
            }
        }

        // 3. Fallback mono-client
        if (sessions.size() == 1) return sessions.keySet().iterator().next();

        System.out.println("[Registre] AVERTISSEMENT : session introuvable pour " + ipUdp + ":" + portUdp);
        return ipUdp;
    }

    /**
     * Retourne le protocole de chiffrement associé à un client.
     *
     * La résolution passe d'abord par la session TCP afin
     * d'identifier correctement le client malgré les éventuelles
     * modifications d'adresse ou de port provoquées par le NAT.
     *
     * Plusieurs stratégies de secours sont appliquées afin
     * d'améliorer la robustesse du système.
     *
     * @param ipUdp adresse observée sur le paquet UDP.
     * @param portUdp port source observé.
     * @return protocole de chiffrement correspondant.
     */
    public ProtocoleDeChiffrement getProtocole(String ipUdp, int portUdp) {
        // Résolution via ipClient
        String ipClient = resoudreIpClient(ipUdp, portUdp);
        ProtocoleDeChiffrement p = sessions.get(ipClient);
        if (p != null) return p;
        // Fallback direct
        p = sessions.get(ipUdp);
        if (p != null) return p;
        if (sessions.size() == 1) return sessions.values().iterator().next();
        return null;
    }

    /**
     * Retourne le port UDP utilisé pour la réception vidéo.
     *
     * Ce port est fourni par le client lors du handshake TCP
     * afin que le serveur puisse lui envoyer le flux vidéo.
     *
     * @param ipClient session cliente.
     * @return port vidéo ou -1 si introuvable.
     */
    public int getPortUdpVideo(String ipClient) {
        Integer port = portsVideo.get(ipClient);
        if (port != null) return port;
        if (portsVideo.size() == 1) return portsVideo.values().iterator().next();
        return -1;
    }

    /**
     * Mémorise l'adresse UDP réellement observée pour une session.
     *
     * Cette information est utile lorsque l'adresse visible
     * côté UDP diffère de l'adresse déclarée lors du handshake TCP.
     *
     * @param ipClient session concernée.
     * @param adresse adresse observée sur les paquets UDP.
     */
    public void setAdresseUdpObservee(String ipClient, InetAddress adresse) {
        adressesObservees.put(ipClient, adresse);
    }

    /**
     * Retourne la dernière adresse UDP observée pour un client.
     *
     * Utilisée pour suivre les éventuels changements provoqués
     * par le NAT ou les environnements virtualisés.
     *
     * @param ipClient session concernée.
     * @return adresse UDP observée.
     */
    public InetAddress getAdresseUdpObservee(String ipClient) {
        InetAddress a = adressesObservees.get(ipClient);
        if (a != null) return a;
        if (adressesObservees.size() == 1) return adressesObservees.values().iterator().next();
        return null;
    }

    /**
     * Retourne l'adresse IP utilisée pour l'envoi du flux vidéo.
     *
     * Cette adresse correspond généralement à celle déclarée
     * par le client lors de l'établissement de la session TCP.
     *
     * @param ipClient session concernée.
     * @return adresse de destination vidéo.
     */
    public InetAddress getAdresseVideoClient(String ipClient) {
        InetAddress a = adressesVideo.get(ipClient);
        if (a != null) return a;
        if (adressesVideo.size() == 1) return adressesVideo.values().iterator().next();
        return null;
    }

    /**
     * Associe un drone à une session cliente.
     *
     * Permet de retrouver rapidement quel drone appartient
     * à quel client connecté.
     *
     * @param ipClient session cliente.
     * @param droneId identifiant du drone.
     */
    public void setDroneId(String ipClient, String droneId) {
        droneIds.put(ipClient, droneId);
        System.out.println("[Registre] DroneId '" + droneId + "' → " + ipClient);
    }

    /**
     * Retourne l'identifiant du drone associé à une session.
     *
     * Un fallback est appliqué lorsqu'un seul drone est connu.
     *
     * @param ipClient session cliente.
     * @return identifiant du drone associé.
     */
    public String getDroneId(String ipClient) {
        String id = droneIds.get(ipClient);
        if (id != null) return id;
        if (droneIds.size() == 1) return droneIds.values().iterator().next();
        return null;
    }

    /**
     * Retourne l'ensemble des sessions enregistrées.
     *
     * Principalement utilisé par le serveur UDP pour tester
     * le déchiffrement d'un paquet avec chaque clé AES connue
     * lorsqu'il est impossible d'identifier directement le client.
     *
     * @return map des sessions actives.
     */
    public java.util.Map<String, ProtocoleDeChiffrement> getSessions() {
        return sessions;
    }
}