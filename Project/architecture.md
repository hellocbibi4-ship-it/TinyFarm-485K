# Architecture du répertoire Project
## Structure des répertoires
- `~/src` : c'est le répertoire de développement principal
  - `~/src/main` : tout ce qui est du ressort du développement de l'application
    - `~/src/main/java` : tous les fichiers Java (donc pour l'API)
    - `~/src/main/resources` : les ressources du projet
      - `~/src/main/resources/static` : tous les fichiers HTML et JS
      - `~/src/main/resources/templates` : tous les fichiers CSS et les assets (images, polices, ...) du projet  
        On y retrouve aussi les fichiers de la base de données
  - `~/src/test` : tous les fichiers de test
- `pom.xml` : c'est le gestionnaire des dépendances de notre projet


## Gestion des dépendances
Le projet utilise **Maven**, avec le fichier `pom.xml` comme gestionnaire de dépendances.

### Dépendances actuelles
Les dépendances actuelles sont:
- **web** : Build web, including RESTful, applications using     Spring MVC. Uses Apache Tomcat as the default embedded container; 
- **jdbc** : Database Connectivity API that defines how a client may connect and query a database;
- **postgresql** : A JDBC and R2DBC driver that allows Java programs to connect to a PostgreSQL database using standard, database independent Java code;
- **h2** : Provides a fast in-memory database that supports JDBC API and R2DBC access, with a small (2mb) footprint. Supports embedded and server modes as well as a browser based console application.

### Ajouter une nouvelle dépendance
Dans la suite, pour ajouter pour nouvelle dépendance, il faut y (dans le fichier pom.xml) la dépendance en suivant le modèle suivant:
```xml
    <dependency>
        <groupId>l'éditeur de la dépendance (org. springframework.boot, org.postgresql, ...)</groupId>
        <artifactId>le nom exact de la dépendance</artifactId>
        <!-- scope est optionnel, compile par défaut -->
        <scope>moment d'utilisation de la dépendance (compile(par défaut), runtime, test, ...)</scope>
    </dependency>
```
Le nom des dépendances existantes et leurs utilités sont fournis dans le fichier springStarterPack.txt
Toute ces informations sont fournis sur le net (chat gpt pour aller vite).