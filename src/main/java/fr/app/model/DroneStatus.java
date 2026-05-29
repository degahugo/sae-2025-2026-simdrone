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

    private double yaw;
    private double pitch;
    private double roll;
    private boolean collisionDetected;
    private String collisionMessage;
    private String environmentName;
    private String droneType;
    private double battery;
    private double heading;
    private double pressure;
    private float frontCamPitch;
    private float frontCamYaw;
    private float rearCamPitch;
    private float rearCamYaw;
    private boolean isShowingFrontCamera;


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
    public double getHeading() {
        return heading;
    }
    public double getPressure() {
        return pressure;
    }

    public double getBattery() {
        return battery;
    }


    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }


    public double getRoll() {
        return roll;
    }


    public boolean isCollisionDetected() {
        return collisionDetected;
    }


    public String getCollisionMessage() {
        return collisionMessage;
    }



    public String getEnvironmentName() {
        return environmentName;
    }



    public String getDroneType() {
        return droneType;
    }

    public float getFrontCamPitch(){ return frontCamPitch; }
    public float getFrontCamYaw(){ return frontCamYaw; }
    public float getRearCamPitch(){ return rearCamPitch; }
    public float getRearCamYaw(){ return rearCamYaw; }
    public boolean getShowingFrontCamera(){ return isShowingFrontCamera; }


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
    public void setBattery(double battery) {
        this.battery = battery;
    }
    public void setPressure(double pressure) {
        this.pressure = pressure;
    }
    public void setHeading(double heading) {
        this.heading = heading;
    }



    public void setDroneType(String droneType) {
        this.droneType = droneType;
    }
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }
    public void setCollisionMessage(String collisionMessage) {
        this.collisionMessage = collisionMessage;
    }
    public void setCollisionDetected(boolean collisionDetected) {
        this.collisionDetected = collisionDetected;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }


    public void setYaw(double yaw) {
        this.yaw = yaw;
    }
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

    public void setFrontCamPitch(float frontCamPitch) { this.frontCamPitch = frontCamPitch; }
    public void setFrontCamYaw(float frontCamYaw) {this.frontCamYaw = frontCamYaw; }
    public void setRearCamPitch(float rearCamPitch) { this.rearCamPitch = rearCamPitch; }
    public void setRearCamYaw(float rearCamYaw) { this.rearCamYaw = rearCamYaw; }
    public void setShowingFrontCamera(boolean showingFrontCamera){ this.isShowingFrontCamera = showingFrontCamera; }

    // @JsonIgnore -> Jackson ignorera cette méthode lors de la sérialisation/désérialisation
    @JsonIgnore
    public String getTemperatureText() {
        return temperature + " °C";
    }
}