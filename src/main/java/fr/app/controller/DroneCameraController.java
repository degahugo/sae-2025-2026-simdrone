package fr.app.controller;

import fr.app.model.DroneManager;
import fr.app.view.camera.DroneCameraManager;

public class DroneCameraController {
    private DroneCameraManager frontCameraManager;
    private DroneCameraManager rearCameraManager;
    private DroneController droneController;
    private DroneManager droneManager;

    private boolean isCameraSwitched = false;

    public DroneCameraController(DroneCameraManager frontCameraManager, DroneCameraManager rearCameraManager, DroneController droneController, DroneManager droneManager){
        this.frontCameraManager = frontCameraManager;
        this.rearCameraManager = rearCameraManager;
        this.droneController = droneController;
        this.droneManager = droneManager;
    }

    public void update(float tpf){
        if (droneController.isSwitchCameraIntention()) {
            if (!isCameraSwitched) {
                droneManager.switchCamera();
                isCameraSwitched = true;
            }
        } else {
            isCameraSwitched = false;
        }
        DroneCameraManager usedCameraManager;
        if (droneManager.isShowingFrontCam()) {
            usedCameraManager = frontCameraManager;
        } else {
            usedCameraManager = rearCameraManager;
        }
        boolean camMoved = false;
        if(droneController.isCamLeftIntention()){
            usedCameraManager.rotateCameraLeft(tpf);
            camMoved = true;
        }
        if(droneController.isCamRightIntention()){
            usedCameraManager.rotateCameraRight(tpf);
            camMoved = true;
        }
        if(droneController.isCamUpIntention()){
            usedCameraManager.rotateCameraUp(tpf);
            camMoved = true;
        }
        if(droneController.isCamDownIntention()){
            usedCameraManager.rotateCameraDown(tpf);
            camMoved = true;
        }
        if(camMoved){
            usedCameraManager.getCameraNode().updateGeometricState();
        }
    }
}
