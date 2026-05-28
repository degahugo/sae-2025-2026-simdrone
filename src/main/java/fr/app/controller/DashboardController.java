package fr.app.controller;

import fr.app.model.DroneStatus;
import fr.app.model.PilotCommand;
import fr.app.model.SimulationConfiguration;
import fr.app.network.NetworkClient;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.ByteArrayInputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DashboardController {

    private static final long POLL_INTERVAL_MS = 150;

    @FXML private Circle radarPoint1;
    @FXML private Circle radarPoint2;
    @FXML private Circle radarPoint3;
    @FXML private Circle radarPoint4;
    @FXML private Circle radarPoint5;
    private double radarScale = 4.0;
    private double radarRangeMeters = 100.0;
    @FXML private Label radarRangeLabel;
    @FXML private ImageView cameraPreviewView;

    // ── Barre supérieure
    @FXML private Label networkLabel;
    @FXML private Label systemLabel;
    @FXML private Circle systemStatusDot;
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
    @FXML private VBox logBox;
    private static final int MAX_LOG_LINES = 50;
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
    @FXML private Label yawLabel;
    @FXML private Label pitchLabel;
    @FXML private Label rollLabel;
    @FXML private Label sensorsLabel;
    @FXML private Label intensityLabel;

    @FXML private Canvas compassCanvas;
    @FXML private Canvas yawCanvas;
    @FXML private Canvas pitchCanvas;
    @FXML private Canvas rollCanvas;


    // ── État interne
    private NetworkClient client;
    private String droneId;
    private SimulationConfiguration configuration;

    private long startTime = 0;
    private boolean simulationRunning = false;
    private boolean stabilizedMode = false;
    private boolean holdPositionMode = false;
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


    // ── Init FXML
    @FXML
    public void initialize() {

        startClock();

        // Dessin initial à zéro
        Platform.runLater(() -> {
            drawCompass(0);
            drawGauge(yawCanvas,   0, -180, 180);
            drawGauge(pitchCanvas, 0,  -90,  90);
            drawGauge(rollCanvas,  0,  -90,  90);
        });

        cameraView.setManaged(false);
        cameraView.fitWidthProperty().bind(cameraPane.widthProperty());
        cameraView.fitHeightProperty().bind(cameraPane.heightProperty());

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
                cameraPreviewView.setImage(image);

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
        drawCompass(status.getHeading());
        setText(compassLabel, String.format("%.1f°", status.getHeading()));

// Orientation
        drawGauge(yawCanvas,   status.getYaw(),   -180, 180);
        drawGauge(pitchCanvas, status.getPitch(),  -90,  90);
        drawGauge(rollCanvas,  status.getRoll(),   -90,  90);
        setText(yawLabel,   String.format("%.1f°", status.getYaw()));
        setText(pitchLabel, String.format("%.1f°", status.getPitch()));
        setText(rollLabel,  String.format("%.1f°", status.getRoll()));

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
        boolean collision = status.isCollisionDetected();
        boolean courantFort = status.getIntensity() > 0.08;

        if (collision) {
            setText(collisionLabel, "Collision détectée");
            setStyle(collisionLabel, "-fx-text-fill: #ef4444;");
        } else {
            setText(collisionLabel, "Aucune collision");
            setStyle(collisionLabel, "-fx-text-fill: #22c55e;");
        }

        if (courantFort) {
            setText(alertLabel, "⚠ Courant fort détecté");
            setStyle(alertLabel, "-fx-text-fill: #f59e0b;");
        } else {
            setText(alertLabel, "Système OK");
            setStyle(alertLabel, "-fx-text-fill: #22c55e;");
        }

        if (collision) {
            setText(systemLabel, "Système : ALERTE");
            setStyle(systemLabel, "-fx-text-fill: #ef4444;");
            if (systemStatusDot != null) systemStatusDot.setFill(Color.web("#ef4444"));
        } else if (courantFort) {
            setText(systemLabel, "Système : ATTENTION");
            setStyle(systemLabel, "-fx-text-fill: #f59e0b;");
            if (systemStatusDot != null) systemStatusDot.setFill(Color.web("#f59e0b"));
        } else {
            setText(systemLabel, "Système : OK");
            setStyle(systemLabel, "-fx-text-fill: #22c55e;");
            if (systemStatusDot != null) systemStatusDot.setFill(Color.web("#22c55e"));
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

    private void updateMiniMap(DroneStatus status) {

        if (miniMapDroneDot == null) {
            return;
        }

        double scale = radarScale;

        double droneMapX = Math.max(-90, Math.min(90, status.getX() * scale));
        double droneMapY = Math.max(-90, Math.min(90, status.getZ() * scale));

        miniMapDroneDot.setTranslateX(droneMapX);
        miniMapDroneDot.setTranslateY(droneMapY);

        boolean danger = status.isCollisionDetected() || status.getIntensity() > 0.08;

        Circle[] points = {
                radarPoint1,
                radarPoint2,
                radarPoint3,
                radarPoint4,
                radarPoint5
        };

        for (Circle point : points) {
            if (point != null) {
                point.setVisible(false);
            }
        }


        if (status.isCollisionDetected() && radarPoint1 != null) {
            radarPoint1.setVisible(true);
            radarPoint1.setFill(javafx.scene.paint.Color.web("#ef4444")); // rouge collision
            radarPoint1.setTranslateX(droneMapX + 15);
            radarPoint1.setTranslateY(droneMapY - 15);
        }

        if (status.getIntensity() > 0.08 && radarPoint2 != null) {
            radarPoint2.setVisible(true);
            radarPoint2.setFill(javafx.scene.paint.Color.web("#f59e0b")); // orange courant
            radarPoint2.setTranslateX(droneMapX - 18);
            radarPoint2.setTranslateY(droneMapY + 12);
        }
    }

    @FXML
    private void zoomRadarIn() {

        radarScale = Math.min(10.0, radarScale + 1.0);

        radarRangeMeters = Math.max(25.0, radarRangeMeters - 15.0);

        if (radarRangeLabel != null) {
            radarRangeLabel.setText(
                    String.format("PORTÉE %.0f m", radarRangeMeters)
            );
        }

        log("Zoom radar +");
    }

    @FXML
    private void zoomRadarOut() {

        radarScale = Math.max(1.0, radarScale - 1.0);

        radarRangeMeters = Math.min(200.0, radarRangeMeters + 15.0);

        if (radarRangeLabel != null) {
            radarRangeLabel.setText(
                    String.format("PORTÉE %.0f m", radarRangeMeters)
            );
        }

        log("Zoom radar -");
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

    @FXML
    private void rotateLeft() {
        send(PilotCommand.ROTATELEFT);
    }

    @FXML
    private void rotateRight() {
        send(PilotCommand.ROTATERIGHT);
    }

    @FXML
    private void pitchUp() {
        send(PilotCommand.PITCHUP);
    }

    @FXML
    private void pitchDown() {
        send(PilotCommand.PITCHDOWN);
    }

    @FXML
    private void rollLeft() {
        send(PilotCommand.ROLLLEFT);
    }

    @FXML
    private void rollRight() {
        send(PilotCommand.ROLLRIGHT);
    }


    @FXML
    private void enableStabilizedMode() {
        send(PilotCommand.TOGGLEHOVERMODE);
        log("Mode stabilisé demandé");
        setText(stateLabel, "Mode stabilisé");
    }

    @FXML
    private void enableHoldPosition() {
        holdPositionMode = !holdPositionMode;
        stabilizedMode = false;

        if (holdPositionMode) {
            currentCommand = PilotCommand.STOP;
            log("Maintien position activé");
            setText(stateLabel, "Maintien position");
        } else {
            log("Maintien position désactivé");
            setText(stateLabel, "En vol");
        }
    }

    @FXML
    private void emergencyStop() {
        simulationRunning = false;
        currentCommand = PilotCommand.STOP;

        if (pollExecutor != null) {
            pollExecutor.shutdownNow();
        }

        try {
            if (client != null && client.isConnected()) {
                client.sendCommand(droneId, PilotCommand.STOP);
            }
        } catch (Exception e) {
            log("Erreur arrêt urgence : " + e.getMessage());
        }

        setText(stateLabel, "ARRÊT D'URGENCE");
        setText(systemLabel, "Système : ARRÊT");
        setStyle(systemLabel, "-fx-text-fill: #ef4444;");
        setText(cameraStatusLabel, "Flux caméra : arrêté");
        log("ARRÊT D'URGENCE déclenché");
    }


    @FXML
    private void stopSimulation() {

        simulationRunning = false;
        currentCommand = PilotCommand.STOP;

        if (pollExecutor != null) {
            pollExecutor.shutdownNow();
        }

        try {

            if (client != null && client.isConnected()) {
                client.sendCommand(droneId, PilotCommand.STOP);
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ConfigurationSimulation.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) cameraView.getScene().getWindow();

            stage.setScene(scene);
            stage.setMaximized(true);

        } catch (Exception e) {

            log("Erreur arrêt simulation : " + e.getMessage());

            e.printStackTrace();
        }
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
        if (logBox == null) return;

        String t = LocalTime.now().format(HH_MM_SS);

        String color;
        String prefix;
        if (msg.contains("❌") || msg.contains("collision") || msg.contains("urgence") || msg.contains("ARRÊT")) {
            color = "#ef4444"; prefix = "[ALERTE]";
        } else if (msg.contains("⚠") || msg.contains("Courant") || msg.contains("ATTENTION") || msg.contains("Erreur")) {
            color = "#f59e0b"; prefix = "[WARN] ";
        } else if (msg.contains("OK") || msg.contains("lancée") || msg.contains("chargée") || msg.contains("branché")) {
            color = "#22c55e"; prefix = "[INFO] ";
        } else {
            color = "#94a3b8"; prefix = "[LOG]  ";
        }

        final String finalColor = color;
        final String finalPrefix = prefix;

        Platform.runLater(() -> {
            HBox line = new HBox(6);

            Label timeLabel = new Label(t);
            timeLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 10px; -fx-font-family: monospace;");
            timeLabel.setMinWidth(52);

            Label prefixLabel = new Label(finalPrefix);
            prefixLabel.setStyle("-fx-text-fill: " + finalColor + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-font-family: monospace;");
            prefixLabel.setMinWidth(56);

            Label msgLabel = new Label(msg);
            msgLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 10px; -fx-font-family: monospace;");
            msgLabel.setWrapText(true);

            line.getChildren().addAll(timeLabel, prefixLabel, msgLabel);
            logBox.getChildren().add(0, line);

            if (logBox.getChildren().size() > MAX_LOG_LINES) {
                logBox.getChildren().remove(MAX_LOG_LINES, logBox.getChildren().size());
            }
        });
    }

    public void installKeyboardControls(Scene scene) {

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W -> send(PilotCommand.FORWARD);
                case S -> send(PilotCommand.BACKWARD);
                case Q -> send(PilotCommand.LEFT);
                case E -> send(PilotCommand.RIGHT);
                case R -> send(PilotCommand.UP);
                case F -> send(PilotCommand.DOWN);

                case A -> send(PilotCommand.ROTATELEFT);
                case D -> send(PilotCommand.ROTATERIGHT);

                case I -> send(PilotCommand.PITCHUP);
                case K -> send(PilotCommand.PITCHDOWN);

                case J -> send(PilotCommand.ROLLLEFT);
                case L -> send(PilotCommand.ROLLRIGHT);

                case T -> send(PilotCommand.TOGGLEHOVERMODE);
            }
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case W, S, Q, E, R, F, A, D, I, K, J, L, T -> stopDrone();
            }
        });
    }



    // Appelle cette méthode à chaque mise à jour du cap (heading en degrés)
    private void drawCompass(double headingDeg) {
        GraphicsContext gc = compassCanvas.getGraphicsContext2D();
        double w = compassCanvas.getWidth();
        double h = compassCanvas.getHeight();
        double cx = w / 2, cy = h / 2, r = w / 2 - 4;

        gc.clearRect(0, 0, w, h);

        // Fond
        gc.setFill(Color.web("#07111b"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
        gc.setStroke(Color.web("#1e3a5f"));
        gc.setLineWidth(2.5);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

        // Graduation
        for (int i = 0; i < 72; i++) {
            double angle = Math.toRadians(i * 5 - 90);
            boolean major = (i % 9 == 0);
            boolean mid = (i % 3 == 0);
            double r1 = r - 2;
            double r2 = major ? r - 16 : mid ? r - 11 : r - 7;
            gc.setStroke(major ? Color.web("#38bdf8") : Color.web("#1e3a5f"));
            gc.setLineWidth(major ? 2 : 1);
            gc.strokeLine(cx + r1 * Math.cos(angle), cy + r1 * Math.sin(angle),
                    cx + r2 * Math.cos(angle), cy + r2 * Math.sin(angle));
        }

        // Rose tournante — SEULEMENT l'aiguille
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(headingDeg);

// Aiguille rouge (nord)
        double nr = r - 28;
        double[] xRed = {0, -5, 0, 5};
        double[] yRed = {-nr, 0, 8, 0};
        gc.setFill(Color.web("#ef4444"));
        gc.fillPolygon(xRed, yRed, 4);
// Blanc vers le bas
        double[] xWht = {0, -5, 0, 5};
        double[] yWht = {nr, 0, 8, 0};
        gc.setFill(Color.web("#94a3b8"));
        gc.fillPolygon(xWht, yWht, 4);

        gc.restore();
// Lettres fixes
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 13));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("N", cx, cy - (r - 20));
        gc.fillText("S", cx, cy + (r - 8));
        gc.fillText("E", cx + (r - 10), cy + 5);
        gc.fillText("W", cx - (r - 10), cy + 5);

        gc.setFont(javafx.scene.text.Font.font("System", 11));
        gc.setFill(Color.web("#64748b"));
        double ic = (r - 22) * Math.sin(Math.toRadians(45));
        gc.fillText("NE", cx + ic, cy - ic + 5);
        gc.fillText("SE", cx + ic, cy + ic + 5);
        gc.fillText("SW", cx - ic, cy + ic + 5);
        gc.fillText("NW", cx - ic, cy - ic + 5);

      // Centre
        gc.setFill(Color.web("#e2e8f0"));
        gc.fillOval(cx - 7, cy - 7, 14, 14);
        gc.setStroke(Color.web("#64748b"));
        gc.setLineWidth(1.2);
        gc.strokeOval(cx - 9, cy - 9, 18, 18);

    }

    // Appelle cette méthode à chaque mise à jour des angles
    private void drawGauge(Canvas canvas, double value, double minVal, double maxVal) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double cx = 30, cy = 30, r = 23;
        gc.clearRect(0, 0, 60, 60);

        double startAngle = 225;   // degrés JavaFX (sens antihoraire)
        double arcExtent  = -270;  // 270° au total

        // Arc de fond
        gc.setStroke(Color.web("#0f2744"));
        gc.setLineWidth(6);
        gc.strokeArc(cx - r, cy - r, r * 2, r * 2, startAngle, arcExtent, javafx.scene.shape.ArcType.OPEN);

        // Arc coloré arc-en-ciel (40 segments)
        int segments = 40;
        double segArc = arcExtent / segments;
        for (int i = 0; i < segments; i++) {
            double t = (double) i / segments;
            double hue = 200 + t * 160; // bleu -> vert -> jaune -> rouge
            gc.setStroke(Color.hsb(hue, 0.9, 0.85));
            gc.setLineWidth(4.5);
            gc.strokeArc(cx - r, cy - r, r * 2, r * 2,
                    startAngle + i * segArc, segArc * 0.85, javafx.scene.shape.ArcType.OPEN);
        }

        // Aiguille
        double norm = (value - minVal) / (maxVal - minVal);
        double needleAngleDeg = 225 - (norm * 270);
        double needleRad = Math.toRadians(needleAngleDeg);
        double nx = cx + (r - 8) * Math.cos(needleRad);
        double ny = cy - (r - 8) * Math.sin(needleRad); //
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.5);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        gc.strokeLine(cx, cy, nx, ny);

        // Centre
        gc.setFill(Color.web("#94a3b8"));
        gc.fillOval(cx - 3.5, cy - 3.5, 7, 7);
    }
}