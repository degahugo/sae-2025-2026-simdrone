package fr.app.network;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;

// Classe gérant la connexion TCP et UDP de Client
// On se connecte en TCP
// On communique en UDP
public class NetworkClient {

    private final TcpSessionClient tcpSessionClient;
    private UdpClientTransport udpClientTransport;

    private final int portUdpServeur;

    public NetworkClient(String adresseServeur, int portTcp, int portUdpServeur) {
        this.tcpSessionClient = new TcpSessionClient(adresseServeur, portTcp);
        this.portUdpServeur = portUdpServeur;
    }

    // Étape 1 : connexion TCP + handshake RSA/AES
    // Étape 2 : création du socket UDP local
    // Étape 3 : envoi du port UDP local au serveur via TCP
    public void connect() throws Exception {
        // Initialisation de la connexion TCP
        tcpSessionClient.connect();

        //On recupere la clé AES issu du handshake pour l'utiliser dans le transport UDP
        ProtocoleDeChiffrement protocoleUdp = new ProtocoleDeChiffrement();
        protocoleUdp.setAesKey(tcpSessionClient.getAesKey());
        // Initialisation de la connnexion UDP
        udpClientTransport = new UdpClientTransport(tcpSessionClient.getAdresseServeur(), portUdpServeur,protocoleUdp);

        // Retourne le port local pour la communication en UDP pour le serveur
        tcpSessionClient.sendUdpPort(udpClientTransport.getLocalPort());

        System.out.println("[NetworkClient] Client prêt.");
    }

    public void sendCommand(String droneId, PilotCommand command) throws Exception {
        // L'envoie des commandes de base se fait via UDP
        udpClientTransport.sendCommand(droneId, command);
    }

    public void sendStartSimulation(String droneId) throws Exception {
        udpClientTransport.sendStartSimulation(droneId);
    }

    public DroneStatus receiveStatus() throws Exception {
        // Le status du drone se fait via UDP
        return udpClientTransport.receiveStatus();
    }

    public void disconnect() throws Exception {
        if (udpClientTransport != null) {
            udpClientTransport.close();
        }

        tcpSessionClient.disconnect();
    }

    public boolean isConnected() {
        return tcpSessionClient.isConnected();
    }
}