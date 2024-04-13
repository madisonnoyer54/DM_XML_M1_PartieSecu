# Projet BD XML sécurisées

L’objectif de ce TP est d’implanter une application java permettant à deux agents (threads) de
lire des bases de données XML distribuées (stockées sous forme de documents XML) par
l’intermédiaire de requêtes XPath échangées dans un format XML sécurisé.

## Comprendre le projet
Après avoir cloné ou téléchargé le projet, vous pourrez explorer les différents dossiers :

- 'DM_XML_M1_PartieSecu/src/XML' : Ce répertoire contient deux sous-dossiers, Paul et Sarah, chacun représentant un agent. Chaque sous-dossier contient les éléments suivants :

- BDD.xml : Ce fichier représente la base de données sous forme de documents XML.

- reponsesEnvoyer : Ce dossier est destiné à contenir les fichiers de réponses générés par les agents. Il est possible que quelques réponses manquent (pratique pour voir la génération des fichier), elles seront donc générées après l'exécution du projet.

- requetes : Ce dossier contient les requêtes que les agents peuvent exécuter. Les requêtes sont préparées, mais le code est automatisé, vous pouvez donc les modifier ou en ajouter de nouvelles selon vos besoins.

Cette structure de dossier permet de séparer les bases de données et les requêtes des agents pour une gestion plus claire et modulaire du projet.

## Configuration requise

Pour exécuter ce projet, vous aurez besoin des logiciels suivants installés sur votre système :

- Java JDK 11 (11.0.16) ou version ultérieure.

## Installation

1. Clonez ce dépôt sur votre machine locale (https://github.com/madisonnoyer54/DM_XML_M1_PartieSecu.git) ou Extraire le dossier zip donnée sur arche.
2. Assurez-vous d'avoir installé les dépendances mentionnées dans la section Configuration requise.
4. Placer vous à la racine du projet dans le dossier 'DM_XML_M1_PartieSecu'
3. Compilez le code source à l'aide de la commande suivante :
	javac -d bin src/*.java
4. Exécutez le programme à l'aide de la commande suivante :
	java -cp bin Main


## Fonctionnalités

- Une programmation à deux threads.
- Le chargement de documents XML.
- La génération de paires de clés pour une cryptographie asymétrique.
- La signature des documents XML.
- La validation des documents signés

## Structure du projet

- `src/` : Contient les fichiers sources Java.
- `bin/` : Contiendra les fichiers compilés `.class`.
- `README.md` : Ce fichier que vous lisez actuellement.

## Auteurs

- NOYER Madison

