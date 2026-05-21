package fr.app.controller;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import fr.app.model.Drone;
import fr.app.model.DroneMovement;
import fr.app.model.WorldView;
import fr.app.model.PilotCommand;
import static java.lang.Math.min;

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
    private boolean toggleHoverModeIntention = false;
    // Physic
    private RigidBodyControl physicsControl;
    private float mass;
    private float gravityCounterForce; // Force pour contrer la gravité
    // Hover mode
    private boolean isHoverModeOn = false; // Pour savoir si on est en mode hover
    private boolean wasHoverModeCalculated = false; // Pour détecter quand on appuie sur la touche
    private float hoverHeight = 0f;
    private float updateSpeedHoverMode = 0.5f; // Modification de la hauteur en mode hover

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
    DroneMovement toggleHoverMode = DroneMovement.TOGGLEHOVERMODE;

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
                toggleHoverMode.name()
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
        if (toggleHoverModeIntention) {
            // Si l'intention est vraie mais qu'elle ne l'était pas à la frame précédente
            if (!wasHoverModeCalculated) {
                isHoverModeOn = !isHoverModeOn;
                if(isHoverModeOn){
                    // Récupération de la hauteur à laquelle on va hover
                    hoverHeight = min(drone.getDroneSpatial().getLocalTranslation().getY(),waterHeight); // On cap à la hauteur de l'eau
                    System.out.println("Hover at : " + hoverHeight);
                }
                System.out.println("Hover mode is : " + isHoverModeOn);
                wasHoverModeCalculated = true; // On bloque les futurs changements tant que la touche reste enfoncée
            }
        } else {
            // Dès qu'on relâche la touche
            wasHoverModeCalculated = false;
        }
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
            // Yaw
            if (rotateLeftIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y).mult(torquePower));
            else if (rotateRightIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_Y).mult(-torquePower));
            // If the hover mode is activated we block the pitch and roll to not disturb the hovermode
            if(!isHoverModeOn){
                // Up - Add force to push the drone upward
                if (upIntention) totalForce.addLocal(up.mult(thrustPower));
                // Down - Reducing force to let the drone sink
                if (downIntention) totalForce.addLocal(up.negate().mult(thrustPower));
                // Pitch
                if (pitchForwardIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X).mult(-torquePower));
                else if (pitchBackwardIntention) totalTorque.addLocal(drone.getDroneSpatial().getLocalRotation().mult(Vector3f.UNIT_X).mult(torquePower));
                // Roll
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
        } else {
            // Make the drone float a little bit when it reaches surface
            // Pretty simple and avoids all the complicated Archimede's stuff
            totalForce.setY(gravityCounterForce * 0.7f);
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

    /**
     * Calculate the force needed to move the drone to his set height
     * @param totalForce The vector used for applying forces
     * @param droneHeight The desired height
     * @param linearVelocity The vector containing the speed to dampen the movement and avoid overshoot
     */
    private void StabiliseDroneHeight(Vector3f totalForce, float droneHeight, Vector3f linearVelocity){
        float heightError = hoverHeight - droneHeight;
        float kpHeight = 15.0f;
        float kdHeight = 5.0f;
        // On corrige l'assiette
        float verticalCorrection = (kpHeight * heightError) - (kdHeight * linearVelocity.y);
        totalForce.setY(gravityCounterForce + verticalCorrection);
    }

    /**
     * Calculate the torque requiered to set the drone to a plane attitude
     * @param totalTorque The vector used for applying torques
     * @param angularVelocity The vector containing the angular speeds to dampen the movement and avoid overshoot
     */
    private void StabiliseDroneAttitude(Vector3f totalTorque, Vector3f angularVelocity){
        // On récupère les angles (radians)
        float[] angles = new float[3];
        drone.getDroneSpatial().getLocalRotation().toAngles(angles);
        float currentPitch = angles[0]; // Rotation autour de l'axe X local
        float currentRoll  = angles[2]; // Rotation autour de l'axe Z local
        // Réglages : coefficients à ajuster selon le poids du drone
        float kp = 0.8f;  // Force du "ressort" (Proportionnel)
        float kd = 0.2f;  // Force de "l'amortisseur" (Dérivé)
        // Couple de redressement : correction = - (kp * Angle + kd * Vitesse_Angulaire)
        // On utilise l'inverse de la vitesse angulaire locale pour amortir
        // On convertit la vitesse angulaire globale en repère local pour agir précisément
        Vector3f localAngularVelocity = drone.getDroneSpatial().getLocalRotation().inverse().mult(angularVelocity);
        float pitchCorrection = -(kp * currentPitch + kd * localAngularVelocity.x);
        float rollCorrection  = -(kp * currentRoll  + kd * localAngularVelocity.z);
        // On transforme ces corrections locales en vecteur de couple global
        Vector3f localTorqueHover = new Vector3f(pitchCorrection, 0, rollCorrection).multLocal(torquePower);
        Vector3f globalTorqueHover = drone.getDroneSpatial().getLocalRotation().mult(localTorqueHover);
        // On applique au totalTorque
        totalTorque.addLocal(globalTorqueHover);
    }

    /**
     * @return The hover height, not the actual height, with can vary
     */
    public float GetHoverHeight(){
        return this.hoverHeight;
    }

    /**
     * Set the new height to hover.
     * @param newHoverHeight Should not be more than the water height for obvious reasons, the drone can't fly
     */
    public void SetHoverHeight(float newHoverHeight){
        this.hoverHeight = min(newHoverHeight,waterHeight);
    }

    /**
     * @return The speed which the hover height will be changed
     */
    public float GetUpdateSpeedHoverMode(){
        return this.updateSpeedHoverMode;
    }

    /**
     * Set the new speed to change the hover mode height.
     * @param newUpdateSpeed Should not be more than 1f, it will be too fast
     */
    public void SetUpdateSpeedHoverMode(float newUpdateSpeed){
        this.updateSpeedHoverMode = newUpdateSpeed;
    }
}

