# Architecture du dossier `project`

Ce dossier contient l'application principale TinyFarm.
Il regroupe le backend Spring Boot, le front statique servi par Spring Boot,
les tests Java, et les tests Python lies a l'application.

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
- `mvnw` / `mvnw.cmd` : scripts a utiliser pour lancer Maven sans installer
  `mvn` globalement
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
- `outils/` : utilitaires Java partages

### Ressources

- `src/main/resources/application.properties` : configuration Spring
- `src/main/resources/dataBaseTinyFarm.sql` : script SQL du projet
- `src/main/resources/static/` : front statique servi tel quel
  - `index.html`, `css/`, `js/`, `assets/`, `data/`
- `src/main/resources/templates/` : templates cote serveur si besoin

Note :
- Dans l'etat actuel du projet, le front principal visible passe surtout par
  `static/`.
- Le dossier `templates/` existe, mais il n'est pas la porte d'entree la plus
  importante aujourd'hui.

### Tests

- `src/test/java/com/farm/tinyfarm/` : tests Java / JUnit
- `tests/python/` : tests Python / pytest pour tester des endpoints HTTP

Les dependances Python de test ne sont pas dans `project/` mais a la racine du
depot via `requirements-test.txt`, avec `pytest.ini` egalement a la racine.

### Dossiers et fichiers secondaires

- `target/` : fichiers generes par Maven, a ne pas modifier a la main
- `docs/spring-starters-reference.md` : document de reference sur les
  starters Spring, utile comme aide projet mais non necessaire au build

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

- Si tu dois modifier l'application, commence dans `project/`.
- Si tu cherches le backend, regarde `src/main/java/com/farm/tinyfarm/`.
- Si tu cherches le front actuellement servi par Spring Boot, regarde
  `src/main/resources/static/`.
- Si tu cherches les tests Python, regarde `project/tests/python/`.
- Si tu vois `screens/` a la racine du depot, considere-le comme un prototype
  historique et non comme l'application principale.
