package fr.app.controller;

import fr.app.view.camera.DroneCameraManager;

public class DroneCameraController {
    private DroneCameraManager droneCameraManager;
    private DroneController droneController;

    public DroneCameraController(DroneCameraManager droneCameraManager, DroneController droneController){
        this.droneCameraManager = droneCameraManager;
        this.droneController = droneController;
    }

    public void update(float tpf){
        boolean camMoved = false;
        if(droneController.isCamLeftIntention()){
            droneCameraManager.rotateCameraLeft(tpf);
            camMoved = true;
        }
        if(droneController.isCamRightIntention()){
            droneCameraManager.rotateCameraRight(tpf);
            camMoved = true;
        }
        if(droneController.isCamUpIntention()){
            droneCameraManager.rotateCameraUp(tpf);
            camMoved = true;
        }
        if(droneController.isCamDownIntention()){
            droneCameraManager.rotateCameraDown(tpf);
            camMoved = true;
        }
        if(camMoved){
            droneCameraManager.getCameraNode().updateGeometricState();
        }
    }
}
