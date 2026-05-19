package fr.app.model;

import com.jme3.scene.Node;

import java.util.ArrayList;
import java.util.List;
import fr.app.model.DroneState;
import fr.app.model.Battery;

import fr.app.model.sensor.Sensor;

public class Drone {

    private Node droneSpatial;
    private DroneState state;
    private List<Sensor> sensors;
    private Battery battery;

    public Drone(Node droneSpatial) {

        this.droneSpatial = droneSpatial;
        this.sensors = new ArrayList<>();
        this.state = new DroneState();
        this.battery=new Battery(100f);
    }

    public Node getDroneSpatial() {
        return droneSpatial;
    }

    public void setDroneSpatial(Node droneSpatial) {
        this.droneSpatial = droneSpatial;
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

    public DroneState getState() {
        return state;
    }

    public void setState(DroneState state) {
        this.state = state;
    }
    public Battery getBattery() {return battery;}
}