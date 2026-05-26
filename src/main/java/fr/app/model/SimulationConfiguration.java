package fr.app.model;

import fr.app.model.environment.EnvironmentConditions;
import fr.app.model.environment.EnvironmentType;

import java.util.ArrayList;
import java.util.List;

public class SimulationConfiguration {

    private EnvironmentType environmentType;
    private DroneType droneType;
    private List<SensorType> sensors = new ArrayList<>();
    private EnvironmentConditions condition;
    private EnvironmentConditions visibility;

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

    public EnvironmentConditions getCondition() {
        return condition;
    }

    public void setCondition(EnvironmentConditions condition) {
        this.condition = condition;
    }

    public EnvironmentConditions getVisibility(){
        return visibility;
    }

    public void setVisibility(EnvironmentConditions visibility){
        this.visibility = visibility;
    }
}