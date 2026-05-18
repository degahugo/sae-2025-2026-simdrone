package fr.app.controller;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;

import com.jme3.scene.control.UpdateControl;
import fr.app.model.Drone;
import fr.app.model.DroneMovement;

public class DroneController {

    private InputManager inputManager;
    private Drone drone;

    // Speed for cinematic mode
    private float droneSpeed = 0.2f;
    // Speed for the physic mode
    private float thrustPower = 10f;
    // Rotation speed for the physic mode
    private float torquePower = 0.5f;
    // Max speed, rotation speed dans height
    private float maxRotationSpeed = 2.0f;
    private float waterHeight;
    private float maxSpeed = 20f;

    // intentions
    private boolean forwardIntention = false;
    private boolean backwardIntention = false;
    private boolean leftIntention = false;
    private boolean rightIntention = false;
    private boolean rotateLeftIntention = false;
    private boolean rotateRightIntention = false;
    private boolean upIntention = false;
    private boolean downIntention = false;
    private RigidBodyControl physicsControl;
    private float mass;
    private float gravityCounterForce;

    // vecteurs
   // private Vector3f forwardMovement = new Vector3f(0, 0, -1);
    //private Vector3f backwardMovement = new Vector3f(0, 0, 1);
    //private Vector3f leftMovement = new Vector3f(-1, 0, 0);
    //private Vector3f rightMovement = new Vector3f(1, 0, 0);
    //private Vector3f upMovement = new Vector3f(0, 1, 0);
    //private Vector3f downMovement = new Vector3f(0, -1, 0);

    // noms des inputs et des mouvements
    DroneMovement moveForward = DroneMovement.FORWARD;
    DroneMovement moveBackward = DroneMovement.BACKWARD;
    DroneMovement moveLeft = DroneMovement.LEFT;
    DroneMovement moveRight = DroneMovement.RIGHT;
    DroneMovement rotateLeft = DroneMovement.ROTATELEFT;
    DroneMovement rotateRight = DroneMovement.ROTATERIGHT;
    DroneMovement moveUp = DroneMovement.UP;
    DroneMovement moveDown = DroneMovement.DOWN;




    public DroneController(InputManager inputManager, Drone drone) {
        this.inputManager = inputManager;
        this.drone = drone;
        physicsControl = drone.getDroneSpatial().getControl(RigidBodyControl.class);
        mass = physicsControl.getMass();
        gravityCounterForce = mass * 9.81f;
    }

    /**
     * Getter
     * @return Mass of the drone
     */
    public float getMass(){
        return this.mass;
    }

    /**
     * Setter
     * @param newMass New mass of the drone
     */
    public void setMass(float newMass){
        this.mass = newMass;
    }

    /**
     * Getter
     * @return Speed multiplier of the drone
     */
    public float getDroneSpeed(){
        return this.droneSpeed;
    }

    /**
     * Setter
     * @param newSpeed New speed of the drone
     */
    public void setDroneSpeed(float newSpeed){
        this.droneSpeed = newSpeed;
    }


    /**
     *
     */
    public void setUpKeys() {
        // Key mappings
        inputManager.addMapping(moveForward.name(), new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(moveBackward.name(), new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(moveLeft.name(), new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(moveRight.name(), new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(rotateRight.name(), new KeyTrigger(KeyInput.KEY_E));
        inputManager.addMapping(rotateLeft.name(), new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(moveUp.name(), new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping(moveDown.name(), new KeyTrigger(KeyInput.KEY_X));
        // Listeners
        inputManager.addListener(actionListener,
                moveForward.name(),
                moveBackward.name(),
                moveLeft.name(),
                moveRight.name(),
                rotateLeft.name(),
                rotateRight.name(),
                moveDown.name(),
                moveUp.name()
        );
    }

    private final ActionListener actionListener = new ActionListener() {
        //une touche après l'autre
        @Override
        public void onAction(String name,
                             boolean keyPressed,
                             float tpf) {
            switch (name) {
                    case "FORWARD":
                        forwardIntention = keyPressed;break;
                    case "BACKWARD":
                        backwardIntention = keyPressed;break;
                    case "LEFT":
                        leftIntention = keyPressed;break;
                    case "RIGHT":
                        rightIntention = keyPressed;break;
                    case "ROTATELEFT":
                        rotateLeftIntention = keyPressed;break;
                    case "ROTATERIGHT":
                        rotateRightIntention = keyPressed;break;
                    case "UP":
                        upIntention = keyPressed;break;
                    case "DOWN":
                        downIntention = keyPressed;break;
            }
        }
    };

    /**
     * Method to calculate the movement of a physic object.
     */
    public void updateMovement(){
        // Main vector
        Vector3f totalForce = new Vector3f(0f, gravityCounterForce, 0f);
        // Local direction
        Vector3f forward = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Z);
        Vector3f left = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X);
        Vector3f up = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y);
        Vector3f angularVelocity = physicsControl.getAngularVelocity(); // Used to avoid the beyblade spin
        Vector3f linearVelocity = physicsControl.getLinearVelocity(); // Used to avoid infinite acceleration
        // Forward
        if (forwardIntention) {
            totalForce.addLocal(forward.mult(thrustPower));
        }
        // Backward
        if (backwardIntention) {
            totalForce.addLocal(forward.negate().mult(thrustPower));
        }
        // Move left
        if (leftIntention) {
            totalForce.addLocal(left.mult(thrustPower));
        }
        // Move right
        if (rightIntention) {
            totalForce.addLocal(left.negate().mult(thrustPower));
        }
        // Up
        if (upIntention) {
            // Add force to push the drone upward
            totalForce.addLocal(up.negate().mult(thrustPower));
        }
        // Down
        if (downIntention) {
            // Reducing force to let the drone sink
            totalForce.addLocal(up.mult(thrustPower));
        }
        // Apply forces
        physicsControl.applyCentralForce(totalForce);
        // Left and right rotation
        if (rotateLeftIntention) {
            physicsControl.applyTorque(new Vector3f(0f, torquePower, 0f));
        } else if (rotateRightIntention) {
            physicsControl.applyTorque(new Vector3f(0f, -torquePower, 0f));
        } else {
            Vector3f currentAngularVelocity = physicsControl.getAngularVelocity();
            physicsControl.setAngularVelocity(currentAngularVelocity.mult(0.95f));
        }
        // SPPED MANAGEMENT :
        // Cap the speed to avoid infinite acceleration
        if (linearVelocity.length() > maxSpeed) {
            // On conserve la direction du déplacement, mais on force sa taille à maxLinearSpeed
            Vector3f clampedVelocity = linearVelocity.normalize().mult(maxSpeed);
            physicsControl.setLinearVelocity(clampedVelocity);
        }
        // Cap the angular speed on the Y axis (yaw)
        if (angularVelocity.y > maxRotationSpeed) {
            physicsControl.setAngularVelocity(new Vector3f(angularVelocity.x, maxRotationSpeed, angularVelocity.z));
        } else if (angularVelocity.y < -maxRotationSpeed) {
            physicsControl.setAngularVelocity(new Vector3f(angularVelocity.x, -maxRotationSpeed, angularVelocity.z));
        }
    }

    /**
     * Method to calculate the movement of an non physic object.
     * @param tpf Time per frame, requiered to work properly
     */
    public void updateMovementCinematic(float tpf) {
        Vector3f moveDirection = new Vector3f();
        /*
         * Direction avant du drone
         */
        Vector3f forward =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Z);
        // Direction verticale
        Vector3f up =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Y);
        // Avancer
        if (forwardIntention) {
            moveDirection.addLocal(
                    forward
            );
        }
        // Reculer
        if (backwardIntention) {
            moveDirection.addLocal(
                    forward.negate()
            );
        }
        // Monter
        if (upIntention) {
            moveDirection.addLocal(
                    up.negate()
            );
        }
        // Descendre
        if (downIntention) {
            moveDirection.addLocal(
                    up
            );
        }
        // Rotation gauche
        if (rotateLeftIntention) {
            drone.getDroneSpatial()
                    .rotate(
                            0f,
                            1.5f * tpf,
                            0f
                    );
        }
        // Rotation droite
        if (rotateRightIntention) {
            drone.getDroneSpatial()
                    .rotate(
                            0f,
                            -1.5f * tpf,
                            0f
                    );
        }
        // Déplacement fluide
        Vector3f nextPosition =
                drone.getDroneSpatial()
                        .getLocalTranslation()
                        .add(moveDirection.mult(droneSpeed * tpf));
        drone.getDroneSpatial()
                .setLocalTranslation(nextPosition);
    }
}

