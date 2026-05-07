package fr.app.network;

// Import des outils de chiffrement fournis par Java
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class ProtocoleDeChiffrement {

    // 🔐 Clé AES (clé secrète partagée après la connexion)
    private SecretKey aesKey;

    // 🔐 Clés RSA (publique + privée) utilisées par le serveur
    private KeyPair rsaKeyPair;

    // =========================
    // 🔵 PARTIE SERVEUR
    // =========================

    /**
     * Le serveur génère ses clés RSA au démarrage
     */
    public void initServeur() throws Exception {
        // Création du générateur RSA
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

        // Taille de clé sécurisée
        kpg.initialize(2048);

        // Génération des clés (publique + privée)
        rsaKeyPair = kpg.generateKeyPair();
    }

    /**
     * Le serveur envoie sa clé publique au client
     */
    public PublicKey getClePublique() {
        return rsaKeyPair.getPublic();
    }

    /**
     * Le serveur reçoit la clé AES chiffrée par le client
     */
    public void recevoirCleAES(byte[] cleChiffree) throws Exception {

        // Création du déchiffreur RSA
        Cipher cipher = Cipher.getInstance("RSA");

        // On utilise la clé privée pour déchiffrer
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());

        // Déchiffrement de la clé AES
        byte[] aesBytes = cipher.doFinal(cleChiffree);

        // Transformation en clé AES utilisable
        aesKey = new SecretKeySpec(aesBytes, "AES");
    }

    // =========================
    // 🟢 PARTIE CLIENT
    // =========================

    /**
     * Le client crée une clé AES et la chiffre avec la clé publique du serveur
     */
    public byte[] creerEtChiffrerCleAES(PublicKey clePublique) throws Exception {

        // Génération d’une clé AES
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        aesKey = keyGen.generateKey();

        // Création du chiffrement RSA
        Cipher cipher = Cipher.getInstance("RSA");

        // On chiffre avec la clé publique du serveur
        cipher.init(Cipher.ENCRYPT_MODE, clePublique);

        // Retourne la clé AES chiffrée
        return cipher.doFinal(aesKey.getEncoded());
    }

    // =========================
    // 🔁 PARTIE AES (COMMUNICATION)
    // =========================

    /**
     * Chiffrer un message avec AES
     */
    public byte[] chiffrer(byte[] data) throws Exception {

        // Mode sécurisé AES/GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // IV = nombre aléatoire obligatoire
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);

        // Paramètre GCM
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);

        // Initialisation du chiffrement
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);

        // Chiffrement du message
        byte[] encrypted = cipher.doFinal(data);

        // On combine IV + message chiffré
        byte[] result = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return result;
    }

    /**
     * Déchiffrer un message AES
     */
    public byte[] dechiffrer(byte[] data) throws Exception {

        // On récupère le IV (12 premiers bytes)
        byte[] iv = new byte[12];

        // Le reste = message chiffré
        byte[] encrypted = new byte[data.length - 12];

        System.arraycopy(data, 0, iv, 0, 12);
        System.arraycopy(data, 12, encrypted, 0, encrypted.length);

        // Préparation du déchiffrement
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));

        // Déchiffrement
        return cipher.doFinal(encrypted);
    }
}