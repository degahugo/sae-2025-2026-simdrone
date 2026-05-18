package fr.app.controller;

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

    // vitesse
    private float droneSpeed = 0.2f;


    // intentions
    private boolean forwardIntention = false;
    private boolean backwardIntention = false;
    private boolean leftIntention = false;
    private boolean rightIntention = false;
    private boolean upIntention = false;
    private boolean downIntention = false;

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
    DroneMovement moveUp = DroneMovement.UP;
    DroneMovement moveDown = DroneMovement.DOWN;




    public DroneController(InputManager inputManager,
                           Drone drone) {

        this.inputManager = inputManager;
        this.drone = drone;
    }

    public void setUpKeys() {

        // touches
        inputManager.addMapping(moveForward.name(),
                new KeyTrigger(KeyInput.KEY_K));

        inputManager.addMapping(moveBackward.name(),
                new KeyTrigger(KeyInput.KEY_I));

        inputManager.addMapping(moveLeft.name(),
                new KeyTrigger(KeyInput.KEY_J));

        inputManager.addMapping(moveRight.name(),
                new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping(moveUp.name(),
                new KeyTrigger(KeyInput.KEY_B));

        inputManager.addMapping(moveDown.name(),
                new KeyTrigger(KeyInput.KEY_N));

        // listeners
        inputManager.addListener(actionListener,
                moveForward.name(),
                moveBackward.name(),
                moveLeft.name(),
                moveRight.name(),
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
                        forwardIntention = keyPressed;
                        break;

                    case "BACKWARD":
                        backwardIntention = keyPressed;
                        break;

                    case "LEFT":
                        leftIntention = keyPressed;
                        break;

                    case "RIGHT":
                        rightIntention = keyPressed;
                        break;

                    case "UP":
                        upIntention = keyPressed;
                        break;

                    case "DOWN":
                        downIntention = keyPressed;
                        break;



            }
        }
    };

    public void updateMovement(float tpf) {

        Vector3f moveDirection = new Vector3f();

        /*
         * Direction avant du drone
         */
        Vector3f forward =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Z);

        /*
         * Direction verticale
         */
        Vector3f up =
                drone.getDroneSpatial()
                        .getLocalRotation()
                        .mult(Vector3f.UNIT_Y);

        /*
         * Avancer / reculer
         */

        if (forwardIntention) {

            moveDirection.addLocal(
                    forward.negate()
            );
        }

        if (backwardIntention) {

            moveDirection.addLocal(
                    forward
            );
        }

        /*
         * Monter / descendre
         */

        if (upIntention) {

            moveDirection.addLocal(
                    up.negate()
            );
        }

        if (downIntention) {

            moveDirection.addLocal(
                    up
            );
        }

        /*
         * Rotation gauche / droite
         */

        if (leftIntention) {

            drone.getDroneSpatial()
                    .rotate(
                            0f,
                            1.5f * tpf,
                            0f
                    );
        }

        if (rightIntention) {

            drone.getDroneSpatial()
                    .rotate(
                            0f,
                            -1.5f * tpf,
                            0f
                    );
        }

        /*
         * Déplacement fluide
         */

        Vector3f nextPosition =
                drone.getDroneSpatial()
                        .getLocalTranslation()
                        .add(moveDirection.mult(droneSpeed * drone.getState().getSpeedFactor() * tpf * 60f));
        drone.getDroneSpatial()
                .setLocalTranslation(nextPosition);
    }
}

