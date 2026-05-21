package fr.app.model;

public enum DroneType {

    BR2R4(
            "drone1",
            "ROV BlueRobotics BR2R4",
            "BR2R4"
    ),

    GUARDIAN(
            "drone1",
            "ROV SubseaTech Guardian",
            "Guardian"
    );

    private final String droneId;
    private final String label;
    private final String simulatorName;

    DroneType(String droneId, String label, String simulatorName) {
        this.droneId = droneId;
        this.label = label;
        this.simulatorName = simulatorName;
    }

    public String getDroneId() {
        return droneId;
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