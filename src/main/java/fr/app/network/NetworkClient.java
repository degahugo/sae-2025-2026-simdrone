package fr.app.network;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import java.net.DatagramSocket;
import java.util.function.Consumer;

import fr.app.model.SimulationConfiguration;

// Classe gérant la connexion TCP et UDP de Client
// On se connecte en TCP
// On communique en UDP
/**
 * Point d'entrée réseau côté client.
 *
 * Cette classe encapsule l'ensemble des communications
 * avec le serveur :
 *
 * - connexion TCP et handshake RSA/AES ;
 * - envoi des commandes via UDP ;
 * - réception des statuts du drone ;
 * - réception du flux vidéo ;
 * - gestion de la connexion et de la déconnexion.
 *
 * Elle sert de façade afin que l'interface graphique
 * n'ait jamais à manipuler directement TCP, UDP ou
 * les mécanismes de chiffrement.
 */
public class NetworkClient {

    private final TcpSessionClient tcpSessionClient;
    private UdpClientTransport udpClientTransport;

    private final int portUdpServeur;

    private final int portUdpVideo;   // port local sur lequel le client écoute la vidéo

    private DatagramSocket socketVideo;
    private VideoFragmentReceiver videoReceiver;
    private Thread videoThread;

    private String droneId;

    /**
     * Construit un client réseau.
     *
     * Les connexions réseau ne sont pas encore ouvertes.
     * Seules les informations nécessaires à la future
     * connexion sont mémorisées.
     *
     * @param adresseServeur adresse IP ou nom d'hôte du serveur.
     * @param portTcp port TCP utilisé pour le handshake.
     * @param portUdpServeur port UDP utilisé pour les commandes.
     * @param portUdpVideo port local utilisé pour recevoir la vidéo.
     */
    public NetworkClient(String adresseServeur, int portTcp, int portUdpServeur, int portUdpVideo) {
        this.tcpSessionClient = new TcpSessionClient(adresseServeur, portTcp);
        this.portUdpServeur = portUdpServeur;
        this.portUdpVideo = portUdpVideo;
    }
    /**
     * Constructeur simplifié.
     *
     * Le port vidéo est automatiquement calculé à partir
     * du port UDP principal.
     *
     * @param adresseServeur adresse du serveur.
     * @param portTcp port TCP du handshake.
     * @param portUdpServeur port UDP des commandes.
     */
    public NetworkClient(String adresseServeur, int portTcp, int portUdpServeur) {
        this(adresseServeur, portTcp, portUdpServeur, portUdpServeur + 1);
    }

    // Étape 1 : connexion TCP + handshake RSA/AES
    // Étape 2 : création du socket UDP local
    // Étape 3 : envoi du port UDP local au serveur via TCP
    /**
     * Établit complètement la connexion avec le serveur.
     *
     * Étapes réalisées :
     * 1. Connexion TCP.
     * 2. Handshake RSA/AES.
     * 3. Création du transport UDP.
     * 4. Transmission des ports UDP au serveur.
     * 5. Démarrage optionnel de la réception vidéo.
     *
     * Une fois cette méthode terminée, le client est prêt
     * à envoyer des commandes et recevoir des statuts.
     *
     * @param onFrameVideo callback appelé à chaque image vidéo reçue.
     */
    public void connect(Consumer<byte[]> onFrameVideo) throws Exception {
        // Initialisation de la connexion TCP + handshake RSA/AES
        tcpSessionClient.connect();

        //On recupere la clé AES issu du handshake pour l'utiliser dans le transport UDP
        ProtocoleDeChiffrement protocoleUdp = new ProtocoleDeChiffrement();
        protocoleUdp.setAesKey(tcpSessionClient.getAesKey());
        // Initialisation de la connnexion UDP
        udpClientTransport = new UdpClientTransport(tcpSessionClient.getAdresseServeur(), portUdpServeur,protocoleUdp);

        // Retourne le port local pour la communication en UDP pour le serveur
        //tcpSessionClient.sendUdpPort(udpClientTransport.getLocalPort());

        tcpSessionClient.sendUdpPorts(udpClientTransport.getLocalPort(), portUdpVideo);

        // droneId sera utilisé automatiquement dans sendCommand / sendStartSimulation
        droneId = "drone1"; // valeur par défaut — le serveur assigne via le registre

        // 4. Démarrer la réception vidéo si un callback est fourni
        if (onFrameVideo != null) {
            demarrerReceptionVideo(onFrameVideo);
        }

        System.out.println("[NetworkClient] Client prêt.");
    }

    /**
     * Établit la connexion sans activer la réception vidéo.
     *
     * Utilisé lorsque seul le contrôle du drone ou la
     * télémétrie sont nécessaires.
     */
    public void connect() throws Exception {
        connect(null);
    }

    /**
     * Initialise la réception du flux vidéo.
     *
     * Cette méthode :
     * - ouvre le socket UDP vidéo ;
     * - crée le réassembleur de fragments vidéo ;
     * - démarre le thread de réception ;
     * - associe un callback aux images reconstruites.
     *
     * Si la réception vidéo est déjà active, seul le callback
     * est remplacé.
     *
     * @param onFrameVideo fonction appelée lorsqu'une image
     * complète est reconstruite.
     */
    public void demarrerReceptionVideo(Consumer<byte[]> onFrameVideo) throws Exception {
        System.out.println("[NetworkClient] demarrerReceptionVideo appelé, socketVideo null ? " + (socketVideo == null));
        if (socketVideo != null && !socketVideo.isClosed()) {
            System.out.println("[NetworkClient] Socket vidéo déjà ouvert sur " + socketVideo.getLocalPort());
            if (videoReceiver != null) videoReceiver.setCallback(onFrameVideo);
            return;
        }

        ProtocoleDeChiffrement protocoleVideo = new ProtocoleDeChiffrement();
        protocoleVideo.setAesKey(tcpSessionClient.getAesKey());

        socketVideo = new DatagramSocket(portUdpVideo);
        videoReceiver = new VideoFragmentReceiver(socketVideo, protocoleVideo, onFrameVideo);
        videoThread = new Thread(videoReceiver, "video-receiver");
        videoThread.setDaemon(true);
        videoThread.start();

        System.out.println("[NetworkClient] Réception vidéo sur port " + portUdpVideo);
    }

    /**
     * Envoie une commande de pilotage à un drone spécifique.
     *
     * La commande est transmise au serveur via UDP après
     * sérialisation et chiffrement.
     *
     * @param droneId identifiant du drone ciblé.
     * @param command commande à exécuter.
     */
    public void sendCommand(String droneId, PilotCommand command) throws Exception {
        // L'envoie des commandes de base se fait via UDP
        udpClientTransport.sendCommand(droneId, command);
    }

    /**
     * Envoie une commande au drone actuellement sélectionné.
     *
     * Utilise automatiquement le droneId mémorisé dans
     * l'instance NetworkClient.
     *
     * @param command commande de pilotage.
     */
    public void sendCommand(PilotCommand command) throws Exception {
        udpClientTransport.sendCommand(droneId != null ? droneId : "drone1", command);
    }

    /**
     * Demande au serveur de créer et démarrer un drone
     * dans la simulation.
     *
     * Cette requête déclenche :
     * - la création du drone ;
     * - l'initialisation des capteurs ;
     * - la préparation du flux vidéo ;
     * - la génération des statuts.
     *
     * @param droneId identifiant du drone à créer.
     */
    public void sendStartSimulation(String droneId) throws Exception {
        udpClientTransport.sendStartSimulation(droneId);
    }

    /**
     * Envoie la configuration de simulation au serveur.
     *
     * Cette configuration contient les paramètres qui
     * seront utilisés lors de la création de la simulation :
     * - environnement ;
     * - modèle de drone ;
     * - capteurs ;
     * - options diverses.
     *
     * @param configuration configuration souhaitée.
     */
    public void sendConfiguration(SimulationConfiguration configuration) throws Exception {
        udpClientTransport.sendConfiguration(configuration);
    }

    /**
     * Démarre la simulation pour le drone actuellement sélectionné.
     *
     * Utilise automatiquement le droneId mémorisé dans le client.
     */
    public void sendStartSimulation() throws Exception {
        udpClientTransport.sendStartSimulation(droneId != null ? droneId : "drone1");
    }

    /**
     * Retourne le dernier statut connu du drone.
     *
     * Le statut est fourni par le thread de réception UDP
     * et stocké dans un cache thread-safe.
     *
     * Cette méthode ne lit jamais directement le réseau.
     *
     * @return dernier statut reçu.
     */
    public DroneStatus receiveStatus() throws Exception {
        // Le status du drone se fait via UDP
        return udpClientTransport.receiveStatus();
    }

    /**
     * Attend explicitement un nouveau statut du serveur.
     *
     * Les anciens statuts mis en cache sont ignorés afin
     * de garantir que la valeur retournée correspond à une
     * mise à jour récente.
     *
     * @return statut fraîchement reçu.
     */
    public DroneStatus receiveStatusFrais() throws Exception {
        // permet de vider les anciens paquets, ignorer les anciens statuts, attendre un statut récent
        return udpClientTransport.receiveStatusFrais();
    }

    /**
     * Ferme proprement toutes les connexions réseau.
     *
     * Cette méthode :
     * - arrête la réception vidéo ;
     * - ferme le transport UDP ;
     * - ferme la session TCP ;
     * - libère les ressources réseau locales.
     *
     * Après l'appel de cette méthode, le client n'est plus
     * capable de communiquer avec le serveur.
     */
    public void disconnect() throws Exception {
        if (videoReceiver != null) videoReceiver.stop();
        if (udpClientTransport != null) {
            udpClientTransport.close();
        }

        udpClientTransport.close();
        tcpSessionClient.disconnect();
    }

    /**
     * Indique si la session TCP est toujours active.
     *
     * @return true si le client est connecté.
     */
    public boolean isConnected() {
        return tcpSessionClient.isConnected();
    }

    /**
     * Retourne l'identifiant du drone actuellement associé
     * à cette instance cliente.
     *
     * @return identifiant du drone sélectionné.
     */
    public String getDroneId() {
        return droneId;
    }

    /**
     * Définit le drone utilisé par défaut pour les commandes
     * et les démarrages de simulation.
     *
     * @param id identifiant du drone.
     */
    public void setDroneId(String id) {
        this.droneId = id;
    }
}