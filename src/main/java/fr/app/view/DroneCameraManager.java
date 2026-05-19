package fr.app.view;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

import fr.app.model.Drone;

public class DroneCameraManager {

    private final Camera cam;
    private final Drone drone;
    private final Node cameraNode;


    private final Vector3f cameraOffset =
            new Vector3f(0f, 0f, 0f);

    public DroneCameraManager(Camera cam,
                              Drone drone) {

        this.cam = cam;
        this.drone = drone;

        this.cameraNode = new Node("CameraNode");

        drone.getDroneSpatial()
                .attachChild(cameraNode);


        cameraNode.setLocalTranslation(cameraOffset);
    }

    public void update(float tpf) {


        Vector3f cameraWorldPosition =
                cameraNode.getWorldTranslation();

        Vector3f dronePosition =
                drone.getDroneSpatial()
                        .getWorldTranslation();
        cam.setLocation(cameraWorldPosition);


        Vector3f forward =
                drone.getDroneSpatial()
                        .getWorldRotation()
                        .mult(Vector3f.UNIT_Z);


        Vector3f lookTarget =
                dronePosition
                        .add(forward.mult(6f))
                        .add(0f, 0f, 0f);

        cam.lookAt(
                lookTarget,
                Vector3f.UNIT_Y
        );
    }
}