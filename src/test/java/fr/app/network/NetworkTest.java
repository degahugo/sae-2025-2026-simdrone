package fr.app.network;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.MessageType;
import fr.app.model.PilotCommand;
import fr.app.structure.JsonSerializer;
import org.junit.jupiter.api.*;

import javax.crypto.SecretKey;
import java.net.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration et tests unitaires du sous-système réseau DronSim.
 *
 * Organisation :
 *   - ProtocoleChiffrementTest  : tests unitaires AES/GCM et RSA (sans réseau)
 *   - RegistreSessionsTest      : tests unitaires du registre des sessions
 *   - TcpTest                   : tests d'intégration TCP + handshake RSA/AES
 *   - UdpTest                   : tests d'intégration UDP (envoi/réception chiffrés)
 */
class NetworkTest {

    // =========================================================
    //  1. PROTOCOLE DE CHIFFREMENT — tests unitaires purs
    // =========================================================

    @Nested
    @DisplayName("ProtocoleDeChiffrement")
    class ProtocoleChiffrementTest {

        private ProtocoleDeChiffrement protocole;

        @BeforeEach
        void setUp() throws Exception {
            protocole = new ProtocoleDeChiffrement();
            protocole.generateAESKey();
        }

        // --- AES/GCM ---

        @Test
        @DisplayName("Chiffrement AES puis déchiffrement → message identique")
        void chiffrementAesAllerRetour() throws Exception {
            byte[] original = "Commande : FORWARD".getBytes();

            byte[] chiffre = protocole.encrypt(original);
            byte[] dechiffre = protocole.decrypt(chiffre);

            assertArrayEquals(original, dechiffre);
        }

        @Test
        @DisplayName("Deux chiffrements du même message → IVs différents (AES/GCM)")
        void deuxChiffrementsProduitsDifferents() throws Exception {
            byte[] data = "test".getBytes();
            byte[] c1 = protocole.encrypt(data);
            byte[] c2 = protocole.encrypt(data);

            // Les IVs (12 premiers octets) doivent être différents
            boolean ivsDifferents = false;
            for (int i = 0; i < 12; i++) {
                if (c1[i] != c2[i]) {
                    ivsDifferents = true;
                    break;
                }
            }
            assertTrue(ivsDifferents, "AES/GCM doit générer un IV unique à chaque chiffrement");
        }

        @Test
        @DisplayName("Données corrompues → exception au déchiffrement")
        void donneesCorrompiuesLevantException() throws Exception {
            byte[] original = "test corruption".getBytes();
            byte[] chiffre = protocole.encrypt(original);

            // On corrompt quelques octets après l'IV
            chiffre[13]++;
            chiffre[14]++;

            assertThrows(Exception.class, () -> protocole.decrypt(chiffre),
                    "Un message corrompu doit lever une exception");
        }

        @Test
        @DisplayName("Chiffrement d'un message vide")
        void chiffrementMessageVide() throws Exception {
            byte[] vide = new byte[0];
            byte[] chiffre = protocole.encrypt(vide);
            byte[] dechiffre = protocole.decrypt(chiffre);
            assertArrayEquals(vide, dechiffre);
        }

        @Test
        @DisplayName("Chiffrement d'un grand message (4 Ko)")
        void chiffrementGrandMessage() throws Exception {
            byte[] grand = new byte[4096];
            new java.util.Random().nextBytes(grand);
            byte[] chiffre = protocole.encrypt(grand);
            byte[] dechiffre = protocole.decrypt(chiffre);
            assertArrayEquals(grand, dechiffre);
        }

        // --- RSA ---

        @Test
        @DisplayName("Échange RSA : chiffrer avec clé publique → déchiffrer avec clé privée")
        void echangeRsaAllerRetour() throws Exception {
            ProtocoleDeChiffrement serveur = new ProtocoleDeChiffrement();
            serveur.generateRSAKeys();

            ProtocoleDeChiffrement client = new ProtocoleDeChiffrement();
            SecretKey cleAes = client.generateAESKey();

            // Le client chiffre la clé AES avec la clé publique du serveur
            byte[] cleChiffree = client.encryptAESKey(cleAes, serveur.getPublicKey());

            // Le serveur déchiffre avec sa clé privée
            serveur.decryptAESKey(cleChiffree);

            assertArrayEquals(
                    cleAes.getEncoded(),
                    serveur.getAESKey().getEncoded(),
                    "La clé AES récupérée par le serveur doit être identique à celle du client"
            );
        }

        @Test
        @DisplayName("Clé AES corrompue lors de l'échange RSA → exception")
        void cleAesCorrompiueLevantException() throws Exception {
            ProtocoleDeChiffrement serveur = new ProtocoleDeChiffrement();
            serveur.generateRSAKeys();

            ProtocoleDeChiffrement client = new ProtocoleDeChiffrement();
            SecretKey cleAes = client.generateAESKey();
            byte[] cleChiffree = client.encryptAESKey(cleAes, serveur.getPublicKey());

            // Corruption de la clé chiffrée
            cleChiffree[0]++;

            assertThrows(Exception.class, () -> serveur.decryptAESKey(cleChiffree));
        }

        @Test
        @DisplayName("setAesKey puis encrypt/decrypt fonctionne")
        void setAesKeyExterne() throws Exception {
            ProtocoleDeChiffrement p1 = new ProtocoleDeChiffrement();
            SecretKey cle = p1.generateAESKey();

            ProtocoleDeChiffrement p2 = new ProtocoleDeChiffrement();
            p2.setAesKey(cle);

            byte[] data = "partage de clé".getBytes();
            byte[] chiffre = p1.encrypt(data);
            byte[] dechiffre = p2.decrypt(chiffre);

            assertArrayEquals(data, dechiffre,
                    "Deux protocoles partageant la même clé AES doivent pouvoir communiquer");
        }
    }

    // =========================================================
    //  2. REGISTRE DES SESSIONS — tests unitaires purs
    // =========================================================

    @Nested
    @DisplayName("RegistreDesSessions")
    class RegistreSessionsTest {

        private RegistreDesSessions registre;
        private SecretKey cleAes;

        @BeforeEach
        void setUp() throws Exception {
            registre = new RegistreDesSessions();
            ProtocoleDeChiffrement p = new ProtocoleDeChiffrement();
            cleAes = p.generateAESKey();
        }

        @Test
        @DisplayName("Enregistrer une session puis récupérer son protocole")
        void enregistrerEtRecuperer() {
            registre.enregistrer("192.168.1.1", 50000, 50001, cleAes);

            ProtocoleDeChiffrement protocole = registre.getProtocole("192.168.1.1", 50000);
            assertNotNull(protocole, "Le protocole doit être retrouvé après enregistrement");
        }

        @Test
        @DisplayName("Supprimer une session → protocole introuvable")
        void supprimerSession() {
            registre.enregistrer("10.0.0.1", 40000, 40001, cleAes);
            registre.supprimer("10.0.0.1");

            // Avec un seul client enregistré puis supprimé, getSessions() est vide
            assertTrue(registre.getSessions().isEmpty(),
                    "La session supprimée ne doit plus apparaître dans le registre");
        }

        @Test
        @DisplayName("Résolution par IP directe")
        void resolutionParIpDirecte() {
            registre.enregistrer("172.16.0.5", 55000, 55001, cleAes);
            String ip = registre.resoudreIpClient("172.16.0.5", 99999);
            assertEquals("172.16.0.5", ip);
        }

        @Test
        @DisplayName("Résolution par port UDP déclaré (IP différente, port connu)")
        void resolutionParPort() {
            registre.enregistrer("192.168.0.10", 62501, 62502, cleAes);
            // Même port UDP, IP différente (NAT)
            String ip = registre.resoudreIpClient("10.0.0.99", 62501);
            assertEquals("192.168.0.10", ip);
        }

        @Test
        @DisplayName("Fallback mono-client : un seul client → toujours résolu")
        void fallbackMonoClient() {
            registre.enregistrer("192.168.1.50", 60000, 60001, cleAes);
            // IP totalement inconnue, mais seul client
            String ip = registre.resoudreIpClient("8.8.8.8", 12345);
            assertEquals("192.168.1.50", ip);
        }

        @Test
        @DisplayName("Enregistrement du droneId et récupération")
        void droneIdAssociation() {
            registre.enregistrer("192.168.1.1", 50000, 50001, cleAes);
            registre.setDroneId("192.168.1.1", "drone1");
            assertEquals("drone1", registre.getDroneId("192.168.1.1"));
        }

        @Test
        @DisplayName("Port vidéo retourné correctement")
        void portVideoCorrect() {
            registre.enregistrer("192.168.1.1", 50000, 50002, cleAes);
            assertEquals(50002, registre.getPortUdpVideo("192.168.1.1"));
        }

        @Test
        @DisplayName("Plusieurs sessions : protocoles distincts")
        void plusieursSessionsIndependantes() throws Exception {
            ProtocoleDeChiffrement p2 = new ProtocoleDeChiffrement();
            SecretKey cle2 = p2.generateAESKey();

            registre.enregistrer("10.0.0.1", 51000, 51001, cleAes);
            registre.enregistrer("10.0.0.2", 52000, 52001, cle2);

            ProtocoleDeChiffrement proto1 = registre.getProtocole("10.0.0.1", 51000);
            ProtocoleDeChiffrement proto2 = registre.getProtocole("10.0.0.2", 52000);

            assertNotNull(proto1);
            assertNotNull(proto2);
            assertNotSame(proto1, proto2, "Chaque session doit avoir son propre protocole");
        }
    }

    // =========================================================
    //  3. TCP — tests d'intégration TcpSessionServeur / Client
    // =========================================================

    @Nested
    @DisplayName("TCP — TcpSessionServeur + TcpSessionClient")
    class TcpTest {

        private static final int PORT_TCP = 63100;
        private TcpSessionServeur serveurTcp;
        private Thread serveurThread;

        @BeforeEach
        void demarrerServeur() throws InterruptedException {
            RegistreDesSessions registre = new RegistreDesSessions();
            serveurTcp = new TcpSessionServeur(PORT_TCP, registre);
            serveurThread = new Thread(serveurTcp, "test-tcp-serveur");
            serveurThread.setDaemon(true);
            serveurThread.start();
            Thread.sleep(200); // attendre que le ServerSocket soit prêt
        }

        @AfterEach
        void arreterServeur() throws Exception {
            serveurTcp.stop();
        }

        @Test
        @DisplayName("Connexion TCP réussie — handshake RSA/AES")
        void connexionTcpHandshake() {
            TcpSessionClient client = new TcpSessionClient("127.0.0.1", PORT_TCP);
            assertDoesNotThrow(client::connect,
                    "La connexion TCP avec handshake RSA/AES ne doit pas lever d'exception");
            assertTrue(client.isConnected());
            assertDoesNotThrow(client::disconnect);
        }

        @Test
        @DisplayName("Connexion TCP échoue si le serveur est absent")
        void connexionTcpServeurAbsent() {
            TcpSessionClient client = new TcpSessionClient("127.0.0.1", 63199);
            assertThrows(Exception.class, client::connect);
        }

        @Test
        @DisplayName("Clé AES disponible après le handshake")
        void cleAesDisponibleApresHandshake() throws Exception {
            TcpSessionClient client = new TcpSessionClient("127.0.0.1", PORT_TCP);
            client.connect();
            assertNotNull(client.getAesKey(),
                    "La clé AES doit être disponible après le handshake RSA");
            client.disconnect();
        }

        @Test
        @DisplayName("isConnected() retourne false après déconnexion")
        void isConnectedApresDeconnexion() throws Exception {
            TcpSessionClient client = new TcpSessionClient("127.0.0.1", PORT_TCP);
            client.connect();
            assertTrue(client.isConnected());
            client.disconnect();
            assertFalse(client.isConnected());
        }

        @Test
        @DisplayName("Connexions simultanées de deux clients")
        void deuxClientsSimultanes() throws Exception {
            TcpSessionClient c1 = new TcpSessionClient("127.0.0.1", PORT_TCP);
            TcpSessionClient c2 = new TcpSessionClient("127.0.0.1", PORT_TCP);

            assertDoesNotThrow(c1::connect);
            assertDoesNotThrow(c2::connect);

            assertTrue(c1.isConnected());
            assertTrue(c2.isConnected());

            // Clés AES distinctes pour chaque client
            assertFalse(
                    java.util.Arrays.equals(
                            c1.getAesKey().getEncoded(),
                            c2.getAesKey().getEncoded()
                    ),
                    "Chaque client doit négocier une clé AES unique"
            );

            c1.disconnect();
            c2.disconnect();
        }
    }

    // =========================================================
    //  4. UDP — tests d'intégration UdpClientTransport
    // =========================================================

    @Nested
    @DisplayName("UDP — UdpClientTransport + UdpServeurTransport")
    class UdpTest {

        private static final int PORT_UDP = 63200;

        private ProtocoleDeChiffrement protocoleServeur;
        private ProtocoleDeChiffrement protocoleClient;
        private DatagramSocket socketServeur;
        private Thread serveurThread;

        @BeforeEach
        void setUp() throws Exception {
            protocoleServeur = new ProtocoleDeChiffrement();
            SecretKey cle = protocoleServeur.generateAESKey();

            protocoleClient = new ProtocoleDeChiffrement();
            protocoleClient.setAesKey(cle);

            socketServeur = new DatagramSocket(PORT_UDP);
            socketServeur.setSoTimeout(3000);
        }

        @AfterEach
        void tearDown() {
            if (socketServeur != null && !socketServeur.isClosed()) {
                socketServeur.close();
            }
        }

        @Test
        @DisplayName("Envoi d'une commande UDP chiffrée reçue et déchiffrée côté serveur")
        void envoiCommandeUdpChiffree() throws Exception {
            UdpClientTransport client =
                    new UdpClientTransport("127.0.0.1", PORT_UDP, protocoleClient);

            client.sendCommand("drone1", PilotCommand.FORWARD);

            // Réception côté serveur
            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socketServeur.receive(packet);

            byte[] data = java.util.Arrays.copyOf(packet.getData(), packet.getLength());
            byte[] brut = protocoleServeur.decrypt(data);

            JsonSerializer serializer = new JsonSerializer();
            CommandeMessage msg = serializer.deserisation(brut, CommandeMessage.class);

            assertEquals(MessageType.COMMAND, msg.getType());
            assertEquals("drone1", msg.getDroneId());
            assertEquals(PilotCommand.FORWARD, msg.getCommand());

            client.close();
        }

        @Test
        @DisplayName("Réception d'un DroneStatus UDP chiffré par le client")
        void receptionStatutUdpChiffre() throws Exception {
            UdpClientTransport client =
                    new UdpClientTransport("127.0.0.1", PORT_UDP, protocoleClient);

            // Le serveur envoie un statut au client
            DroneStatus statut = new DroneStatus();
            statut.setState("En simulation");
            statut.setX(1.5);
            statut.setY(2.0);
            statut.setZ(-3.0);

            JsonSerializer serializer = new JsonSerializer();
            byte[] brut = serializer.serialisation(statut);
            byte[] chiffre = protocoleServeur.encrypt(brut);

            // On détermine le port local du client pour lui envoyer le statut
            int portClient = client.getLocalPort();
            DatagramPacket reponse = new DatagramPacket(
                    chiffre, chiffre.length,
                    InetAddress.getByName("127.0.0.1"), portClient
            );
            socketServeur.send(reponse);

            // Le client doit recevoir le statut via son thread de réception
            DroneStatus recu = client.receiveStatus();

            assertNotNull(recu);
            assertEquals("En simulation", recu.getState());
            assertEquals(1.5, recu.getX(), 0.001);

            client.close();
        }

        @Test
        @DisplayName("Timeout si aucun statut reçu")
        void timeoutSiAucunStatut() {
            assertThrows(Exception.class, () -> {
                UdpClientTransport client =
                        new UdpClientTransport("127.0.0.1", PORT_UDP, protocoleClient);
                // Aucun paquet envoyé → doit lever SocketTimeoutException
                client.receiveStatus();
                client.close();
            });
        }

        @Test
        @DisplayName("Envoi de START_SIMULATION via UDP")
        void envoiStartSimulationUdp() throws Exception {
            UdpClientTransport client =
                    new UdpClientTransport("127.0.0.1", PORT_UDP, protocoleClient);

            client.sendStartSimulation("drone1");

            byte[] buffer = new byte[4096];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socketServeur.receive(packet);

            byte[] data = java.util.Arrays.copyOf(packet.getData(), packet.getLength());
            byte[] brut = protocoleServeur.decrypt(data);

            JsonSerializer serializer = new JsonSerializer();
            CommandeMessage msg = serializer.deserisation(brut, CommandeMessage.class);

            assertEquals(MessageType.START_SIMULATION, msg.getType());
            assertEquals("drone1", msg.getDroneId());

            client.close();
        }

        @Test
        @DisplayName("Paquet chiffré avec une mauvaise clé → ignoré")
        void paquetMauvaiseClefIgnore() throws Exception {
            // Un client utilisant une clé différente
            ProtocoleDeChiffrement autreProtocole = new ProtocoleDeChiffrement();
            autreProtocole.generateAESKey(); // clé différente

            UdpClientTransport client =
                    new UdpClientTransport("127.0.0.1", PORT_UDP, protocoleClient);

            // Envoi d'un paquet chiffré avec la mauvaise clé
            byte[] fausseDonnee = autreProtocole.encrypt("malicious".getBytes());
            DatagramPacket faux = new DatagramPacket(
                    fausseDonnee, fausseDonnee.length,
                    InetAddress.getByName("127.0.0.1"), client.getLocalPort()
            );
            socketServeur.send(faux);

            // Le client ne doit pas lever d'exception — il ignore le paquet
            Thread.sleep(300);
            // Aucun statut ne doit avoir été enregistré
            assertThrows(Exception.class, client::receiveStatus,
                    "Aucun statut ne doit être disponible après un paquet avec une mauvaise clé");

            client.close();
        }

        @Test
        @DisplayName("receiveStatusFrais() vide le cache avant d'attendre")
        void receiveStatusFraisVideLeCache() throws Exception {
            UdpClientTransport client =
                    new UdpClientTransport("127.0.0.1", PORT_UDP, protocoleClient);

            // Envoyer un premier statut
            DroneStatus s1 = new DroneStatus();
            s1.setState("ancien");
            JsonSerializer serializer = new JsonSerializer();
            byte[] chiffre1 = protocoleServeur.encrypt(serializer.serialisation(s1));
            socketServeur.send(new DatagramPacket(chiffre1, chiffre1.length,
                    InetAddress.getByName("127.0.0.1"), client.getLocalPort()));
            Thread.sleep(200);

            // Maintenant on attend un statut "frais" — envoyer un second statut après le vidage
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(300);
                    DroneStatus s2 = new DroneStatus();
                    s2.setState("frais");
                    byte[] chiffre2 = protocoleServeur.encrypt(serializer.serialisation(s2));
                    socketServeur.send(new DatagramPacket(chiffre2, chiffre2.length,
                            InetAddress.getByName("127.0.0.1"), client.getLocalPort()));
                } catch (Exception ignored) {}
            });

            DroneStatus recu = client.receiveStatusFrais();
            assertEquals("frais", recu.getState());

            client.close();
        }
    }
}