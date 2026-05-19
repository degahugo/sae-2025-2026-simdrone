package fr.app.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import java.util.Arrays;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;

import fr.app.structure.JsonSerializer;
import fr.app.model.MessageType;
import fr.app.structure.SerialisationException;

// Classe utilisant le protocole UDP du coté Client
// Sérialise et envoie les commandes de pilotage ainsi que les réponses de statut
// (PilotCommand) et (DroneStatus)
public class UdpClientTransport {
    private final DatagramSocket socket;
    private final InetAddress adresseServeur;
    private final int port;
    //Serialisation
    private final JsonSerializer serializer =new JsonSerializer();
    private final ProtocoleDeChiffrement protocole;


    public UdpClientTransport(String host, int port, ProtocoleDeChiffrement protocole) throws IOException{
        this.protocole = protocole;
        // Crée un socket UDP
        this.socket = new DatagramSocket();
        this.adresseServeur = InetAddress.getByName(host);
        this.port = port;
        System.out.println("[UDP Client] Socket créé sur le port local : " + socket.getLocalPort());

    }

    // Retourne le port local UDP pour l'envoyer au serveur via TCP
    public int getLocalPort(){
        return socket.getLocalPort();
    }

    //Envoie une commande de pilotage au serveur
    public void sendCommand(String droneId, PilotCommand command) throws Exception{
        CommandeMessage message = new CommandeMessage(MessageType.COMMAND,droneId,command);
        // on sérialise le message en le transformant en tableau de byte
        byte[] data =serializer.serialisation(message);
        // Tableau de bytes
        //System.out.println("[Client] Bytes sérialisés     : " + java.util.Arrays.toString(data));
        System.out.println("[UDP Client] Bytes sérialisés      : " + java.util.Arrays.toString(data));
        System.out.println("[UDP Client] Commande sérialisée   : " + command + " -> " + new String(data));

        //chiffrement AES avant envoi
        byte[] donneesChiffrees = protocole.encrypt(data);
        // Envoie le paquet UDP au serveur
        DatagramPacket packet = new DatagramPacket(donneesChiffrees,donneesChiffrees.length,adresseServeur,port);
        socket.send(packet);
        System.out.println("[UDP Client] Paquet envoyé à " + adresseServeur + ":" + port);

    }

    // Attend un paquet UDP du serveur, puis désérialise le DroneStatus reçu.
    public DroneStatus receiveStatus() throws Exception {
        byte[] buffer = new byte[4096];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.setSoTimeout(5000);
        socket.receive(packet);

        byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
        System.out.println("[UDP Client] Bytes reçus           : " + java.util.Arrays.toString(data));
        byte[] donneesBrutes = protocole.decrypt(data);
        System.out.println("[UDP Client] Statut reçu (JSON)    : " + new String(donneesBrutes));

        //Désérialisation : bytes JSON -> objet DroneStatus
        DroneStatus status = serializer.deserisation(donneesBrutes, DroneStatus.class);
        System.out.println("[UDP Client] Statut désérialisé    : " + status.getState());
        return status;
    }

    // Envoie une demande de lancement de simulation au serveur en UDP
    public void sendStartSimulation(String droneId) throws Exception {

        CommandeMessage msg = new CommandeMessage(
                MessageType.START_SIMULATION,
                droneId,
                null
        );

        byte[] data = serializer.serialisation(msg);
        byte[] donneesChiffrees = protocole.encrypt(data);

        DatagramPacket packet = new DatagramPacket(donneesChiffrees, donneesChiffrees.length, adresseServeur, port);
        socket.send(packet);
        System.out.println("[UDP Client] Start simulation envoyé : " + new String(data));
    }

    // Ferme le socket UDP
    public void close(){
        socket.close();
        System.out.println("[UDP Client] Socket fermé.");

    }
}
