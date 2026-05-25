package fr.app.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.Arrays;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.model.MessageType;


import fr.app.structure.JsonSerializer;
import fr.app.Main;
import fr.app.model.SimulationConfiguration;


// Gère le transport UDP côté serveur.
// Rôle : recevoir les commandes de pilotage (PilotCommand) envoyées par le client,
// les désérialiser, construire un DroneStatus de réponse et le renvoyer au client.
// Implémente Runnable pour s'exécuter dans un thread dédié.
public class UdpServeurTransport implements Runnable{
    private final DatagramSocket socketCommandes;
    private final DatagramSocket socketVideo;
    // Sérialisation Json : transforme un objet en bytes
    private final JsonSerializer serializer=new JsonSerializer();

    private final RegistreDesSessions registre;

    private Main simulation;
    private SimulationConfiguration configuration;

    public UdpServeurTransport(int portUdp, RegistreDesSessions registre)throws SocketException {
        // Crée un socket UDP en écoute sur le port spécifié
        this.socketCommandes = new DatagramSocket(portUdp);
        this.registre = registre;
        //Port libre : le serveur envoie la vidéo, il n'écoute pas sur un port fixe
        this.socketVideo = new DatagramSocket();

        System.out.println("[UDP Serveur] Commandes sur port " + portUdp
                + ", vidéo sortante sur port " + socketVideo.getLocalPort());
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4096];

        while(!socketCommandes.isClosed()){
            try{
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                socketCommandes.receive(packet);

                byte[] data = Arrays.copyOf(packet.getData(),packet.getLength());
                String clientIp = packet.getAddress().getHostAddress();
                int  port = packet.getPort();

                System.out.println("[UDP Serveur] Paquet reçu de " + clientIp + ":" + port);
                System.out.println("[UDP Serveur] Bytes reçus          : " + java.util.Arrays.toString(data));

                ProtocoleDeChiffrement protocole = registre.getProtocole(clientIp, port);
                if (protocole == null) {
                    System.out.println("[UDP Serveur] Session inconnue pour "
                            + clientIp + ":" + port + " — paquet ignoré.");
                    continue;
                }
                // Mémoriser l'adresse UDP réelle observée sur le paquet
                registre.setAdresseUdpObservee(clientIp, packet.getAddress());

                // Désérialisation
                byte[] donneesBrutes = protocole.decrypt(data);
                System.out.println("[UDP Serveur] Commande déchiffrée : " + new String(donneesBrutes));
                CommandeMessage message = serializer.deserisation(donneesBrutes,CommandeMessage.class);
                String droneId = message.getDroneId();
                PilotCommand command=message.getCommand();
                MessageType type = message.getType();
                if (type == MessageType.CONFIGURATION) {
                    this.configuration = message.getConfiguration();

                    System.out.println("[UDP Serveur] Configuration reçue : "
                            + configuration.getEnvironmentType()
                            + " | "
                            + configuration.getDroneType()
                            + " | "
                            + configuration.getSensors());

                    continue;
                }
                if (type == MessageType.START_SIMULATION) {
                    traiterStartSimulation(droneId, clientIp);
                    // Réponse immédiate sans bloquer sur le statut JME3
                    DroneStatus confirm = new DroneStatus();
                    confirm.setState("Simulation lancée");
                    envoyerStatut(protocole, confirm, packet.getAddress(), packet.getPort());
                    continue;
                }

                if (type == MessageType.COMMAND && simulation != null) {
                    // Envoyer la commande sans .get() pour ne pas bloquer
                    simulation.enqueue(() -> {
                        simulation.executeNetworkCommand(droneId, command);
                        return null;
                    });
                }
                System.out.println("[UDP Serveur] Objet désérialisé    : " + command);

                // Récupérer le statut SANS .get() bloquant :
                // on lit le dernier statut connu directement depuis Main
                DroneStatus droneStatus = simulation != null
                        ? simulation.getDroneStatusDirect(droneId)
                        : statusSimulationNonLancee();

                envoyerStatut(protocole, droneStatus, packet.getAddress(), packet.getPort());

            } catch (SocketException e) {
                System.out.println("[UDP Serveur] Arrêt du transport UDP.");
                break;
            } catch (Exception e) {
                System.out.println("[UDP Serveur] Erreur traitement paquet : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void traiterStartSimulation(String droneId, String clientIp) {
        if (simulation == null) {
            simulation = new Main();
            com.jme3.system.AppSettings settings = new com.jme3.system.AppSettings(true);
            settings.setTitle("DronSim - Simulation 3D");
            settings.setWidth(1024);
            settings.setHeight(768);
            simulation.setShowSettings(false);
            simulation.setSettings(settings);
            simulation.setPauseOnLostFocus(false);
            simulation.start();
            System.out.println("[UDP Serveur] Simulation lancée pour " + droneId);
        } else {
            System.out.println("[UDP Serveur] Simulation déjà lancée.");
        }

        // Thread dédié : attend l'init JME3, crée le viewport, démarre la vidéo
        new Thread(() -> {
            try {
                attendreInitialisation();

                // Créer le viewport dans le thread JME3 avec .get() — c'est acceptable
                // ici car ce thread init n'est pas le thread de commandes UDP
                simulation.enqueue(() -> {
                    simulation.creerViewPortOffScreen(droneId);
                    return null;
                }).get();

                System.out.println("[UDP Serveur] Drone " + droneId + " prêt, démarrage flux vidéo.");

                int portVideo = registre.getPortUdpVideo(clientIp);
                ProtocoleDeChiffrement protocole = registre.getProtocole(clientIp, 0);
                VideoFragmentSender sender = new VideoFragmentSender(socketVideo, protocole);

                // Thread vidéo : utilise capturerFrameDrone qui est non bloquant (AtomicReference)
                Thread videoThread = new Thread(() -> {
                    while (!socketCommandes.isClosed()) {
                        try {
                            // Pour la vidéo : utiliser l'IP déclarée en TCP (172.17.x.x en WSL)
                            // et non l'IP observée sur les paquets UDP (10.x.x.x = IP hôte Windows)
                            InetAddress adresse = registre.getAdresseVideoClient(clientIp);
                            if (adresse == null || portVideo < 0) {
                                Thread.sleep(100);
                                continue;
                            }

                            // NON BLOQUANT : lit depuis AtomicReference dans DroneFrameCapture
                            byte[] jpeg = simulation.capturerFrameDrone(droneId);
                            if (jpeg != null) {
                                sender.envoyerFrame(jpeg, adresse, portVideo);
                            } else {
                                System.out.println("[VideoSender] Frame null pour " + droneId + " — pas encore rendue");
                            }
                            Thread.sleep(33); // ~30 fps
                        } catch (InterruptedException e) {
                            break;
                        } catch (Exception e) {
                            System.err.println("[VideoThread] Erreur : " + e.getMessage());
                        }
                    }
                }, "video-sender-" + droneId);
                videoThread.setDaemon(true);
                videoThread.start();

            } catch (Exception e) {
                System.err.println("[UDP Serveur] Erreur démarrage vidéo : " + e.getMessage());
                e.printStackTrace();
            }
        }, "video-init-" + droneId).start();
    }

    private void envoyerStatut(ProtocoleDeChiffrement protocole, DroneStatus status, InetAddress adresse, int port) throws Exception {
        byte[] responseBrute = serializer.serialisation(status);
        byte[] response      = protocole.encrypt(responseBrute);
        System.out.println("[UDP Serveur] Statut envoyé à " + adresse + ":" + port
                + " — " + status.getState());
        DatagramPacket responsePacket = new DatagramPacket(response, response.length, adresse, port);
        socketCommandes.send(responsePacket);
    }

    private DroneStatus statusSimulationNonLancee() {
        DroneStatus s = new DroneStatus();
        s.setState("Simulation non lancée");
        return s;
    }

    private void attendreInitialisation() throws InterruptedException {
        int tentatives = 0;
        while (!simulation.isInitialized() && tentatives < 300) {
            Thread.sleep(100);
            tentatives++;
        }
        if (!simulation.isInitialized()) {
            System.err.println("[UDP Serveur] Timeout : simulation non initialisée !");
        }
    }
    // Ferme le socket UDP et arrête le thread
    public void close(){
        socketCommandes.close();
        System.out.println("[UDP Serveur] Socket fermé.");
    }
}
