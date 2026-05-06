package fr.app.controller;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;

import fr.app.model.Drone;

public class DroneController {

    private InputManager inputManager;
    private Drone drone;

    // vitesse
    private float droneSpeed = 0.2f;

    // intentions
    private Boolean forwardIntention = false;
    private Boolean backwardIntention = false;
    private Boolean leftIntention = false;
    private Boolean rightIntention = false;
    private Boolean upIntention = false;
    private Boolean downIntention = false;

    // vecteurs
    private Vector3f forwardMovement = new Vector3f(0, 0, -1);
    private Vector3f backwardMovement = new Vector3f(0, 0, 1);
    private Vector3f leftMovement = new Vector3f(-1, 0, 0);
    private Vector3f rightMovement = new Vector3f(1, 0, 0);
    private Vector3f upMovement = new Vector3f(0, 1, 0);
    private Vector3f downMovement = new Vector3f(0, -1, 0);

    // noms des inputs
    String moveForward = "Move Forward";
    String moveBackward = "Move Backward";
    String moveLeft = "Move Left";
    String moveRight = "Move Right";


    public DroneController(InputManager inputManager,
                           Drone drone) {

        this.inputManager = inputManager;
        this.drone = drone;
    }

    public void setUpKeys() {

        // touches
        inputManager.addMapping(moveForward,
                new KeyTrigger(KeyInput.KEY_I));

        inputManager.addMapping(moveBackward,
                new KeyTrigger(KeyInput.KEY_K));

        inputManager.addMapping(moveLeft,
                new KeyTrigger(KeyInput.KEY_J));

        inputManager.addMapping(moveRight,
                new KeyTrigger(KeyInput.KEY_L));


        // listeners
        inputManager.addListener(actionListener,
                moveForward,
                moveBackward,
                moveLeft,
                moveRight
               );
    }

    private final ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name,
                             boolean keyPressed,
                             float tpf) {

            switch (name) {

                case "Move Forward":
                    forwardIntention = keyPressed;
                    break;

                case "Move Backward":
                    backwardIntention = keyPressed;
                    break;

                case "Move Left":
                    leftIntention = keyPressed;
                    break;

                case "Move Right":
                    rightIntention = keyPressed;
                    break;

            }
        }
    };

    public void updateMovement() {

        Vector3f moveDirection = new Vector3f();

        if (forwardIntention) {
            moveDirection.addLocal(forwardMovement);
        }

        if (backwardIntention) {
            moveDirection.addLocal(backwardMovement);
        }

        if (leftIntention) {
            moveDirection.addLocal(leftMovement);
        }

        if (rightIntention) {
            moveDirection.addLocal(rightMovement);
        }


        Vector3f nextPosition =
                drone.getDroneSpatial()
                        .getLocalTranslation()
                        .add(moveDirection.mult(droneSpeed));

        drone.getDroneSpatial()
                .setLocalTranslation(nextPosition);
    }
}