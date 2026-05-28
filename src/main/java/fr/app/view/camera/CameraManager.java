package fr.app.view.camera;

import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

public class CameraManager {
    private final Camera cam;
    private final FlyByCamera flyCam;

    public CameraManager(Camera cam, FlyByCamera flyCam){
        this.cam = cam;
        this.flyCam = flyCam;
    }

    /**
     * Configure the camera settings to fly around
     */
    public void configureCamera(boolean flyCamEnable) {
        this.cam.setLocation(new Vector3f(10f, 10f, 10f));
        this.cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        this.flyCam.setMoveSpeed(10f);
        this.flyCam.setEnabled(flyCamEnable);
    }

    /**
     * Enables the flying camera
     */
    public void enableFlyCam(){
        if (flyCam != null) {
            this.flyCam.setEnabled(true);
        }
    }

    /**
     * Disable the flying camera
     */
    public void disableFlyCam(){
        if (flyCam != null) {
            this.flyCam.setEnabled(false);
        }
    }

    /**
     * @return true if the flying camera is enabled or false otherwise
     */
    public boolean isFlyCamEnabled(){
        if (flyCam != null) {
            return this.flyCam.isEnabled();
        }
        return false;
    }

    /**
     * @return flying camera speed
     */
    public float getFlyCamSpeed(){
        if (flyCam != null) {
            return flyCam.getMoveSpeed();
        }
        return 0f;
    }

    /**
     * @param speed of the flying camera
     */
    public void setFlyCamSpeed(float speed){
        if (flyCam != null) {
            flyCam.setMoveSpeed(speed);
        }
    }
}
