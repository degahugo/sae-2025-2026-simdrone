package fr.app.model;

public class CommandeMessage {
    private PilotCommand command;

    public CommandeMessage() {} // constructeur vide requis par Jackson

    public CommandeMessage(PilotCommand command) {
        this.command = command;
    }

    public PilotCommand getCommand() { return command; }
    public void setCommand(PilotCommand command) { this.command = command; }
}
