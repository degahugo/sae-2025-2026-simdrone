package fr.app.model;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import fr.app.model.sensor.Sensor;

import java.util.ArrayList;
import java.util.List;
import com.jme3.math.FastMath;
import fr.app.view.DroneBeamEffect;
import fr.app.view.DroneLights;


public class Drone {

    private String id;
    private Node droneSpatial;
    private DroneState state;
    private List<Sensor> sensors;
    private Battery battery;
    private boolean collisionDetected;
    private String collisionMessage = "Aucune collision";
    private DroneLights droneLights;
    private DroneBeamEffect beamEffect;

    public Drone(String id, Node droneSpatial,Node rootNode, AssetManager assetManager) {

        this.id = id;

        this.droneSpatial = droneSpatial;
        this.droneLights = new DroneLights(droneSpatial, rootNode);
        this.sensors = new ArrayList<>();
        this.beamEffect = new DroneBeamEffect(droneSpatial, rootNode,assetManager);

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

    public boolean isCollisionDetected() {
        return collisionDetected;
    }

    public void setCollisionDetected(boolean collisionDetected) {
        this.collisionDetected = collisionDetected;
    }

    public String getCollisionMessage() {
        return collisionMessage;
    }

    public void setCollisionMessage(String collisionMessage) {
        this.collisionMessage = collisionMessage;
    }

    public double getPitch() {
        float[] angles = droneSpatial.getLocalRotation().toAngles(null);
        return angles[0] * FastMath.RAD_TO_DEG;
    }

    public double getYaw() {
        float[] angles = droneSpatial.getLocalRotation().toAngles(null);
        return angles[1] * FastMath.RAD_TO_DEG;
    }

    public double getRoll() {
        float[] angles = droneSpatial.getLocalRotation().toAngles(null);
        return angles[2] * FastMath.RAD_TO_DEG;
    }

    public double getHeading() {
        double yaw = getYaw();
        if (yaw < 0) {
            yaw += 360;
        }
        return yaw;
    }
    public void update(float tpf) {
        droneLights.update();
        beamEffect.update();
    }
}