package fr.app.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.util.Arrays;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;

import fr.app.structure.JsonSerializer;
import fr.app.structure.SerialisationException;

// Gère le transport UDP côté serveur.
// Rôle : recevoir les commandes de pilotage (PilotCommand) envoyées par le client,
// les désérialiser, construire un DroneStatus de réponse et le renvoyer au client.
// Implémente Runnable pour s'exécuter dans un thread dédié.
public class UdpServeurTransport implements Runnable{
    private final DatagramSocket socket;
    // Sérialisation Json : transforme un objet en bytes
    private final JsonSerializer serializer=new JsonSerializer();

    private final RegistreDesSessions registre;

    public UdpServeurTransport(int port, RegistreDesSessions registre)throws SocketException {
        // Crée un socket UDP en écoute sur le port spécifié
        this.socket = new DatagramSocket(port);
        this.registre = registre;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4096];

        while(!socket.isClosed()){
            try{
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                socket.receive(packet);

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

                // Désérialisation
                byte[] donneesBrutes = protocole.decrypt(data);
                System.out.println("[UDP Serveur] Commande déchiffrée : " + new String(donneesBrutes));
                CommandeMessage message = serializer.deserisation(donneesBrutes,CommandeMessage.class);
                PilotCommand command=message.getCommand();
                System.out.println("[UDP Serveur] Objet désérialisé    : " + command);

                DroneStatus droneStatus = new DroneStatus();
                droneStatus.setState("En vol");

                // Sérialisation
                byte[] responseBrute    = serializer.serialisation(droneStatus);
                byte[] response = protocole.encrypt(responseBrute);
                System.out.println("[UDP Serveur] Statut sérialisé     : " + new String(response));
                System.out.println("[UDP Serveur] Bytes statut envoyé  : " + java.util.Arrays.toString(response));
                DatagramPacket responsePacket = new DatagramPacket(response,response.length,packet.getAddress(),packet.getPort());
                socket.send(responsePacket);
                System.out.println("[UDP Serveur] Statut envoyé à " + packet.getAddress() + ":" + packet.getPort());
            } catch (SocketException e) {
                // Socket fermé proprement via close()
                System.out.println("[UDP Serveur] Arrêt du transport UDP.");
                break;
            } catch (Exception e) {
                // Erreur sur un paquet individuel : on continue
                System.out.println("[UDP Serveur] Erreur traitement paquet : " + e.getMessage());
            }
        }
    }
    // Ferme le socket UDP et arrête le thread
    public void close(){
        socket.close();
        System.out.println("[UDP Serveur] Socket fermé.");
    }
}
