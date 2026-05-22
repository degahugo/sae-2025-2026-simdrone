package fr.app.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Côté serveur : découpe une frame JPEG en fragments UDP et les envoie au client.
 *
 * Chaque instance est associée à un client (droneId).
 * Le frameId est un compteur atomique global pour identifier les frames.
 */
public class VideoFragmentSender {

    /** Compteur global de frameId partagé entre tous les senders */
    private static final AtomicLong FRAME_COUNTER = new AtomicLong(0);

    private final DatagramSocket socket;
    private final ProtocoleDeChiffrement protocole;

    public VideoFragmentSender(DatagramSocket socket, ProtocoleDeChiffrement protocole) {
        this.socket    = socket;
        this.protocole = protocole;
    }

    /**
     * Envoie une frame JPEG complète fragmentée en paquets UDP.
     *
     * @param jpegBytes  bytes de la frame JPEG
     * @param adresse    adresse IP du client
     * @param portUdp    port UDP du client
     */
    public void envoyerFrame(byte[] jpegBytes, InetAddress adresse, int portUdp) throws Exception {
        if (jpegBytes == null || jpegBytes.length == 0) return;

        long  frameId         = FRAME_COUNTER.incrementAndGet();
        int   totalFragments  = (int) Math.ceil((double) jpegBytes.length / VideoFragment.MAX_PAYLOAD_SIZE);

        for (int i = 0; i < totalFragments; i++) {
            int debut = i * VideoFragment.MAX_PAYLOAD_SIZE;
            int fin   = Math.min(debut + VideoFragment.MAX_PAYLOAD_SIZE, jpegBytes.length);

            byte[] payload = new byte[fin - debut];
            System.arraycopy(jpegBytes, debut, payload, 0, payload.length);

            VideoFragment fragment = new VideoFragment(
                frameId,
                (short) i,
                (short) totalFragments,
                payload
            );

            // Chiffrement AES du fragment
            byte[] fragmentBytes  = fragment.toBytes();
            byte[] fragmentChiffre = protocole.encrypt(fragmentBytes);

            DatagramPacket packet = new DatagramPacket(
                fragmentChiffre,
                fragmentChiffre.length,
                adresse,
                portUdp
            );
            if (i == 0) System.out.println("[VideoSender] Envoi fragment à " + adresse + ":" + portUdp);

            socket.send(packet);
        }

        System.out.println("[VideoSender] Frame " + frameId
            + " envoyée en " + totalFragments
            + " fragment(s), " + jpegBytes.length + " octets JPEG");
    }
}
