package fr.app.network;

public class NetworkServeur {

    private final TcpSessionServeur tcpSessionServeur;
    private final UdpServeurTransport udpServeurTransport;

    public NetworkServeur(int portTcp, int portUdp) throws Exception {
        // le registre est créé ici et partagé entre TCP et UDP
        RegistreDesSessions registre = new RegistreDesSessions();

        this.tcpSessionServeur   = new TcpSessionServeur(portTcp, registre);
        this.udpServeurTransport = new UdpServeurTransport(portUdp, registre);
    }

    public void start() {
        new Thread(tcpSessionServeur).start();
        new Thread(udpServeurTransport).start();
        System.out.println("[NetworkServeur] Serveur TCP + UDP démarré.");
    }
}