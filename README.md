# DronSim

Simulateur de drones réaliste, multi-milieux et multi-agent.

## Objectif

Développer une application permettant de simuler des drones dans différents environnements.

## Stack technique

- Java 21
- Maven
- JMonkeyEngine
- JavaFX

## Architecture

MVC :
- Modèle
- Vue
- Contrôleur

## Workflow Git

- main : socle du projet
- develop : développement
- feature/* : fonctionnalités

### Lancement du projet

0) Intégrer les ressources nécessaires pour le fonctionnement du projet

- /src/main/resources/Common/ (dans src/main/resources pour être accessible par le code)
- /data/  (à la racine du projet)

1) Démarrage du serveur


   (windows)  
```bash 
mvn exec:java "-Dexec.mainClass=fr.app.network.MainTestServeur"
```

(linux)
```bash
mvn exec:java -Dexec.mainClass=fr.app.network.MainTestServeur
```

2) Démarrage du client
```bash
mvn javafx:run
```


3) Connexion :
- IP : localhost ou alors l'adresse donnée par le serveur (commande ipconfig)
- Port : 62500 (par défaut du serveur)


4) Choix du drone et de l'environnement
    
5) Lancement de la simulation (bouton "Lancer la Simulation")

Notes  :   
    - le multi-agent fonctionne mais à partir de 3 drones il y a des problèmes de synchronisation / optimisation, à régler dans les prochaines versions.  
    - le simulateur fonctionne bien sur le même réseau mais peut être pourrait avoir des difficultés ou des problèmes sur des réseaux différents