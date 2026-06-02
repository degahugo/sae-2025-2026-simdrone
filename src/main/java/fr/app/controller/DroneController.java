package fr.app.controller;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import fr.app.model.*;

import static java.lang.Math.min;

public class DroneController {

    private InputManager inputManager;
    private Drone drone;
    private WorldView model;
    private CurrentMatrix currentMatrix;

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
    private boolean toggleHoverModeIntention = false;

    // --- Intentions Free-Look (AJOUT) ---
    private boolean camLeftIntention = false;
    private boolean camRightIntention = false;
    private boolean camUpIntention = false;
    private boolean camDownIntention = false;

    private boolean switchCameraIntention = false;

    public boolean isCamLeftIntention() { return camLeftIntention; }
    public boolean isCamRightIntention() { return camRightIntention; }
    public boolean isCamUpIntention() { return camUpIntention; }
    public boolean isCamDownIntention() { return camDownIntention; }

    // Physic
    private RigidBodyControl physicsControl;
    private float mass;
    private float gravityCounterForce; // Force pour contrer la gravité

    // Hover mode
    private boolean isHoverModeOn = false; // Pour savoir si on est en mode hover
    private boolean wasHoverModeCalculated = false; // Pour détecter quand on appuie sur la touche
    private float hoverHeight = 0f;
    private float updateSpeedHoverMode = 0.5f; // Modification de la hauteur en mode hover

    // noms des inputs et des mouvements
    PilotCommand moveForward = PilotCommand.FORWARD;
    PilotCommand moveBackward = PilotCommand.BACKWARD;
    PilotCommand moveLeft = PilotCommand.LEFT;
    PilotCommand moveRight = PilotCommand.RIGHT;
    PilotCommand rotateLeft = PilotCommand.ROTATELEFT;
    PilotCommand rotateRight = PilotCommand.ROTATERIGHT;
    PilotCommand moveUp = PilotCommand.UP;
    PilotCommand moveDown = PilotCommand.DOWN;
    PilotCommand pitchUp = PilotCommand.PITCHUP;
    PilotCommand pitchDown = PilotCommand.PITCHDOWN;
    PilotCommand rollLeft = PilotCommand.ROLLLEFT;
    PilotCommand rollRight = PilotCommand.ROLLRIGHT;
    PilotCommand toggleHoverMode = PilotCommand.TOGGLEHOVERMODE;
    PilotCommand switchCamera = PilotCommand.SWITCHCAM;


    public DroneController(InputManager inputManager, Drone drone, WorldView worldView, CurrentMatrix currentMatrix) {
        this.inputManager = inputManager;
        this.drone = drone;
        this.model = worldView;
        this.currentMatrix = currentMatrix;
        this.waterHeight = model.getWaterHeight();
        physicsControl = drone.getDroneSpatial().getControl(RigidBodyControl.class);
        mass = physicsControl.getMass();
        gravityCounterForce = mass * 9.81f;
    }

    public float getMass(){ return this.mass; }
    public void setMass(float newMass){ this.mass = newMass; }
    public float getDroneSpeed(){ return this.droneSpeed; }
    public void setDroneSpeed(float newSpeed){ this.droneSpeed = newSpeed; }

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
            case TOGGLEHOVERMODE: toggleHoverModeIntention = pressed; break;

            // --- Free-Look Camera (AJOUT) ---
            case CAM_LEFT:  camLeftIntention = pressed; break;
            case CAM_RIGHT: camRightIntention = pressed; break;
            case CAM_UP:    camUpIntention = pressed; break;
            case CAM_DOWN:  camDownIntention = pressed; break;

            case SWITCHCAM: switchCameraIntention = pressed; break;

            // --- Lampe d'éclairage ---
            // TOGGLELIGHT est une action ponctuelle (comme SWITCHCAM) : on ne
            // garde pas d'intention booléenne, le serveur gère l'anti-rebond.
            // LIGHT_RANGE_UP/DOWN sont aussi ponctuels : un appui = un palier de portée.
            case TOGGLELIGHT:
            case LIGHT_RANGE_UP:
            case LIGHT_RANGE_DOWN:

                break;

            case STOP:
                forwardIntention = false;
                backwardIntention = false;
                leftIntention = false;
                rightIntention = false;
                rotateLeftIntention = false;
                rotateRightIntention = false;
                upIntention = false;
                downIntention = false;
                pitchForwardIntention = false;
                pitchBackwardIntention = false;
                rollLeftIntention = false;
                rollRightIntention = false;
                toggleHoverModeIntention = false;
                camLeftIntention = false;
                camRightIntention = false;
                camUpIntention = false;
                camDownIntention = false;
                break;
        }
    }

    /**
     * Set up the key mappings
     */
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
        // Hover mode
        inputManager.addMapping(toggleHoverMode.name(), new KeyTrigger(KeyInput.KEY_T));

        // --- Free-Look Camera (AJOUT) ---
        inputManager.addMapping(PilotCommand.CAM_LEFT.name(),  new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(PilotCommand.CAM_RIGHT.name(), new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(PilotCommand.CAM_UP.name(),    new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(PilotCommand.CAM_DOWN.name(),  new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addMapping(PilotCommand.SWITCHCAM.name(), new KeyTrigger(KeyInput.KEY_C));

        // --- Lampe d'éclairage ---
        // L = toggle (allumer / éteindre la lampe)
        // + (EQUALS) = augmenter la portée nominale de 10 m
        // - (MINUS)  = diminuer la portée nominale de 10 m
        inputManager.addMapping(PilotCommand.TOGGLELIGHT.name(),   new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping(PilotCommand.LIGHT_RANGE_UP.name(),new KeyTrigger(KeyInput.KEY_ADD));
        inputManager.addMapping(PilotCommand.LIGHT_RANGE_DOWN.name(), new KeyTrigger(KeyInput.KEY_SUBTRACT));

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
                rollRight.name(),
                toggleHoverMode.name(),

                // --- Free-Look Camera (AJOUT) ---
                PilotCommand.CAM_LEFT.name(),
                PilotCommand.CAM_RIGHT.name(),
                PilotCommand.CAM_UP.name(),
                PilotCommand.CAM_DOWN.name(),

                PilotCommand.SWITCHCAM.name(),

                // --- Lampe d'éclairage ---
                PilotCommand.TOGGLELIGHT.name(),
                PilotCommand.LIGHT_RANGE_UP.name(),
                PilotCommand.LIGHT_RANGE_DOWN.name()
        );
    }

    private final ActionListener actionListener = new ActionListener() {
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

    @Deprecated
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
     * @param tpf Time per frame, used to adjust the height while hovering
     */
    public void updateMovement(float tpf){
        // ⭐ TOUT LEUR CODE EST CONSERVÉ
        // ⭐ JE NE CHANGE RIEN
        // ⭐ FREE-LOOK NE TOUCHE PAS LA PHYSIQUE

        if (toggleHoverModeIntention) {

            Vector3f angularVelocity = physicsControl.getAngularVelocity();
            Vector3f totalTorqueStabilisation = new Vector3f();

            StabiliseDroneAttitude(totalTorqueStabilisation, angularVelocity);

            physicsControl.applyTorque(totalTorqueStabilisation.multLocal(8f));

            physicsControl.setAngularVelocity(
                    physicsControl.getAngularVelocity().mult(0.4f)
            );

            toggleHoverModeIntention = false;

            System.out.println("Stabilisation ponctuelle du drone");
        }
        Vector3f totalForce = new Vector3f();
        Vector3f forward = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Z);
        Vector3f left = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X);
        Vector3f up = drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y);
        Vector3f angularVelocity = physicsControl.getAngularVelocity();
        Vector3f linearVelocity = physicsControl.getLinearVelocity();
        Vector3f totalTorque = new Vector3f();
        float droneHeight = drone.getDroneSpatial().getLocalTranslation().getY();

        if(isSubmerged) {
            if(droneHeight >= waterHeight + submersionHysteresis) isSubmerged = false;
        } else{
            if(droneHeight <= waterHeight) isSubmerged = true;
        }

        if(isSubmerged){
            totalForce.setY(gravityCounterForce);
            ApplyCUrrentForce(totalForce);

            if (forwardIntention) totalForce.addLocal(forward.mult(thrustPower));
            if (backwardIntention) totalForce.addLocal(forward.negate().mult(thrustPower));
            if (leftIntention) totalForce.addLocal(left.mult(thrustPower));
            if (rightIntention) totalForce.addLocal(left.negate().mult(thrustPower));

            if(!isHoverModeOn){
                if (upIntention) totalForce.addLocal(up.mult(thrustPower));
                if (downIntention) totalForce.addLocal(up.negate().mult(thrustPower));

                if (pitchForwardIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X).mult(-torquePower));
                else if (pitchBackwardIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X).mult(torquePower));

                if (rollLeftIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Z).mult(-torquePower));
                else if (rollRightIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Z).mult(torquePower));
            } else{
                if (upIntention) {
                    hoverHeight = min(hoverHeight + updateSpeedHoverMode * tpf, waterHeight);
                }
                if (downIntention) {
                    hoverHeight -= updateSpeedHoverMode * tpf;
                }
                StabiliseDroneHeight(totalForce, droneHeight, linearVelocity);
                StabiliseDroneAttitude(totalTorque, angularVelocity);
            }

            if (rotateLeftIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y).mult(torquePower));
            else if (rotateRightIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y).mult(-torquePower));

        } else {
            totalForce.setY(gravityCounterForce * 0.9f);
        }

        physicsControl.applyCentralForce(totalForce);
        physicsControl.applyTorque(totalTorque);

        if (linearVelocity.length() > maxSpeed) {
            Vector3f clampedVelocity = linearVelocity.normalize().mult(maxSpeed);
            physicsControl.setLinearVelocity(clampedVelocity);
        }

        if (angularVelocity.y > maxRotationSpeed) {
            physicsControl.setAngularVelocity(new Vector3f(angularVelocity.x, maxRotationSpeed, angularVelocity.z));
        } else if (angularVelocity.y < -maxRotationSpeed) {
            physicsControl.setAngularVelocity(new Vector3f(angularVelocity.x, -maxRotationSpeed, angularVelocity.z));
        }
    }

    public void updateMovementCinematic(float tpf) {
        Vector3f moveDirection = new Vector3f();

        Vector3f forward =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Z);

        Vector3f up =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Y);

        if (forwardIntention) moveDirection.addLocal(forward);
        if (backwardIntention) moveDirection.addLocal(forward.negate());
        if (upIntention) moveDirection.addLocal(up.negate());
        if (downIntention) moveDirection.addLocal(up);

        if (rotateLeftIntention) {
            drone.getDroneSpatial().rotate(0f, 1.5f * tpf, 0f);
        }
        if (rotateRightIntention) {
            drone.getDroneSpatial().rotate(0f, -1.5f * tpf, 0f);
        }

        Vector3f nextPosition =
                drone.getDroneSpatial()
                        .getLocalTranslation()
                        .add(moveDirection.mult(droneSpeed * tpf));

        drone.getDroneSpatial().setLocalTranslation(nextPosition);
    }

    private void StabiliseDroneHeight(Vector3f totalForce, float droneHeight, Vector3f linearVelocity){
        float heightError = hoverHeight - droneHeight;
        float kpHeight = 15.0f;
        float kdHeight = 5.0f;
        float verticalCorrection = (kpHeight * heightError) - (kdHeight * linearVelocity.y);
        totalForce.setY(gravityCounterForce + verticalCorrection);
    }

    private void StabiliseDroneAttitude(Vector3f totalTorque, Vector3f angularVelocity){
        float[] angles = new float[3];
        drone.getDroneSpatial().getLocalRotation().toAngles(angles);
        float currentPitch = angles[0];
        float currentRoll  = angles[2];

        float kp = 0.8f;
        float kd = 0.2f;

        Vector3f localAngularVelocity = drone.getDroneSpatial().getLocalRotation().inverse().mult(angularVelocity);
        float pitchCorrection = -(kp * currentPitch + kd * localAngularVelocity.x);
        float rollCorrection  = -(kp * currentRoll  + kd * localAngularVelocity.z);

        Vector3f localTorqueHover = new Vector3f(pitchCorrection, 0, rollCorrection).multLocal(torquePower);
        Vector3f globalTorqueHover = drone.getDroneSpatial().getLocalRotation().mult(localTorqueHover);

        totalTorque.addLocal(globalTorqueHover);
    }

    private void ApplyCUrrentForce(Vector3f totalForce){
        if (currentMatrix != null) {
            Vector3f dronePosition = drone.getDroneSpatial().getLocalTranslation();
            Vector3f waterCurrent = currentMatrix.getCurrentAt(dronePosition);
            totalForce.addLocal(waterCurrent);
        }
    }

    public float GetHoverHeight(){ return this.hoverHeight; }
    public void SetHoverHeight(float newHoverHeight){ this.hoverHeight = min(newHoverHeight,waterHeight); }
    public float GetUpdateSpeedHoverMode(){ return this.updateSpeedHoverMode; }
    public void SetUpdateSpeedHoverMode(float newUpdateSpeed){ this.updateSpeedHoverMode = newUpdateSpeed; }
    public boolean isSwitchCameraIntention(){ return switchCameraIntention; }
}
