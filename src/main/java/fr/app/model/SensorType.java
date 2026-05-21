package fr.app.model;

public enum SensorType {

    CAMERA("Caméra"),
    TEMPERATURE("Température"),
    DEPTH("Profondeur"),
    COLLISION("Collision"),
    PRESSURE("Pression"),
    SONAR("Sonar");

    private final String label;

    SensorType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}