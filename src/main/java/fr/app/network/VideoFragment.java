package fr.app.network;

import java.nio.ByteBuffer;

/**
 * Fragment UDP d'une frame vidéo.
 *
 * Header (12 octets) :
 *  frameId        : long  (8 octets)
 *  fragmentIndex  : short (2 octets)
 *  totalFragments : short (2 octets)
 *
 * Payload : tout ce qui suit le header.
 * On ne stocke plus la taille du payload dans le header —
 * elle est déduite de la taille totale du paquet reçu.
 */
public class VideoFragment {

    public static final int MAX_PAYLOAD_SIZE = 1024;
    public static final int HEADER_SIZE      = 12; // long(8) + short(2) + short(2)
    public static final int MAX_PACKET_SIZE  = HEADER_SIZE + MAX_PAYLOAD_SIZE;

    private final long   frameId;
    private final short  fragmentIndex;
    private final short  totalFragments;
    private final byte[] payload;

    public VideoFragment(long frameId, short fragmentIndex, short totalFragments, byte[] payload) {
        this.frameId        = frameId;
        this.fragmentIndex  = fragmentIndex;
        this.totalFragments = totalFragments;
        this.payload        = payload;
    }

    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE + payload.length);
        buf.putLong(frameId);
        buf.putShort(fragmentIndex);
        buf.putShort(totalFragments);
        buf.put(payload);
        return buf.array();
    }

    /**
     * Désérialise un paquet reçu.
     * La taille du payload = taille totale - HEADER_SIZE (pas de champ taille dans le header).
     */
    public static VideoFragment fromBytes(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        long  frameId        = buf.getLong();
        short fragmentIndex  = buf.getShort();
        short totalFragments = buf.getShort();

        // Payload = tout ce qui reste après le header
        int payloadSize = data.length - HEADER_SIZE;
        byte[] payload  = new byte[payloadSize];
        buf.get(payload);

        return new VideoFragment(frameId, fragmentIndex, totalFragments, payload);
    }

    public long   getFrameId()        { return frameId; }
    public short  getFragmentIndex()  { return fragmentIndex; }
    public short  getTotalFragments() { return totalFragments; }
    public byte[] getPayload()        { return payload; }
    public boolean isLast()           { return fragmentIndex == totalFragments - 1; }
}