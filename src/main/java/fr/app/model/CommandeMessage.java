package fr.app.model;

public class CommandeMessage {

    private String droneId;
    private PilotCommand command;
    private MessageType type;
    private SimulationConfiguration configuration;

    public CommandeMessage() {} // constructeur vide requis par Jackson


    public CommandeMessage(MessageType type, String droneId, PilotCommand command) {
        this.droneId = droneId;
        this.command = command;
        this.type = type;
    }
    public CommandeMessage(MessageType type,
                           String droneId,
                           PilotCommand command,
                           SimulationConfiguration configuration) {
        this.type = type;
        this.droneId = droneId;
        this.command = command;
        this.configuration = configuration;
    }


    public SimulationConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(SimulationConfiguration configuration) {
        this.configuration = configuration;
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