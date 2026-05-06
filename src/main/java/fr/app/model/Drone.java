package fr.app.model;

import com.jme3.scene.Node;

public class Drone {

    private Node droneSpatial;

    public Drone(Node droneSpatial) {
        this.droneSpatial = droneSpatial;
    }

    public Node getDroneSpatial() {
        return droneSpatial;
    }

    public void setDroneSpatial(Node droneSpatial) {
        this.droneSpatial = droneSpatial;
    }
}

