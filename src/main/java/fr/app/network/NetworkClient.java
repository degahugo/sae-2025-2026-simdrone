package fr.app.network;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import java.net.DatagramSocket;
import java.util.function.Consumer;


// Classe gérant la connexion TCP et UDP de Client
// On se connecte en TCP
// On communique en UDP
public class NetworkClient {

    private final TcpSessionClient tcpSessionClient;
    private UdpClientTransport udpClientTransport;

    private final int portUdpServeur;

    private final int portUdpVideo;   // port local sur lequel le client écoute la vidéo

    private DatagramSocket socketVideo;
    private VideoFragmentReceiver videoReceiver;
    private Thread videoThread;

    private String droneId;

    public NetworkClient(String adresseServeur, int portTcp, int portUdpServeur, int portUdpVideo) {
        this.tcpSessionClient = new TcpSessionClient(adresseServeur, portTcp);
        this.portUdpServeur = portUdpServeur;
        this.portUdpVideo = portUdpVideo;
    }
    // Constructeur sans port vidéo
    public NetworkClient(String adresseServeur, int portTcp, int portUdpServeur) {
        this(adresseServeur, portTcp, portUdpServeur, portUdpServeur + 1);
    }

    // Étape 1 : connexion TCP + handshake RSA/AES
    // Étape 2 : création du socket UDP local
    // Étape 3 : envoi du port UDP local au serveur via TCP
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

    public void connect() throws Exception {
        connect(null);
    }

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

    public void sendCommand(String droneId, PilotCommand command) throws Exception {
        // L'envoie des commandes de base se fait via UDP
        udpClientTransport.sendCommand(droneId, command);
    }

    // Envoie la commande avec le droneId mémorisé
    public void sendCommand(PilotCommand command) throws Exception {
        udpClientTransport.sendCommand(droneId != null ? droneId : "drone1", command);
    }

    public void sendStartSimulation(String droneId) throws Exception {
        udpClientTransport.sendStartSimulation(droneId);
    }

    public void sendStartSimulation() throws Exception {
        udpClientTransport.sendStartSimulation(droneId != null ? droneId : "drone1");
    }

    public DroneStatus receiveStatus() throws Exception {
        // Le status du drone se fait via UDP
        return udpClientTransport.receiveStatus();
    }

    public void disconnect() throws Exception {
        if (videoReceiver != null) videoReceiver.stop();
        if (udpClientTransport != null) {
            udpClientTransport.close();
        }

        udpClientTransport.close();
        tcpSessionClient.disconnect();
    }

    public boolean isConnected() {
        return tcpSessionClient.isConnected();
    }

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String id) {
        this.droneId = id;
    }
}