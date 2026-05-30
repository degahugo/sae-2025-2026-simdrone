package fr.app.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

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

    private volatile Main simulation;
    private final Object  simulationLock = new Object();
    private final ConcurrentHashMap<String, SimulationConfiguration> configurations = new ConcurrentHashMap<>();

    /**
     * Initialise le transport UDP côté serveur.
     *
     * Deux sockets sont créés :
     * - un socket de réception des commandes UDP des clients ;
     * - un socket dédié à l'envoi du flux vidéo.
     *
     * Le registre des sessions est utilisé pour retrouver les
     * clés de chiffrement et les informations réseau associées
     * à chaque client connecté.
     *
     * @param portUdp port d'écoute des commandes UDP.
     * @param registre registre central des sessions clientes.
     */
    public UdpServeurTransport(int portUdp, RegistreDesSessions registre)throws SocketException {
        // Crée un socket UDP en écoute sur le port spécifié
        this.socketCommandes = new DatagramSocket(portUdp);
        this.registre = registre;
        //Port libre : le serveur envoie la vidéo, il n'écoute pas sur un port fixe
        this.socketVideo = new DatagramSocket();

        System.out.println("[UDP Serveur] Commandes sur port " + portUdp
                + ", vidéo sortante sur port " + socketVideo.getLocalPort());
    }

    /**
     * Boucle principale du serveur UDP.
     *
     * Cette méthode :
     * - reçoit les paquets UDP des clients ;
     * - identifie la session associée ;
     * - déchiffre les messages ;
     * - désérialise les CommandeMessage ;
     * - traite les demandes de configuration ;
     * - traite les démarrages de simulation ;
     * - transmet les commandes de pilotage à JME3 ;
     * - renvoie le dernier statut connu du drone.
     *
     * Cette boucle reste active tant que le socket UDP
     * n'est pas fermé.
     */
    @Override
    public void run() {
        byte[] buffer = new byte[4096];

        while(!socketCommandes.isClosed()){
            try{
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                socketCommandes.receive(packet);

                byte[] data = Arrays.copyOf(packet.getData(),packet.getLength());
                String ipUdp = packet.getAddress().getHostAddress();
                int  portUdp = packet.getPort();

                System.out.println("[UDP Serveur] Paquet reçu de " + ipUdp + ":" + portUdp);
                System.out.println("[UDP Serveur] Bytes reçus          : " + java.util.Arrays.toString(data));

                // Résout l'ipClient TCP depuis l'IP:port UDP observé
                // (nécessaire quand IP UDP ≠ IP TCP, ex: WSL ou 2 clients sur même machine)
                // Résolution de la session par déchiffrement essai-par-essai.
                // Nécessaire en WSL : le port source UDP change après NAT,
                // impossible de résoudre par IP ou port de manière fiable.
                String ipClient = null;
                ProtocoleDeChiffrement protocole = null;
                byte[] donneesBrutes = null;

                for (java.util.Map.Entry<String, ProtocoleDeChiffrement> entry
                        : registre.getSessions().entrySet()) {
                    try {
                        donneesBrutes = entry.getValue().decrypt(data);
                        System.out.println("[UDP Serveur] Commande déchiffrée : " + new String(donneesBrutes));
                        ipClient  = entry.getKey();
                        protocole = entry.getValue();
                        // Mémoriser l'adresse UDP observée pour cette session
                        registre.setAdresseUdpObservee(ipClient, packet.getAddress());
                        break;
                    } catch (Exception ignored) {}
                }

                if (protocole == null) {
                    // Aucun protocole n'a pu déchiffrer — paquet invalide
                    continue;
                }
                System.out.println("[UDP Serveur] Commande déchiffrée : " + new String(donneesBrutes));

                // Désérialisation
                CommandeMessage message = serializer.deserisation(donneesBrutes,CommandeMessage.class);
                String droneId = message.getDroneId();
                PilotCommand command=message.getCommand();
                MessageType type = message.getType();

                if (type == MessageType.CONFIGURATION) {
                    configurations.put(ipClient, message.getConfiguration());

                    System.out.println("[UDP Serveur] Configuration reçue : " + ipClient
                            + message.getConfiguration().getEnvironmentType()
                            + " | "
                            +  message.getConfiguration().getDroneType()
                            + " | "
                            +  message.getConfiguration().getSensors());

                    continue;
                }
                if (type == MessageType.START_SIMULATION) {
                    registre.setDroneId(ipClient, droneId);
                    traiterStartSimulation(droneId, ipClient);
                    // Réponse immédiate sans bloquer sur le statut JME3
                    DroneStatus confirm = new DroneStatus();
                    confirm.setState("Simulation lancée");
                    envoyerStatut(protocole, confirm, packet.getAddress(), portUdp);
                    continue;
                }

                if (type == MessageType.COMMAND && simulation != null) {
                    // Envoyer la commande sans .get() pour ne pas bloquer
                    final String fDroneId = droneId;
                    final PilotCommand fCommand = command;
                    simulation.enqueue(() -> {
                        simulation.executeNetworkCommand(fDroneId, fCommand);
                        return null;
                    });
                }
                System.out.println("[UDP Serveur] Objet désérialisé    : " + command);

                // Récupérer le statut SANS .get() bloquant :
                // on lit le dernier statut connu directement depuis Main
                DroneStatus droneStatus = simulation != null
                        ? simulation.getDroneStatusDirect(droneId)
                        : statusSimulationNonLancee();

                envoyerStatut(protocole, droneStatus, packet.getAddress(), portUdp);

            } catch (SocketException e) {
                System.out.println("[UDP Serveur] Arrêt du transport UDP.");
                break;
            } catch (Exception e) {
                System.out.println("[UDP Serveur] Erreur traitement paquet : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Démarre une simulation ou ajoute un drone à une simulation existante.
     *
     * Si aucune simulation n'existe encore :
     * - création de l'instance Main ;
     * - application de la configuration reçue ;
     * - démarrage du moteur JME3.
     *
     * Ensuite :
     * - attente de l'initialisation complète ;
     * - création du drone demandé ;
     * - création de la caméra off-screen ;
     * - lancement du flux vidéo.
     *
     * Cette méthode ne bloque pas le thread UDP principal.
     *
     * @param droneId identifiant du drone à créer.
     * @param ipClient client ayant demandé la simulation.
     */
    private void traiterStartSimulation(String droneId, String ipClient) {

        synchronized (simulationLock) {
            if (simulation == null) {
                simulation = new Main();
                SimulationConfiguration config = configurations.get(ipClient);
                if (config != null) simulation.setConfiguration(config);

                com.jme3.system.AppSettings settings = new com.jme3.system.AppSettings(true);
                settings.setTitle("DronSim - Simulation 3D");
                settings.setWidth(1024);
                settings.setHeight(768);
                settings.setVSync(false);
                settings.setFrameRate(-1);
                simulation.setShowSettings(false);
                simulation.setSettings(settings);
                simulation.setPauseOnLostFocus(false);
                simulation.start();
                System.out.println("[UDP Serveur] Simulation créée pour " + ipClient);
            } else {
                System.out.println("[UDP Serveur] Simulation déjà active — ajout du drone " + droneId);
            }
        }

        final Main sim = simulation;
        new Thread(() -> {
            try {
                attendreInitialisation(sim);

                String modelName = "BR2R4";
                SimulationConfiguration config = configurations.get(ipClient);
                if (config != null && config.getDroneType() != null) {
                    modelName = config.getDroneType().getSimulatorName();
                }

                sim.ajouterDrone(droneId, modelName);
                attendreDrone(sim, droneId);

                sim.enqueue(() -> {
                    sim.creerViewPortOffScreen(droneId);
                    return null;
                }).get();

                System.out.println("[UDP Serveur] Drone " + droneId + " prêt, flux vidéo démarré.");
                demarrerThreadVideo(droneId, ipClient, sim);

            } catch (Exception e) {
                System.err.println("[UDP Serveur] Erreur init drone " + droneId + " : " + e.getMessage());
                e.printStackTrace();
            }
        }, "drone-init-" + droneId).start();
    }

    /**
     * Lance le thread responsable du streaming vidéo d'un drone.
     *
     * Le thread capture périodiquement les images produites
     * par DroneFrameCapture puis les envoie au client sous forme
     * de fragments UDP chiffrés.
     *
     * Chaque drone possède son propre thread vidéo afin de permettre
     * plusieurs flux simultanés.
     *
     * @param droneId identifiant du drone.
     * @param ipClient client destinataire du flux.
     * @param sim instance de simulation active.
     */
    private void demarrerThreadVideo(String droneId, String ipClient, Main sim) {
        int portVideo = registre.getPortUdpVideo(ipClient);
        ProtocoleDeChiffrement protocole = registre.getProtocole(ipClient, 0);
        VideoFragmentSender sender = new VideoFragmentSender(socketVideo, protocole);

        Thread t = new Thread(() -> {
            while (!socketCommandes.isClosed()) {
                try {
                    InetAddress adresse = registre.getAdresseVideoClient(ipClient);
                    if (adresse == null || portVideo < 0) {
                        Thread.sleep(100);
                        continue;
                    }
                    byte[] jpeg = sim.capturerFrameDrone(droneId);
                    if (jpeg != null) {
                        sender.envoyerFrame(jpeg, adresse, portVideo);
                    }
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("[VideoThread-" + droneId + "] " + e.getMessage());
                }
            }
        }, "video-sender-" + droneId);
        t.setDaemon(true);
        t.start();
    }

    /**
     * Envoie un DroneStatus au client.
     *
     * Le statut est :
     * - sérialisé au format JSON ;
     * - chiffré ;
     * - encapsulé dans un paquet UDP ;
     * - transmis au client demandeur.
     *
     * Cette méthode est utilisée après chaque commande afin
     * de fournir l'état le plus récent du drone.
     *
     * @param protocole protocole de chiffrement associé au client.
     * @param status statut à transmettre.
     * @param adresse adresse IP du destinataire.
     * @param port port UDP du destinataire.
     */
    private void envoyerStatut(ProtocoleDeChiffrement protocole, DroneStatus status, InetAddress adresse, int port) throws Exception {
        byte[] responseBrute = serializer.serialisation(status);
        byte[] response      = protocole.encrypt(responseBrute);
        System.out.println("[UDP Serveur] Statut envoyé à " + adresse + ":" + port
                + " — " + status.getState());
        DatagramPacket responsePacket = new DatagramPacket(response, response.length, adresse, port);
        socketCommandes.send(responsePacket);
    }

    /**
     * Construit un statut par défaut lorsque la simulation
     * n'est pas encore démarrée.
     *
     * Utilisé pour répondre aux clients qui envoient des
     * commandes avant l'initialisation du moteur de simulation.
     *
     * @return statut indiquant que la simulation est indisponible.
     */
    private DroneStatus statusSimulationNonLancee() {
        DroneStatus s = new DroneStatus();
        s.setState("Simulation non lancée");
        return s;
    }

    /**
     * Attend que l'instance JME3 ait terminé son initialisation.
     *
     * Cette attente est nécessaire avant toute création de drone,
     * car les objets 3D et les systèmes physiques ne peuvent être
     * manipulés qu'une fois le moteur totalement prêt.
     *
     * Un timeout est appliqué afin d'éviter une attente infinie.
     *
     * @param simulation instance de simulation à surveiller.
     */
    private void attendreInitialisation(Main simulation) throws InterruptedException {
        int tentatives = 0;
        while (!simulation.isInitialized() && tentatives < 300) {
            Thread.sleep(100);
            tentatives++;
        }
        if (!simulation.isInitialized()) {
            System.err.println("[UDP Serveur] Timeout : simulation non initialisée !");
        }
    }

    /**
     * Attend que le drone soit complètement instancié dans la simulation.
     *
     * La méthode vérifie périodiquement l'état du drone jusqu'à ce que :
     * - le drone soit opérationnel ;
     * - ou qu'un timeout soit atteint.
     *
     * Cette étape garantit que la caméra et le flux vidéo ne sont
     * créés qu'après la disponibilité complète du drone.
     *
     * @param sim instance de simulation.
     * @param droneId identifiant du drone attendu.
     */
    private void attendreDrone(Main sim, String droneId) throws InterruptedException {
        int t = 0;
        while (t < 150) {
            String etat = sim.getDroneStatusDirect(droneId).getState();
            if (!"En initialisation...".equals(etat) && !"Drone inconnu".equals(etat)) return;
            Thread.sleep(100);
            t++;
        }
        System.err.println("[UDP Serveur] Timeout : drone " + droneId + " non instancié.");
    }

    /**
     * Arrête le transport UDP côté serveur.
     *
     * Ferme le socket de réception des commandes.
     * La fermeture du socket provoque également l'arrêt
     * de la boucle principale exécutée dans run().
     */
    public void close(){
        socketCommandes.close();
        System.out.println("[UDP Serveur] Socket fermé.");
    }
}
