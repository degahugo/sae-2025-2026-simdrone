package fr.app.model;

import fr.app.model.Environment.EnvironmentType;

import java.util.ArrayList;
import java.util.List;

public class SimulationConfiguration {

    private EnvironmentType environmentType;
    private DroneType droneType;
    private List<SensorType> sensors = new ArrayList<>();
    private String condition;

    public EnvironmentType getEnvironmentType() {
        return environmentType;
    }

    public void setEnvironmentType(EnvironmentType environmentType) {
        this.environmentType = environmentType;
    }

    public DroneType getDroneType() {
        return droneType;
    }

    public void setDroneType(DroneType droneType) {
        this.droneType = droneType;
    }

    public List<SensorType> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorType> sensors) {
        this.sensors = sensors;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}