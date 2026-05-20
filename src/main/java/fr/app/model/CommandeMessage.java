package fr.app.model;

public class CommandeMessage {

    private String droneId;
    private PilotCommand command;
    private MessageType type;

    public CommandeMessage() {} // constructeur vide requis par Jackson


    public CommandeMessage(MessageType type, String droneId, PilotCommand command) {
        this.droneId = droneId;
        this.command = command;
        this.type = type;
    }

    public String getDroneId() {
        return droneId;
    }
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public PilotCommand getCommand() {
        return command;
    }

    public void setCommand(PilotCommand command) {
        this.command = command;
    }
}