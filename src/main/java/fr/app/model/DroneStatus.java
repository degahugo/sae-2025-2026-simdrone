package fr.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DroneStatus {

    private double x;
    private double y;
    private double z;
    private double speed;
    private String state;
    private double temperature;
    private double depth; // profondeur
    private double intensity; // intensité de la distorsion

    public DroneStatus() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.speed = 0;
        this.state = "En attente";
        this.temperature = 0;
    }

    //Getter
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getSpeed() { return speed; }
    public double getTemperature() {
        return temperature;
    }
    public double getDepth() { return depth; }
    public double getIntensity() { return intensity; }

    //Setter
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    public void setDepth(double depth) { this.depth = depth; }
    public void setIntensity(double intensity) { this.intensity = intensity; }

    // @JsonIgnore -> Jackson ignorera cette méthode lors de la sérialisation/désérialisation
    @JsonIgnore
    public String getPositionText() {
        return "x=" + x + " | y=" + y + " | z=" + z;
    }

    // @JsonIgnore -> Jackson ignorera cette méthode lors de la sérialisation/désérialisation
    @JsonIgnore
    public String getSpeedText() {
        return speed + " m/s";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    // @JsonIgnore -> Jackson ignorera cette méthode lors de la sérialisation/désérialisation
    @JsonIgnore
    public String getTemperatureText() {
        return temperature + " °C";
    }
}