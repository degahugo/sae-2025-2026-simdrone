package fr.app.network;

import java.net.InetAddress;

/**
 * Session client active — contient maintenant les deux ports UDP :
 *  - portUdpCommandes : port source des paquets de commande (éphémère)
 *  - portUdpVideo     : port fixe sur lequel le client écoute la vidéo (ex: 62502)
 */
public class SessionInfo {

    private final String ipClient;
    private final int portUdpCommandes;
    private final int portUdpVideo;
    private final InetAddress adresseClient;
    private final ProtocoleDeChiffrement protocole;
    private final String droneId;
    private VideoFragmentSender videoSender;

    public SessionInfo(String ipClient, int portUdpCommandes, int portUdpVideo, InetAddress adresseClient, ProtocoleDeChiffrement protocole, String droneId) {
        this.ipClient = ipClient;
        this.portUdpCommandes = portUdpCommandes;
        this.portUdpVideo = portUdpVideo;
        this.adresseClient = adresseClient;
        this.protocole = protocole;
        this.droneId = droneId;
    }

    public String getIpClient(){ return ipClient; }
    public int getPortUdpClient(){ return portUdpCommandes; }
    public int getPortUdpVideo(){ return portUdpVideo; }
    public InetAddress getAdresseClient(){ return adresseClient; }
    public ProtocoleDeChiffrement getProtocole(){ return protocole; }
    public String getDroneId(){ return droneId; }
    public VideoFragmentSender getVideoSender(){ return videoSender; }
    public void setVideoSender(VideoFragmentSender s){ this.videoSender = s; }

    @Override
    public String toString() {
        return "SessionInfo{ip=" + ipClient + ", cmdPort=" + portUdpCommandes
                + ", videoPort=" + portUdpVideo + ", drone=" + droneId + "}";
    }

    private InetAddress adresseUdpObservee;

    public InetAddress getAdresseUdpObservee() {
        return adresseUdpObservee != null ? adresseUdpObservee : adresseClient;
    }
    public void setAdresseUdpObservee(InetAddress a) {
        this.adresseUdpObservee = a;
    }
}