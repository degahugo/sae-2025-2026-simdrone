package fr.app.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Arrays;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;

import fr.app.structure.JsonSerializer;
import fr.app.model.MessageType;
import fr.app.structure.SerialisationException;
import fr.app.model.SimulationConfiguration;

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

    // Dernier statut reçu mis à jour par le thread de réception
    private final AtomicReference<DroneStatus> dernierStatut = new AtomicReference<>(null);
    private Thread receptionThread;
    private volatile boolean running = true;

    /**
     * Initialise la couche de transport UDP du client.
     *
     * Cette classe est responsable :
     * - de l'envoi des commandes de pilotage au serveur ;
     * - de la réception des statuts du drone ;
     * - du chiffrement et du déchiffrement des échanges UDP ;
     * - du maintien d'un thread de réception permanent.
     *
     * Dès la création du client, un thread de réception est lancé
     * afin d'écouter en continu les réponses du serveur.
     *
     * @param host adresse IP ou nom d'hôte du serveur.
     * @param port port UDP du serveur.
     * @param protocole protocole de chiffrement utilisé pour sécuriser les échanges.
     */
    public UdpClientTransport(String host, int port, ProtocoleDeChiffrement protocole) throws IOException{
        this.protocole = protocole;
        // Crée un socket UDP
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(500); // timeout court pour le thread de réception
        this.adresseServeur = InetAddress.getByName(host);
        this.port = port;
        System.out.println("[UDP Client] Socket créé sur le port local : " + socket.getLocalPort());
        demarrerReception();
    }

    /**
     * Démarre le thread de réception UDP.
     *
     * Ce thread fonctionne en arrière-plan pendant toute la durée
     * de vie du client et reçoit les statuts envoyés par le serveur.
     *
     * Chaque paquet reçu est :
     * - déchiffré ;
     * - désérialisé en DroneStatus ;
     * - stocké dans le cache dernierStatut.
     *
     * Les paquets qui ne peuvent pas être déchiffrés sont ignorés.
     * Cela permet de filtrer les paquets destinés à d'autres clients
     * présents sur la même machine ou le même réseau.
     */
    private void demarrerReception() {
        receptionThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (running && !socket.isClosed()) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
                    System.out.println("[UDP Client] Bytes reçus           : " + java.util.Arrays.toString(data));
                    try {
                        byte[] donneesBrutes = protocole.decrypt(data);
                        System.out.println("[UDP Client] Statut reçu (JSON)    : " + new String(donneesBrutes));
                        DroneStatus status = serializer.deserisation(donneesBrutes, DroneStatus.class);
                        System.out.println("[UDP Client] Statut désérialisé    : " + status.getState());
                        dernierStatut.set(status);
                    } catch (Exception e) {
                        // Paquet destiné à un autre client  ignoré silencieusement
                    }
                } catch (java.net.SocketTimeoutException e) {
                    // Normal  on reboucle
                } catch (Exception e) {
                    if (running) System.err.println("[UDP Client] Erreur réception : " + e.getMessage());
                }
            }
        }, "udp-status-receiver");
        receptionThread.setDaemon(true);
        receptionThread.start();
    }

    /**
     * Retourne le port UDP local utilisé par ce client.
     *
     * Ce port est transmis au serveur lors de la phase
     * d'initialisation afin que celui-ci sache où envoyer
     * les statuts et le flux vidéo.
     *
     * @return port UDP local du client.
     */
    public int getLocalPort(){
        return socket.getLocalPort();
    }

    /**
     * Envoie une commande de pilotage au serveur.
     *
     * La commande est encapsulée dans un CommandeMessage,
     * sérialisée au format JSON puis chiffrée avant d'être
     * transmise via UDP.
     *
     * Utilisé pour contrôler les mouvements du drone
     * (avance, recule, montée, rotation, etc.).
     *
     * @param droneId identifiant du drone ciblé.
     * @param command commande à exécuter.
     */
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

    /**
     * Retourne le dernier statut reçu du serveur.
     *
     * Cette méthode ne lit pas directement le socket UDP.
     * Elle consulte simplement le cache alimenté par le
     * thread de réception.
     *
     * Si aucun statut n'est encore disponible, la méthode
     * attend un temps limité avant de générer un timeout.
     *
     * @return dernier statut reçu pour le drone.
     */
    public DroneStatus receiveStatus() throws Exception {
        int tentatives = 0;
        while (dernierStatut.get() == null && tentatives < 30) {
            Thread.sleep(100);
            tentatives++;
        }
        DroneStatus status = dernierStatut.get();
        if (status == null) throw new java.net.SocketTimeoutException("Aucun statut reçu");
        return status;
    }

    /**
     * Attend la réception d'un nouveau statut du serveur.
     *
     * Contrairement à receiveStatus(), cette méthode vide
     * d'abord le cache afin d'être certaine de retourner
     * une information récemment reçue.
     *
     * Utile après le démarrage d'une simulation ou après
     * une opération nécessitant une réponse actualisée.
     *
     * @return premier statut reçu après la remise à zéro du cache.
     */
    public DroneStatus receiveStatusFrais() throws Exception {
        dernierStatut.set(null); // vide le cache
        int tentatives = 0;
        while (dernierStatut.get() == null && tentatives < 50) {
            Thread.sleep(100);
            tentatives++;
        }
        DroneStatus status = dernierStatut.get();
        if (status == null) throw new java.net.SocketTimeoutException("Aucun statut frais reçu");
        return status;
    }

    /**
     * Demande au serveur de démarrer une simulation pour le drone.
     *
     * Le serveur crée alors le drone dans la simulation,
     * initialise les capteurs et prépare les flux vidéo
     * et de télémétrie associés.
     *
     * @param droneId identifiant du drone à créer.
     */
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

    /**
     * Ferme proprement le client UDP.
     *
     * Arrête le thread de réception puis ferme le socket.
     * Après l'appel de cette méthode, aucun paquet ne peut
     * plus être envoyé ou reçu.
     */
    public void close(){
        running = false;
        socket.close();
        System.out.println("[UDP Client] Socket fermé.");

    }

    /**
     * Envoie la configuration de simulation au serveur.
     *
     * Cette configuration contient les paramètres nécessaires
     * à l'initialisation de la simulation :
     * - type de drone ;
     * - environnement ;
     * - paramètres de mission ;
     * - options de simulation.
     *
     * La configuration est sérialisée puis chiffrée avant envoi.
     *
     * @param configuration configuration de simulation à appliquer.
     */
    public void sendConfiguration(SimulationConfiguration configuration) throws Exception {

        CommandeMessage message = new CommandeMessage(
                MessageType.CONFIGURATION,
                null,
                null,
                configuration
        );

        byte[] data = serializer.serialisation(message);
        byte[] donneesChiffrees = protocole.encrypt(data);

        DatagramPacket packet = new DatagramPacket(
                donneesChiffrees,
                donneesChiffrees.length,
                adresseServeur,
                port
        );

        socket.send(packet);

        System.out.println("[UDP Client] Configuration envoyée : " + new String(data));
    }
}
