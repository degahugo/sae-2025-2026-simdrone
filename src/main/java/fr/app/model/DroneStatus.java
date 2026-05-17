package fr.app.model;

public class DroneStatus {

    private double x;
    private double y;
    private double z;
    private double speed;
    private String state;

    public DroneStatus() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.speed = 0;
        this.state = "En attente";
    }

    public String getPositionText() {
        return "x=" + x + " | y=" + y + " | z=" + z;
    }

    public String getSpeedText() {
        return speed + " m/s";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}