# Workflow Git

## Branches

Le projet utilise trois types de branches :

- main : version stable du projet
- develop : branche de développement
- feature/* : branches dédiées aux fonctionnalités

## Règles

- Ne pas développer directement sur main
- Créer une branche feature pour chaque tâche
- Faire une Pull Request avant de fusionner
- Vérifier que le projet compile avant de merger

## Créer une branche de fonctionnalité

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nom-feature