package fr.app.model;

import com.jme3.scene.Node;

public class Drone {

    private String id;

    private Node droneSpatial;

    public Drone(String id,Node droneSpatial) {
        this.id = id;
        this.droneSpatial = droneSpatial;
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
}

