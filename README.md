# TinyFarm Project - L2 Informatique

Bienvenue sur le depot **TinyFarm**.
Ce projet est realise dans le cadre du cours **XLG4IU050 - Developpement web cote client**
a l'Universite de Nantes.

## 🐄 A propos du projet

**TinyFarm** est un jeu de gestion de ferme en ligne.
Le joueur commence avec un emprunt de **1 500 ecus** et doit faire evoluer sa ferme
en elevant des vaches, des poules et des lapins, puis en produisant et vendant
des ressources pour rembourser sa dette.

Aujourd'hui, le depot contient :

- une **application Spring Boot** dans `project/`
- une ancienne **maquette front** dans `screens/`
- des **tests Java** et des **tests Python**
- une configuration **Codespaces / Dev Container** pour faciliter le travail d'equipe

## 👥 Equipe 485K

### 🧑‍💼 Management

- **SOUDANT Raphael** - Lead Project
- **ARMANET Andre** - Coordinator

### 🎨 UX Design et Frontend

- DOUANAMOU Alexandre
- TAGODOE Koami
- SOW Bineta

### 💻 Frontend

- **DIXNEUF Arthur** - Lead Frontend
- DIOMANDE Bemisolo
- ALTUNDAG Mehmet
- OPREA Robert
- ELYAKHUNOV Ramzes

### 🖥️ Backend

- **DIATTA Thomas** - Lead Backend
- HANOU Aristippe
- KHALDI Rami
- PENALVA Theo
- ABUBAKER MOHAMED Mohamed
- PARRACHO Henri

## 📂 Organisation du depot

- `project/` : application principale, backend + front integre
- `screens/` : prototype front historique conserve comme reference visuelle
- `docs/` : documents de projet et ressources utiles
- `.devcontainer/` : environnement partage pour Codespaces / VS Code
- `requirements-test.txt` : dependances Python pour les tests `pytest`
- `pytest.ini` : configuration de detection des tests Python

## 🚀 Lancer le projet

Depuis la racine du depot :

### Application Spring Boot

Sous Windows :

```powershell
cd project
.\mvnw.cmd spring-boot:run
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

## 🧪 Lancer les tests

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

## 🛠️ Stack technique

- **Backend** : Java, Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- **Frontend** : HTML5, CSS3, JavaScript
- **Base de donnees** : PostgreSQL, H2
- **Tests** : JUnit, Mockito, Pytest, Requests
- **Outils** : Maven Wrapper, GitHub Codespaces, Dev Containers

## 📚 Documentation utile

- `README.md` : vue d'ensemble du depot
- `project/ARCHITECTURE.md` : architecture et organisation du dossier principal
- `screens/README.md` : notes sur l'ancienne maquette front

## ✨ Notes importantes

- `project/` est le dossier principal a modifier pour travailler sur la vraie application
- `screens/` reste utile pour la reference visuelle, mais ce n'est plus l'application principale
- `project/target/` est genere par Maven et ne doit pas etre modifie a la main
