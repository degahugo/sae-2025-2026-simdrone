package fr.app.model;

public class DroneState {

    private float speedFactor;
    private float health;
    public DroneState() {

        this.speedFactor = 1.0f;
        this.health = 100.0f;
    }

    public float getSpeedFactor() {
        return speedFactor;
    }

    public void setSpeedFactor(float speedFactor) {
        this.speedFactor = speedFactor;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }
}