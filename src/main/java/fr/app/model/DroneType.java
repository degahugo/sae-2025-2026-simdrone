package fr.app.model;

public enum DroneType {

    BR2R4(
            "ROV BlueRobotics BR2R4",
            "BR2R4"
    ),

    GUARDIAN(
            "ROV SubseaTech Guardian",
            "Guardian"
    );

    private final String label;
    private final String simulatorName;

    DroneType(String label, String simulatorName) {
        this.label = label;
        this.simulatorName = simulatorName;
    }

    public String getLabel() {
        return label;
    }

    public String getSimulatorName() {
        return simulatorName;
    }

    @Override
    public String toString() {
        return label;
    }
}