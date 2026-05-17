package fr.app.network;


import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

/**
 * Gère le chiffrement RSA (échange de clé) et AES/GCM (messages).
 * Cette classe est indépendante du réseau : elle ne fait que chiffrer/déchiffrer.
 */
public class ProtocoleDeChiffrement {

    // --- Clés RSA (serveur uniquement) ---
    private PublicKey rsaPublicKey;
    private PrivateKey rsaPrivateKey;

    // --- Clé AES partagée (client + serveur) ---
    private SecretKey aesKey;

    // --- Paramètres AES/GCM ---
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;

    // ============================================================
    // 1. RSA — utilisé pour échanger la clé AES
    // ============================================================

    /**
     * Génère une paire RSA (serveur).
     */
    public void generateRSAKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair pair = kpg.generateKeyPair();
        rsaPublicKey = pair.getPublic();
        rsaPrivateKey = pair.getPrivate();
    }

    public SecretKey getAESKey() {
        return aesKey;
    }


    /**
     * Retourne la clé publique RSA (serveur → client).
     */
    public PublicKey getPublicKey() {
        return rsaPublicKey;
    }

    /**
     * Déchiffre la clé AES envoyée par le client (serveur).
     */
    public void decryptAESKey(byte[] encryptedAES) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);
        byte[] aesBytes = rsaCipher.doFinal(encryptedAES);
        aesKey = new SecretKeySpec(aesBytes, "AES");
    }

    // ============================================================
    // 2. AES — utilisé pour chiffrer les messages
    // ============================================================

    /**
     * Génère une clé AES (client).
     */
    public SecretKey generateAESKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        aesKey = keyGen.generateKey();
        return aesKey;
    }

    /**
     * Chiffre la clé AES avec RSA (client → serveur).
     */
    public byte[] encryptAESKey(SecretKey aesKey, PublicKey rsaPublicKey) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        return rsaCipher.doFinal(aesKey.getEncoded());
    }

    /**
     * Chiffre un message avec AES/GCM.
     * Retourne un tableau contenant {IV, données chiffrées}.
     */
    public ChiffreAES encryptAES(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(data);

        return new ChiffreAES(iv, encrypted);
    }

    /**
     * Déchiffre un message AES/GCM.
     */
    public byte[] decryptAES(byte[] encryptedData, byte[] iv) throws Exception {
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, spec);
        return cipher.doFinal(encryptedData);
    }

    // ============================================================
    // Classe interne pour stocker IV + données chiffrées
    // ============================================================

    public static class ChiffreAES {
        public final byte[] iv;
        public final byte[] data;

        public ChiffreAES(byte[] iv, byte[] data) {
            this.iv = iv;
            this.data = data;
        }
    }
}
