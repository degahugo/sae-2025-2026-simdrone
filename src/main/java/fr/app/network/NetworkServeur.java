package fr.app.network;

public class NetworkServeur {

    private final TcpSessionServeur tcpSessionServeur;
    private final UdpServeurTransport udpServeurTransport;

    // portTcp : port les connexions entrantes
    // portUdp : pour les commandes/statuts
    // portUdpVideo : pour l'envoi des frames vidéo
    public NetworkServeur(int portTcp, int portUdp, int portUdpVideo) throws Exception {
        // le registre est créé ici et partagé entre TCP et UDP
        RegistreDesSessions registre = new RegistreDesSessions();

        this.tcpSessionServeur   = new TcpSessionServeur(portTcp, registre);
        this.udpServeurTransport = new UdpServeurTransport(portUdp, registre);
    }

    public void start() {
        new Thread(tcpSessionServeur,"tcp-serveur").start();
        new Thread(udpServeurTransport,"udp-serveur").start();
        System.out.println("[NetworkServeur] Serveur TCP + UDP démarré.");
    }
}