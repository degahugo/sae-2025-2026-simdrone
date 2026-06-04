package fr.app.structure;

import fr.app.model.CommandeMessage;
import fr.app.model.DroneStatus;
import fr.app.model.MessageType;
import fr.app.model.PilotCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonSerialisationTest {

    private JsonSerializer serializer;

    @BeforeEach
    void setUp() {
        // Instanciation du serializer avant chaque test
        serializer = new JsonSerializer();
    }


    // Tests sérialisation CommandeMessage


    @Test
    void serialisation_CommandeForward_produitJsonValide() throws SerialisationException {
        CommandeMessage msg = new CommandeMessage(MessageType.COMMAND, "drone1", PilotCommand.FORWARD);
        byte[] bytes = serializer.serialisation(msg);
        String json = new String(bytes);

        // Vérifie que le JSON contient bien la commande
        assertTrue(json.contains("FORWARD"));
        assertTrue(json.contains("command"));
    }

    @Test
    void serialisation_produitsDesBytes() throws SerialisationException {
        CommandeMessage msg = new CommandeMessage(MessageType.COMMAND, "drone1", PilotCommand.LEFT);
        byte[] bytes = serializer.serialisation(msg);

        // Vérifie que le tableau de bytes n'est pas vide
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }


    // Tests désérialisation CommandeMessage


    @Test
    void deserialisation_JsonForward_retourneForward() throws SerialisationException {
        String json = "{\"command\":\"FORWARD\"}";
        CommandeMessage msg = serializer.deserisation(json.getBytes(), CommandeMessage.class);

        assertNotNull(msg);
        assertEquals(PilotCommand.FORWARD, msg.getCommand());
    }

    @Test
    void deserialisation_JsonRight_retourneRight() throws SerialisationException {
        String json = "{\"command\":\"RIGHT\"}";
        CommandeMessage msg = serializer.deserisation(json.getBytes(), CommandeMessage.class);

        assertEquals(PilotCommand.RIGHT, msg.getCommand());
    }

    @Test
    void deserialisation_JsonInvalide_lanceSerialisationException() {
        String jsonInvalide = "FORWARD"; // pas du JSON valide

        // Vérifie que l'exception est bien levée
        assertThrows(SerialisationException.class, () ->
                serializer.deserisation(jsonInvalide.getBytes(), CommandeMessage.class)
        );
    }


    // Tests aller-retour (sérialisation -> désérialisation)


    @Test
    void allerRetour_CommandeMessage_donneObjetIdentique() throws SerialisationException {
        CommandeMessage original = new CommandeMessage(MessageType.COMMAND, "drone1", PilotCommand.UP);

        // Sérialise puis désérialise
        byte[] bytes = serializer.serialisation(original);
        CommandeMessage resultat = serializer.deserisation(bytes, CommandeMessage.class);

        // L'objet reconstruit doit être identique à l'original
        assertEquals(original.getCommand(), resultat.getCommand());
    }

    @Test
    void allerRetour_toutesLesCommandes() throws SerialisationException {
        // Teste chaque valeur de l'enum PilotCommand
        for (PilotCommand cmd : PilotCommand.values()) {
            CommandeMessage original = new CommandeMessage(MessageType.COMMAND, "drone1", cmd);
            byte[] bytes = serializer.serialisation(original);
            CommandeMessage resultat = serializer.deserisation(bytes, CommandeMessage.class);

            assertEquals(cmd, resultat.getCommand(),
                    "Échec pour la commande : " + cmd);
        }
    }

    // Tests sérialisation DroneStatus

    @Test
    void serialisation_DroneStatus_contientTousLesChamps() throws SerialisationException {
        DroneStatus status = new DroneStatus();
        status.setState("En vol");

        byte[] bytes = serializer.serialisation(status);
        String json = new String(bytes);

        // Vérifie la présence des champs dans le JSON
        assertTrue(json.contains("state"));
        assertTrue(json.contains("En vol"));
        assertTrue(json.contains("x"));
        assertTrue(json.contains("y"));
        assertTrue(json.contains("z"));
        assertTrue(json.contains("speed"));
    }

    @Test
    void allerRetour_DroneStatus_donneObjetIdentique() throws SerialisationException {
        DroneStatus original = new DroneStatus();
        original.setState("Atterrissage");
        original.setX(1.5);
        original.setY(2.5);
        original.setZ(3.5);
        original.setSpeed(10.0);

        byte[] bytes = serializer.serialisation(original);
        DroneStatus resultat = serializer.deserisation(bytes, DroneStatus.class);

        assertEquals(original.getState(),  resultat.getState());
        assertEquals(original.getX(),      resultat.getX());
        assertEquals(original.getY(),      resultat.getY());
        assertEquals(original.getZ(),      resultat.getZ());
        assertEquals(original.getSpeed(),  resultat.getSpeed());
    }
}

