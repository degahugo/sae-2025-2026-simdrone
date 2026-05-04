# Architecture technique

## Architecture globale

Le projet DronSim suit une architecture modulaire inspirée du modèle MVC (Modèle - Vue - Contrôleur).

L’application sera composée de deux parties principales :

- une application de simulation 3D basée sur JMonkeyEngine
- une application d’interface de pilotage basée sur JavaFX

Ces deux applications communiqueront via le réseau.

## Modèle

Contient les données métier communes :

- Drone
- Mission
- Sensor
- Environment
- TelemetryData
- PilotCommand

## Vue
L’interface utilisateur sera réalisée avec JavaFX.

Elle permettra :

- le pilotage du drone
- l’affichage des données de télémétrie
- l’affichage des commandes
- l’affichage éventuel du retour caméra

## Contrôleur

Gère :

- les actions utilisateur
- les commandes envoyées au drone
- la communication entre l’interface JavaFX et le simulateur
- la mise à jour des données affichées

## Simulation

Le module simulation utilisera JMonkeyEngine.

Il aura pour rôle :

- la gestion de la scène 3D
- le déplacement des drones
- la gestion de l’environnement
- la simulation des capteurs
- l’évolution des drones dans le monde simulé

## Réseau

Le module network gérera la communication entre :

- l’interface de pilotage JavaFX
- le simulateur 3D JMonkeyEngine

Il permettra d’envoyer les commandes du pilote et de recevoir les données du drone.

## Organisation des packages

## Organisation des packages

- fr.dronsim.simulation : application de simulation 3D JMonkeyEngine
- fr.dronsim.pilotui : application d’interface de pilotage JavaFX
- fr.dronsim.model : classes communes
- fr.dronsim.controller : logique de contrôle
- fr.dronsim.network : communication entre les applications
- fr.dronsim.utils : outils communs