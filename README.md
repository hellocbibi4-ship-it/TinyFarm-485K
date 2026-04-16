# TinyFarm Project - L2 Informatique

Bienvenue sur le depot **TinyFarm**.
Ce projet est realise dans le cadre du cours **XLG4IU050 - Developpement web cote client**
a l'Universite de Nantes.

## A propos du projet

**TinyFarm** est un jeu de gestion de ferme en ligne.
Le joueur commence avec **1 500 ecus** et developpe sa ferme en elevant
des vaches, des poules et des lapins, puis en produisant et vendant des ressources.

Le depot contient principalement :

- une application **Spring Boot** dans `project/`
- un front integre servi par Spring Boot
- une connexion **GitHub OAuth**
- des tests Java et Python

## Etat actuel

Le projet est actuellement dans un etat **fonctionnel mais encore evolutif**,
destine a etre repris par l'equipe.

Ce qui est deja en place :

- connexion via **compte GitHub**
- association d'une ferme a chaque utilisateur GitHub
- choix entre reutiliser une ferme existante ou en creer une nouvelle
- ecran principal de ferme
- marche, collectivite, stock et classement visibles

Ce qu'il faut retenir pour la suite :

- les anciennes fermes locales `a` et `b` ne sont plus le mode cible
- le projet doit maintenant etre considere comme **GitHub-only**
- le dossier principal a modifier est `project/`

## Equipe 485K

### Management

- **SOUDANT Raphael** - Lead Project
- **ARMANET Andre** - Coordinator

### UX Design et Frontend

- DOUANAMOU Alexandre
- TAGODOE Koami
- SOW Bineta

### Frontend

- **DIXNEUF Arthur** - Lead Frontend
- DIOMANDE Bemisolo
- ALTUNDAG Mehmet
- OPREA Robert
- ELYAKHUNOV Ramzes

### Backend

- **DIATTA Thomas** - Lead Backend
- HANOU Aristippe
- KHALDI Rami
- PENALVA Theo
- ABUBAKER MOHAMED Mohamed
- PARRACHO Henri

## Organisation du depot

- `project/` : application principale, backend + front integre
- `docs/` : documents de projet et ressources utiles
- `.devcontainer/` : environnement partage pour Codespaces / VS Code
- `requirements-test.txt` : dependances Python pour les tests `pytest`
- `pytest.ini` : configuration de detection des tests Python

## Configuration GitHub OAuth

Le projet utilise une **OAuth App GitHub**.
Cette application doit etre creee une fois, puis ses identifiants doivent etre
configures sur la machine qui lance TinyFarm.

### 1. Creer l'application GitHub

Dans GitHub :

`Settings` -> `Developer settings` -> `OAuth Apps` -> `New OAuth App`

Configurer les champs ainsi :
⚠️ Sur Codespaces copier l'adresse locale sur le port 8080 et remplacer le "http://localhost:8080" ⚠️
- `Application name` : `TinyFarm Local`
- `Homepage URL` : `http://localhost:8080`
- `Authorization callback URL` : `http://localhost:8080/login/oauth2/code/github`

GitHub fournira ensuite :

- un `Client ID`
- un `Client Secret`

### 2. Configurer les variables d'environnement

Le projet lit ces deux variables :

- `SPRING_SECURITY_OAUTH2_CLIENT_ID`
- `SPRING_SECURITY_OAUTH2_CLIENT_SECRET`

Sous Windows PowerShell :

```powershell
$env:SPRING_SECURITY_OAUTH2_CLIENT_ID="VOTRE_CLIENT_ID"
$env:SPRING_SECURITY_OAUTH2_CLIENT_SECRET="VOTRE_CLIENT_SECRET"
```

Sous Linux / macOS :

```bash
export SPRING_SECURITY_OAUTH2_CLIENT_ID="VOTRE_CLIENT_ID"
export SPRING_SECURITY_OAUTH2_CLIENT_SECRET="VOTRE_CLIENT_SECRET"
```

Important :

- ne jamais commit le `Client Secret` dans le depot
- si le secret a ete partage ou expose, il faut le **regenerer** dans GitHub

## Lancer le projet

Depuis la racine du depot :

### Application Spring Boot

Sous Windows :

```powershell
cd project
.\mvnw.cmd spring-boot:run
```

Sous Codespaces : 

```bash
cd project
mvnw spring-boot:run
```

Sous Linux / macOS :

```bash
cd project
./mvnw spring-boot:run
```

Une fois lancee :

```text
http://localhost:8080
```

## Utiliser la connexion GitHub

Le flux attendu est le suivant :

1. ouvrir `http://localhost:8080`
2. cliquer sur le bouton de connexion GitHub
3. se connecter sur la page GitHub
4. revenir dans TinyFarm
5. si une ferme existe deja pour ce compte, choisir :
   - utiliser la ferme existante
   - ou creer une nouvelle ferme
6. si aucune ferme n'existe, TinyFarm en cree une automatiquement

## Partage pour le groupe

Si plusieurs membres du groupe doivent continuer a travailler sur le projet,
la solution la plus simple est :

- utiliser la **meme OAuth App GitHub**
- partager le **Client ID**
- garder le **Client Secret** sur la machine ou l'instance qui lance TinyFarm

Deux cas pratiques :

- **une seule machine de demo** :
  configurer les variables d'environnement une fois sur cette machine
- **une instance partagee pour toute l'equipe** :
  deploiement recommande, avec le secret uniquement cote serveur

Si chacun lance le projet sur son propre PC, il faut que chacun dispose
de la configuration OAuth locale necessaire ou passe par une instance commune.

## Lancer les tests

### Tests Java

```powershell
cd project
.\mvnw.cmd test
```

### Tests Python

Depuis la racine du depot :

```powershell
python -m venv .venv
.\.venv\Scripts\pip install -r requirements-test.txt
.\.venv\Scripts\pytest
```

## Stack technique

- **Backend** : Java, Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- **Frontend** : HTML5, CSS3, JavaScript
- **Base de donnees** : H2 pour le developpement local
- **Tests** : JUnit, Mockito, Pytest, Requests
- **Outils** : Maven Wrapper, GitHub Codespaces, Dev Containers

## Documentation utile

- `README.md` : vue d'ensemble du depot et configuration GitHub
- `project/ARCHITECTURE.md` : architecture et organisation du dossier principal
- `docs/sujet-du-projet.pdf` : sujet du projet
- `docs/spring-starters-reference.md` : reference Spring conservee comme aide

## Notes importantes

- `project/` est le dossier principal a modifier
- `project/target/` est genere par Maven et ne doit pas etre modifie a la main
- le projet vise maintenant une logique de reprise par **compte GitHub**
