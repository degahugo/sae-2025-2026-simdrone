package fr.app.structure;

public class SerialisationException extends Exception {

    public SerialisationException(String message, Throwable cause) {
        super(message, cause);
    }
    public SerialisationException(String message) {
        super(message);
    }
}