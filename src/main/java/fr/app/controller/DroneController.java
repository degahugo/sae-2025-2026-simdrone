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
import fr.app.model.WorldView;
import fr.app.model.PilotCommand;

public class DroneController {

    private InputManager inputManager;
    private Drone drone;
    private WorldView model;

    // Speed for cinematic mode
    private float droneSpeed = 0.2f;
    // Speed for the physic mode
    private float thrustPower = 7f;
    // Rotation speed for the physic mode
    private float torquePower = 0.1f;
    // Max speed, rotation speed dans height
    private float maxRotationSpeed = 0.5f;
    private float waterHeight;
    private float maxSpeed = 10f;
    private boolean isSubmerged;
    private float submersionHysteresis = 0.1f;

    // intentions
    private boolean forwardIntention = false;
    private boolean backwardIntention = false;
    private boolean leftIntention = false;
    private boolean rightIntention = false;
    private boolean rotateLeftIntention = false;
    private boolean rotateRightIntention = false;
    private boolean upIntention = false;
    private boolean downIntention = false;
    private boolean pitchForwardIntention = false;
    private boolean pitchBackwardIntention = false;
    private boolean rollLeftIntention = false;
    private boolean rollRightIntention = false;
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
    DroneMovement pitchUp = DroneMovement.PITCHUP;
    DroneMovement pitchDown = DroneMovement.PITCHDOWN;
    DroneMovement rollLeft = DroneMovement.ROLLLEFT;
    DroneMovement rollRight = DroneMovement.ROLLRIGHT;

    public DroneController(InputManager inputManager, Drone drone, WorldView worldView) {
        this.inputManager = inputManager;
        this.drone = drone;
        this.model = worldView;
        this.waterHeight = model.getWaterHeight();
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
     * Set up the key mappings
     */
    public void executeCommand(PilotCommand command,
                               boolean pressed) {

        switch (command) {

            case FORWARD: forwardIntention = pressed;break;
            case BACKWARD: backwardIntention = pressed;break;
            case LEFT: leftIntention = pressed;break;
            case RIGHT: rightIntention = pressed;break;
            case ROTATELEFT: rotateLeftIntention = pressed;break;
            case ROTATERIGHT: rotateRightIntention = pressed;break;
            case UP: upIntention = pressed;break;
            case DOWN: downIntention = pressed;break;
            case PITCHUP:pitchForwardIntention = pressed;break;
            case PITCHDOWN: pitchBackwardIntention = pressed; break;
            case ROLLLEFT: rollLeftIntention = pressed; break;
            case ROLLRIGHT: rollRightIntention = pressed; break;

            case STOP:
                forwardIntention = false;
                backwardIntention = false;
                leftIntention = false;
                rightIntention = false;
                upIntention = false;
                downIntention = false;
                break;
        }
    }

    public void setUpKeys() {
        // Key mappings
        // Moving horizontally
        inputManager.addMapping(moveForward.name(), new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping(moveBackward.name(), new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping(moveLeft.name(), new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping(moveRight.name(), new KeyTrigger(KeyInput.KEY_E));
        // Moving vertically
        inputManager.addMapping(moveUp.name(), new KeyTrigger(KeyInput.KEY_R));
        inputManager.addMapping(moveDown.name(), new KeyTrigger(KeyInput.KEY_F));
        // Moving on an axis
        inputManager.addMapping(rotateRight.name(), new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping(rotateLeft.name(), new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping(rollLeft.name(), new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping(rollRight.name(), new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping(pitchUp.name(), new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping(pitchDown.name(), new KeyTrigger(KeyInput.KEY_K));

        // Listeners
        inputManager.addListener(actionListener,
                moveForward.name(),
                moveBackward.name(),
                moveLeft.name(),
                moveRight.name(),
                rotateLeft.name(),
                rotateRight.name(),
                moveDown.name(),
                moveUp.name(),
                pitchUp.name(),
                pitchDown.name(),
                rollLeft.name(),
                rollRight.name()
        );
    }

    private final ActionListener actionListener = new ActionListener() {
        //une touche après l'autre
        @Override
        public void onAction(String name,
                             boolean keyPressed,
                             float tpf) {

            try {
                PilotCommand command = PilotCommand.valueOf(name);
                executeCommand(command, keyPressed);
            } catch (IllegalArgumentException e) {
                System.out.println("Commande inconnue : " + name);
            }
        }
    };



    public void applyCommandOnce(PilotCommand command) {

        float step = 0.6f;
        float rotationStep = 0.25f;

        Vector3f forward =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Z);

        Vector3f up =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Y);

        switch (command) {

            case FORWARD:
                drone.getDroneSpatial().move(forward.mult(step));
                break;

            case BACKWARD:
                drone.getDroneSpatial().move(forward.negate().mult(step));
                break;

            case LEFT:
                drone.getDroneSpatial().rotate(0f, rotationStep, 0f);
                break;

            case RIGHT:
                drone.getDroneSpatial().rotate(0f, -rotationStep, 0f);
                break;

            case UP:
                drone.getDroneSpatial().move(up.mult(step));
                break;

            case DOWN:
                drone.getDroneSpatial().move(up.negate().mult(step));
                break;

            case STOP:
                executeCommand(PilotCommand.STOP, true);
                break;
        }
    }

    /**
     * Method to calculate the movement of a physic object.
     */
    public void updateMovement(){
        // Main vector
        Vector3f totalForce = new Vector3f();
        // Local direction
        Vector3f forward = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Z);
        Vector3f left = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X);
        Vector3f up = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y);
        Vector3f angularVelocity = physicsControl.getAngularVelocity(); // Used to avoid the beyblade spin
        Vector3f linearVelocity = physicsControl.getLinearVelocity(); // Used to avoid infinite acceleration
        Vector3f totalTorque = new Vector3f();
        float droneHeight = drone.getDroneSpatial().getLocalTranslation().getY();
        if(isSubmerged) {
            if(droneHeight >= waterHeight + submersionHysteresis) isSubmerged = false;
        } else{
            if(droneHeight <= waterHeight) isSubmerged = true;
        }
        if(isSubmerged){
            totalForce.setY(gravityCounterForce);
            // Forward
            if (forwardIntention) totalForce.addLocal(forward.mult(thrustPower));
            // Backward
            if (backwardIntention) totalForce.addLocal(forward.negate().mult(thrustPower));
            // Move left
            if (leftIntention) totalForce.addLocal(left.mult(thrustPower));
            // Move right
            if (rightIntention) totalForce.addLocal(left.negate().mult(thrustPower));
            // Up - Add force to push the drone upward
            if (upIntention) totalForce.addLocal(up.mult(thrustPower));
            // Down - Reducing force to let the drone sink
            if (downIntention) totalForce.addLocal(up.negate().mult(thrustPower));
            // Yaw
            if (rotateLeftIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y).mult(torquePower));
            else if (rotateRightIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y).mult(-torquePower));
            // Pitch
            if (pitchForwardIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X).mult(-torquePower));
            else if (pitchBackwardIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X).mult(torquePower));
            // Roll
            if (rollLeftIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Z).mult(-torquePower));
            else if (rollRightIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Z).mult(torquePower));
        } else {
            if (linearVelocity.y > 0) {
                physicsControl.setLinearVelocity(new Vector3f(linearVelocity.x, linearVelocity.y * 0.9f, linearVelocity.z));
            }
        }
        // Apply forces and torques
        physicsControl.applyCentralForce(totalForce);
        physicsControl.applyTorque(totalTorque);
        // SPEED MANAGEMENT :
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
        //System.out.println("hauteur : " + droneHeight);
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

