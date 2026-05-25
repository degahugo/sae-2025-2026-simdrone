package fr.app.controller;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.model.SimulationConfiguration;
import fr.app.network.NetworkClient;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.scene.layout.StackPane;

import java.io.ByteArrayInputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DashboardController {

    private static final long POLL_INTERVAL_MS = 150;

    // ── Barre supérieure
    @FXML private Label networkLabel;
    @FXML private Label systemLabel;
    @FXML private Label stateLabel;
    @FXML private Label simTimeLabel;
    @FXML private Label clockLabel;

    // ── Colonne gauche
    @FXML private Label droneTypeLabel;
    @FXML private Label environmentLabel;
    @FXML private Label positionLabel;
    @FXML private Label speedLabel;
    @FXML private Label depthLabel;
    @FXML private Label temperatureLabel;
    @FXML private Label pressureLabel;
    @FXML private ProgressBar batteryBar;
    @FXML private Label batteryLabel;
    @FXML private Label batteryAutonomyLabel;
    @FXML private Label collisionLabel;
    @FXML private Label alertLabel;
    @FXML private TextArea logArea;
    @FXML private Label depthLeftLabel;

    // ── Centre caméra
    @FXML private ImageView cameraView;
    @FXML private Label cameraStatusLabel;
    @FXML private Label cameraHudLabel;
    @FXML private StackPane cameraPane;

    // ── Mini-map
    @FXML private Circle miniMapDroneDot;

    // ── Mission
    @FXML private Label missionLabel;
    @FXML private Label distanceLabel;
    @FXML private Label runtimeLabel;

    // ── Réseau bas
    @FXML private Label latencyLabel;
    @FXML private Label packetLossLabel;
    @FXML private Label frequencyLabel;
    @FXML private Label uptimeLabel;

    // ── Colonne droite
    @FXML private Label compassLabel;
    @FXML private Line compassNeedle;
    @FXML private Label yawLabel;
    @FXML private Label pitchLabel;
    @FXML private Label rollLabel;
    @FXML private Label sensorsLabel;
    @FXML private Label intensityLabel;

    // ── État interne
    private NetworkClient client;
    private String droneId;
    private SimulationConfiguration configuration;

    private long startTime = 0;
    private boolean simulationRunning = false;
    private PilotCommand currentCommand = PilotCommand.STOP;

    private ScheduledExecutorService pollExecutor;
    private final AtomicReference<DroneStatus> lastStatus = new AtomicReference<>(null);

    // Calcul vitesse
    private double prevX = Double.NaN, prevY = Double.NaN, prevZ = Double.NaN;
    private long prevTimeNs = 0;
    private double totalDistance = 0.0;

    // Latence
    private long lastSendTimeNs = 0;
    private long lastLatencyMs = 0;

    // Horloge
    private AnimationTimer clockTimer;
    private final DateTimeFormatter HH_MM_SS =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private Rotate compassRotate = new Rotate(0, 0, 0);

    // ── Init FXML
    @FXML
    public void initialize() {

        startClock();

        cameraView.setManaged(false);
        cameraView.fitWidthProperty().bind(cameraPane.widthProperty());
        cameraView.fitHeightProperty().bind(cameraPane.heightProperty());

        if (compassNeedle != null) {
            compassNeedle.getTransforms().clear();
            compassNeedle.getTransforms().add(compassRotate);
        }
    }

    // Setters appelés depuis SelectionDroneController

    public void setClient(NetworkClient client) {
        this.client = client;
        Platform.runLater(() ->
                setText(networkLabel, "Connecté"));
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public void setConfiguration(
            SimulationConfiguration configuration
    ) {

        this.configuration = configuration;

        if (configuration == null) {
            return;
        }

        Platform.runLater(() -> {

            if (configuration.getEnvironmentType() != null) {
                setText(
                        environmentLabel,
                        "Environnement : "
                                + configuration.getEnvironmentType()
                );
            }

            if (configuration.getDroneType() != null) {
                setText(
                        droneTypeLabel,
                        "Drone : "
                                + configuration.getDroneType()
                );
            }

            if (configuration.getSensors() != null) {
                setText(
                        sensorsLabel,
                        "Capteurs : "
                                + configuration.getSensors()
                );
            }

            setText(
                    missionLabel,
                    "Exploration / pilotage ROV"
            );

            log(
                    "Configuration chargée : "
                            + configuration.getEnvironmentType()
                            + " | "
                            + configuration.getDroneType()
            );
        });
    }

    /*
     * Appelé par NetworkClient à chaque frame JPEG reçue en UDP.
     * Peut être appelé depuis n'importe quel thread — Platform.runLater pour le thread JavaFX.
     */
    public void afficherFrameVideo(byte[] jpegBytes) {

        Platform.runLater(() -> {

            try {

                Image image = new Image(
                        new ByteArrayInputStream(jpegBytes)
                );

                cameraView.setImage(image);

            } catch (Exception e) {

                System.err.println(
                        "[Dashboard] Erreur affichage frame : "
                                + e.getMessage()
                );
            }
        });
    }

    // Lancement simulation
    @FXML
    private void startSimulation() {

        if (client == null || !client.isConnected()) {

            log("❌ Client non connecté");
            return;
        }

        try {

            if (configuration != null) {

                client.sendConfiguration(configuration);

                log("Configuration envoyée au serveur");
            }

            client.sendStartSimulation(droneId);

            DroneStatus status = client.receiveStatus();

            lastStatus.set(status);

            Platform.runLater(() ->
                    updateTelemetry(status));

            startTime = System.currentTimeMillis();

            simulationRunning = true;

            setText(
                    cameraStatusLabel,
                    "Flux caméra : en cours"
            );

            log(
                    "Simulation lancée pour "
                            + droneId
            );

            startPolling();

        } catch (Exception e) {

            log(
                    "Erreur lancement simulation : "
                            + e.getMessage()
            );
        }
    }

    // ── Polling continu

    private void startPolling() {

        if (pollExecutor != null
                && !pollExecutor.isShutdown()) {
            return;
        }

        pollExecutor =
                Executors.newSingleThreadScheduledExecutor(r -> {

                    Thread t = new Thread(r, "drone-poll");

                    t.setDaemon(true);

                    return t;
                });

        pollExecutor.scheduleAtFixedRate(
                this::pollStatus,
                POLL_INTERVAL_MS,
                POLL_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
    }

    private void pollStatus() {

        if (client == null
                || !client.isConnected()
                || !simulationRunning) {
            return;
        }

        try {

            lastSendTimeNs = System.nanoTime();

            client.sendCommand(
                    droneId,
                    currentCommand
            );

            DroneStatus status =
                    client.receiveStatus();

            lastLatencyMs =
                    (System.nanoTime()
                            - lastSendTimeNs)
                            / 1_000_000;

            injectEstimatedSpeed(status);

            lastStatus.set(status);

            Platform.runLater(() -> {

                updateTelemetry(status);

                updateNetworkStats();

                updateRuntime();
            });

        } catch (Exception e) {

            Platform.runLater(() -> {

                log(
                        "⚠ Erreur polling : "
                                + e.getMessage()
                );

                setText(
                        networkLabel,
                        "● Erreur connexion"
                );

                setStyle(
                        networkLabel,
                        "-fx-text-fill: #ef4444;"
                );
            });
        }
    }

    // Calcul vitesse estimée

    private void injectEstimatedSpeed(
            DroneStatus status
    ) {

        long nowNs = System.nanoTime();

        if (!Double.isNaN(prevX)
                && prevTimeNs > 0) {

            double dx =
                    status.getX() - prevX;

            double dy =
                    status.getY() - prevY;

            double dz =
                    status.getZ() - prevZ;

            double dist =
                    Math.sqrt(dx * dx + dy * dy + dz * dz);

            double dtSec =
                    (nowNs - prevTimeNs)
                            / 1_000_000_000.0;

            if (dtSec > 0) {

                double raw = dist / dtSec;

                // Lissage exponentiel pour éviter les pics
                status.setSpeed(
                        0.7 * status.getSpeed()
                                + 0.3 * raw
                );

                totalDistance += dist;
            }
        }

        prevX = status.getX();
        prevY = status.getY();
        prevZ = status.getZ();

        prevTimeNs = nowNs;
    }

    // Mise à jour UI
    private void updateTelemetry(
            DroneStatus status
    ) {

        if (status == null) {
            return;
        }

        // Infos drone
        if (status.getDroneType() != null) {
            setText(
                    droneTypeLabel,
                    "Drone : "
                            + status.getDroneType()
            );
        }

        if (status.getEnvironmentName() != null) {
            setText(
                    environmentLabel,
                    "Environnement : "
                            + status.getEnvironmentName()
            );
        }

        // État
        setText(
                stateLabel,
                status.getState() != null
                        ? status.getState()
                        : "En vol"
        );

        // Position
        setText(
                positionLabel,
                String.format(
                        "X: %.1f m\nY: %.1f m\nZ: %.1f m",
                        status.getX(),
                        status.getY(),
                        status.getZ()
                )
        );

        // Cinématique
        setText(
                speedLabel,
                String.format(
                        "%.2f m/s",
                        status.getSpeed()
                )
        );

        setText(
                depthLabel,
                String.format(
                        "%.1f m",
                        status.getDepth()
                )
        );

        setText(
                depthLeftLabel,
                String.format(
                        "%.1f m",
                        status.getDepth()
                )
        );

        // Capteurs
        setText(
                temperatureLabel,
                String.format(
                        "%.1f °C",
                        status.getTemperature()
                )
        );

        setText(
                pressureLabel,
                String.format(
                        "%.2f bar",
                        status.getPressure()
                )
        );

        setText(
                intensityLabel,
                String.format(
                        "%.3f",
                        status.getIntensity()
                )
        );

        // Boussole
        setText(
                compassLabel,
                String.format(
                        "%.1f°",
                        status.getHeading()
                )
        );

        compassRotate.setAngle(
                status.getHeading()
        );

        // Orientation
        setText(
                yawLabel,
                String.format(
                        "%.1f°",
                        status.getYaw()
                )
        );

        setText(
                pitchLabel,
                String.format(
                        "%.1f°",
                        status.getPitch()
                )
        );

        setText(
                rollLabel,
                String.format(
                        "%.1f°",
                        status.getRoll()
                )
        );

        // Batterie
        double bat =
                Math.max(
                        0,
                        Math.min(100, status.getBattery())
                );

        if (batteryBar != null) {
            batteryBar.setProgress(
                    bat / 100.0
            );
        }

        if (batteryLabel != null) {

            batteryLabel.setText(
                    String.format("%.0f%%", bat)
            );

            batteryLabel.setStyle(
                    bat < 20
                            ? "-fx-text-fill: #ef4444; -fx-font-size: 20px; -fx-font-weight: bold;"
                            : "-fx-text-fill: #22c55e; -fx-font-size: 20px; -fx-font-weight: bold;"
            );
        }

        if (batteryAutonomyLabel != null) {

            long totalMin =
                    (long)(bat * 0.9);

            batteryAutonomyLabel.setText(
                    String.format(
                            "Autonomie restante  %02d:%02d:00",
                            totalMin / 60,
                            totalMin % 60
                    )
            );
        }

        // Collision / alertes
        if (status.isCollisionDetected()) {

            setText(
                    collisionLabel,
                    status.getCollisionMessage()
            );

            setStyle(
                    collisionLabel,
                    "-fx-text-fill: #ef4444;"
            );

            setText(
                    alertLabel,
                    "⚠ Collision détectée"
            );

            setStyle(
                    alertLabel,
                    "-fx-text-fill: #ef4444;"
            );

            setText(
                    systemLabel,
                    "● Système : ALERTE"
            );

            setStyle(
                    systemLabel,
                    "-fx-text-fill: #ef4444;"
            );

        } else {

            setText(
                    collisionLabel,
                    "Aucune collision"
            );

            setStyle(
                    collisionLabel,
                    "-fx-text-fill: #22c55e;"
            );

            setText(
                    alertLabel,
                    "Système OK"
            );

            setStyle(
                    alertLabel,
                    "-fx-text-fill: #22c55e;"
            );

            setText(
                    systemLabel,
                    "● Système : OK"
            );

            setStyle(
                    systemLabel,
                    "-fx-text-fill: #22c55e;"
            );
        }

        // HUD caméra
        setText(
                cameraHudLabel,
                String.format(
                        "HDG %.1f°     DEPTH %.1f m     SPEED %.2f m/s",
                        status.getHeading(),
                        status.getDepth(),
                        status.getSpeed()
                )
        );

        // Mini-map
        updateMiniMap(status);

        // Distance
        setText(
                distanceLabel,
                String.format(
                        "Distance parcourue : %.1f m",
                        totalDistance
                )
        );
    }

    private void updateMiniMap(
            DroneStatus status
    ) {

        if (miniMapDroneDot == null) {
            return;
        }

        double scale = 4.0;

        miniMapDroneDot.setTranslateX(
                Math.max(
                        -90,
                        Math.min(
                                90,
                                status.getX() * scale
                        )
                )
        );

        miniMapDroneDot.setTranslateY(
                Math.max(
                        -90,
                        Math.min(
                                90,
                                status.getZ() * scale
                        )
                )
        );
    }

    private void updateNetworkStats() {

        setText(
                latencyLabel,
                lastLatencyMs + " ms"
        );

        setText(
                packetLossLabel,
                "0%"
        );

        setText(
                frequencyLabel,
                (1000 / Math.max(1, POLL_INTERVAL_MS))
                        + " Hz"
        );

        if (uptimeLabel != null
                && startTime > 0) {

            long s =
                    (System.currentTimeMillis()
                            - startTime)
                            / 1000;

            uptimeLabel.setText(
                    String.format(
                            "%02d:%02d:%02d",
                            s / 3600,
                            (s % 3600) / 60,
                            s % 60
                    )
            );
        }
    }

    private void updateRuntime() {

        if (startTime <= 0) {
            return;
        }

        long s =
                (System.currentTimeMillis()
                        - startTime)
                        / 1000;

        String fmt =
                String.format(
                        "%02d:%02d:%02d",
                        s / 3600,
                        (s % 3600) / 60,
                        s % 60
                );

        setText(
                runtimeLabel,
                "Temps : " + fmt
        );

        setText(
                simTimeLabel,
                fmt
        );
    }

    // Horloge
    private void startClock() {

        if (clockLabel == null) {
            return;
        }

        clockTimer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                clockLabel.setText(
                        LocalTime.now()
                                .format(HH_MM_SS)
                );
            }
        };

        clockTimer.start();
    }

    // Commandes manuelles
    private void send(PilotCommand cmd) {

        currentCommand = cmd;

        log("▶ " + cmd);

        if (client == null
                || !client.isConnected()) {

            log("❌ Non connecté");
            return;
        }

        try {

            lastSendTimeNs = System.nanoTime();

            client.sendCommand(
                    droneId,
                    cmd
            );

            DroneStatus status =
                    client.receiveStatus();

            lastLatencyMs =
                    (System.nanoTime()
                            - lastSendTimeNs)
                            / 1_000_000;

            injectEstimatedSpeed(status);

            lastStatus.set(status);

            Platform.runLater(() -> {

                updateTelemetry(status);

                updateNetworkStats();
            });

        } catch (Exception e) {

            log(
                    "Erreur envoi : "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void moveForward() {
        send(PilotCommand.FORWARD);
    }

    @FXML
    private void moveBackward() {
        send(PilotCommand.BACKWARD);
    }

    @FXML
    private void moveLeft() {
        send(PilotCommand.LEFT);
    }

    @FXML
    private void moveRight() {
        send(PilotCommand.RIGHT);
    }

    @FXML
    private void moveUp() {
        send(PilotCommand.UP);
    }

    @FXML
    private void moveDown() {
        send(PilotCommand.DOWN);
    }

    @FXML
    private void stopDrone() {
        send(PilotCommand.STOP);
    }

    public void shutdown() {

        simulationRunning = false;

        if (pollExecutor != null) {
            pollExecutor.shutdownNow();
        }

        if (clockTimer != null) {
            clockTimer.stop();
        }

        if (client != null) {

            try {
                client.disconnect();

            } catch (Exception ignored) {
            }
        }
    }

    // Helpers

    private void setText(
            Label l,
            String t
    ) {

        if (l != null) {
            l.setText(t);
        }
    }

    private void setStyle(
            Label l,
            String s
    ) {

        if (l != null) {
            l.setStyle(s);
        }
    }

    private void log(String msg) {

        if (logArea == null) {
            return;
        }

        String t =
                LocalTime.now()
                        .format(HH_MM_SS);

        logArea.appendText(
                "[" + t + "] "
                        + msg
                        + "\n"
        );
    }
}