# Architecture du dossier `project`

Ce dossier contient l'application principale TinyFarm.
Il regroupe le backend Spring Boot, le front statique servi par Spring Boot,
les tests Java, et les tests Python lies a l'application.

Dans l'etat actuel du projet, la connexion vise un fonctionnement
**GitHub OAuth uniquement**. Les anciennes fermes locales de demonstration
ne constituent plus le flux normal d'utilisation.

## Arborescence utile

```text
project/
|-- .mvn/
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
|-- src/
|   |-- main/
|   |   |-- java/com/farm/tinyfarm/
|   |   `-- resources/
|   |       |-- application.properties
|   |       |-- dataBaseTinyFarm.sql
|   |       |-- static/
|   |       `-- templates/
|   `-- test/
|       `-- java/com/farm/tinyfarm/
|-- tests/
|   `-- python/
`-- target/
```

## Role des principaux elements

### Build et dependances

- `.mvn/` : fichiers internes du Maven Wrapper
- `mvnw` / `mvnw.cmd` : scripts pour lancer Maven sans installation globale
- `pom.xml` : declaration des dependances Java et du build Spring Boot

Les dependances principales declarees dans `pom.xml` sont notamment :

- `spring-boot-starter-web`
- `spring-boot-starter-jdbc`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-security`
- `spring-boot-starter-oauth2-client`
- `postgresql`
- `h2`
- `spring-boot-starter-test`
- `lombok`

### Code applicatif

- `src/main/java/com/farm/tinyfarm/` : code Java principal

Sous-dossiers importants :

- `controller/` : endpoints HTTP exposes par l'application
- `service/` : logique metier
- `repository/` : acces aux donnees avec Spring Data JPA
- `model/` : entites et enums du domaine
- `security/` : configuration de securite et OAuth GitHub
- `config/` : initialisation et configuration applicative
- `outils/` : utilitaires Java partages

Points a connaitre en priorite :

- `controller/AuthController` gere les endpoints lies a la connexion GitHub
- `controller/UserController` renvoie les informations du compte OAuth courant
- `security/CustomOAuth2UserService` cree ou retrouve l'utilisateur GitHub
- `config/LegacyUserCleanupConfig` nettoie les anciens comptes `a` et `b`
- `service/FermeService` gere la creation et la recreation propre des fermes

### Ressources

- `src/main/resources/application.properties` : configuration Spring
- `src/main/resources/dataBaseTinyFarm.sql` : script SQL du projet
- `src/main/resources/static/` : front statique servi tel quel
  - `index.html`, `css/`, `js/`, `assets/`, `data/`
- `src/main/resources/templates/` : templates cote serveur si besoin

Note :

- dans l'etat actuel du projet, le front principal visible passe surtout par `static/`
- le dossier `templates/` existe, mais il n'est pas la porte d'entree principale

Pour le front, les fichiers a connaitre en priorite sont :

- `static/index.html` : structure principale
- `static/js/script.js` : orchestration generale
- `static/js/app-api.js` : appels HTTP
- `static/js/app-ui.js` : rendu et comportements d'interface
- `static/js/app-shell.js` : etat partage et references DOM

### Tests

- `src/test/java/com/farm/tinyfarm/` : tests Java / JUnit
- `tests/python/` : tests Python / pytest pour tester des endpoints HTTP

Les dependances Python de test ne sont pas dans `project/` mais a la racine du
depot via `requirements-test.txt`, avec `pytest.ini` egalement a la racine.

### Dossiers et fichiers secondaires

- `target/` : fichiers generes par Maven, a ne pas modifier a la main
- `docs/spring-starters-reference.md` : document de reference sur les starters Spring

## Commandes de base

### Lancer l'application

Depuis `project/` :

Windows :

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS :

```bash
./mvnw spring-boot:run
```

Avant le lancement, la connexion GitHub attend generalement :

- `SPRING_SECURITY_OAUTH2_CLIENT_ID`
- `SPRING_SECURITY_OAUTH2_CLIENT_SECRET`

Ces variables sont detaillees dans le `README.md`.

### Lancer les tests Java

Depuis `project/` :

```powershell
.\mvnw.cmd test
```

### Lancer les tests Python

Depuis la racine du depot :

```powershell
python -m venv .venv
.\.venv\Scripts\pip install -r requirements-test.txt
.\.venv\Scripts\pytest
```

## Regles de lecture du depot

- si tu dois modifier l'application, commence dans `project/`
- si tu cherches le backend, regarde `src/main/java/com/farm/tinyfarm/`
- si tu cherches le front servi par Spring Boot, regarde `src/main/resources/static/`
- si tu cherches le flux de connexion, commence par `security/` puis `controller/AuthController`
- si tu vois un comportement lie a `a` ou `b`, considere-le comme un reliquat historique
