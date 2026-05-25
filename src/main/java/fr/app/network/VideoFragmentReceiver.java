package fr.app.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Côté client : reçoit les fragments UDP vidéo, les réassemble et notifie
 * le DashboardController dès qu'une frame complète est disponible.
 *
 * Tourne dans son propre thread pour ne pas bloquer l'interface JavaFX.
 *
 * Stratégie de réassemblage :
 *  - On mémorise les fragments par frameId dans une Map
 *  - Dès que tous les fragments d'une frame sont reçus, on reconstitue le JPEG
 *  - Les frames incomplètes anciennes sont abandonnées (réseau UDP = perte possible)
 */
public class VideoFragmentReceiver implements Runnable {

    /** Port UDP dédié à la réception vidéo (portTcp + 2, ex: 62502) */
    private final DatagramSocket socket;
    private final ProtocoleDeChiffrement protocole;

    /** Callback appelé avec les bytes JPEG dès qu'une frame est complète */
    private Consumer<byte[]> onFrameComplete;

    /** Fragments en attente de réassemblage : frameId → tableau de fragments */
    private final Map<Long, byte[][]> fragmentsEnAttente = new HashMap<>();

    /** Dernière frameId traitée (pour ignorer les frames trop vieilles) */
    private long derniereFrameId = -1;

    private volatile boolean running = true;

    /**
     * @param socket          Socket UDP en écoute sur le port vidéo
     * @param protocole       Protocole AES pour le déchiffrement
     * @param onFrameComplete Callback JavaFX appelé avec les bytes JPEG d'une frame complète
     */
    public VideoFragmentReceiver(DatagramSocket socket,
                                 ProtocoleDeChiffrement protocole,
                                 Consumer<byte[]> onFrameComplete) {
        this.socket          = socket;
        this.protocole       = protocole;
        this.onFrameComplete = onFrameComplete;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[(VideoFragment.MAX_PACKET_SIZE + 64) * 2];

        System.out.println("[VideoReceiver] En écoute vidéo...");

        while (running && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                System.out.println("[VideoReceiver] Paquet reçu : " + packet.getLength() + " octets de " + packet.getAddress());


                byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                // Déchiffrement AES
                byte[] donneesBrutes = protocole.decrypt(data);

                // Désérialisation du fragment
                VideoFragment fragment = VideoFragment.fromBytes(donneesBrutes);

                traiterFragment(fragment);

            } catch (Exception e) {
                if (running) {
                    System.err.println("[VideoReceiver] Erreur réception : " + e.getMessage());
                }
            }
        }

        System.out.println("[VideoReceiver] Arrêt.");
    }

    /**
     * Stocke le fragment et reconstruit la frame si tous les fragments sont reçus.
     */
    private void traiterFragment(VideoFragment fragment) {
        long  frameId        = fragment.getFrameId();
        int   totalFragments = fragment.getTotalFragments();
        int   index          = fragment.getFragmentIndex();

        // Ignorer les frames trop vieilles (on privilégie la fluidité)
        if (frameId < derniereFrameId - 5) {
            System.out.println("[VideoReceiver] Fragment obsolète ignoré (frame " + frameId + ")");
            return;
        }

        // Initialiser le tableau de fragments si c'est une nouvelle frame
        fragmentsEnAttente.computeIfAbsent(frameId, id -> new byte[totalFragments][]);

        byte[][] fragments = fragmentsEnAttente.get(frameId);
        fragments[index] = fragment.getPayload();

        // Vérifier si tous les fragments sont reçus
        boolean frameComplete = true;
        int     tailleJpeg    = 0;
        for (byte[] frag : fragments) {
            if (frag == null) {
                frameComplete = false;
                break;
            }
            tailleJpeg += frag.length;
        }

        if (frameComplete) {
            // Reconstituer le JPEG
            byte[] jpeg = new byte[tailleJpeg];
            int pos = 0;
            for (byte[] frag : fragments) {
                System.arraycopy(frag, 0, jpeg, pos, frag.length);
                pos += frag.length;
            }

            fragmentsEnAttente.remove(frameId);
            derniereFrameId = frameId;

            // Nettoyer les frames incomplètes plus vieilles
            fragmentsEnAttente.entrySet().removeIf(e -> e.getKey() < frameId - 10);

            System.out.println("[VideoReceiver] Frame " + frameId
                + " complète (" + tailleJpeg + " octets JPEG)");

            // Notifier le controller JavaFX
            onFrameComplete.accept(jpeg);
        }
    }

    public void stop() {
        running = false;
        if (!socket.isClosed()) {
            socket.close();
        }
    }
    public void setCallback(Consumer<byte[]> onFrameComplete) {
        this.onFrameComplete = onFrameComplete;
    }
}
