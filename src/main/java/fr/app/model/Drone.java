package fr.app.model;

import com.jme3.scene.Node;
import fr.app.model.sensor.Sensor;

import java.util.ArrayList;
import java.util.List;

public class Drone {

    private String id;

    private Node droneSpatial;

    private DroneState state;

    private List<Sensor> sensors;

    private Battery battery;

    public Drone(String id, Node droneSpatial) {

        this.id = id;

        this.droneSpatial = droneSpatial;

        this.sensors = new ArrayList<>();

        this.state = new DroneState();

        this.battery = new Battery(100f);
    }

    public String getId() {
        return id;
    }

    public Node getDroneSpatial() {
        return droneSpatial;
    }

    public void setDroneSpatial(Node droneSpatial) {
        this.droneSpatial = droneSpatial;
    }

    public DroneState getState() {
        return state;
    }

    public void setState(DroneState state) {
        this.state = state;
    }

    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public Battery getBattery() {
        return battery;
    }
}