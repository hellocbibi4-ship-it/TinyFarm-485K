CREATE TABLE Utilisateur(
    idUtilisateur SERIAL PRIMARY KEY,
    pseudo VARCHAR(16) NOT NULL,
    derniere_co TIMESTAMP DEFAULT NOW(),
    CONSTRAINT formatPseudo CHECK (pseudo ~* '^[a-zA-Z0-9_-]{3,50}$')
);

CREATE TABLE ferme(
    idFerme SERIAL PRIMARY KEY,
    idUtilisateur REFERENCES Utilisateur(idUtilisateur) ON DELETE CASCADE,
    nom VARCHAR(16) NOT NULL,
    solde_ecus INT DEFAULT 1500,
    Hibernation BOOL,
    date_creation TIMESTAMP DEFAULT NOW(),
    score INT DEFAULT 0,
    CONSTRAINT formatNomFerme CHECK (nom ~* '^[a-zA-Z0-9_-]{3,50}$')
);

CREATE TABLE animal(
    idFerme INTEGER NOT NULL REFERENCES ferme(idFerme) ON DELETE CASCADE,
    idAnimal SERIAL NOT NULL,
    PRIMARY KEY (idFerme, idAnimal),
    idHabitat INTEGER REFERENCES Habitat(idHabitat) ON DELETE SET NULL,
    nom VARCHAR(15),
    typeAnimal VARCHAR(50) NOT NULL CHECK(typeAnimal IN('poule','lapin','vache')),
    poids DECIMAL(5,2) NOT NULL,
    age INT NOT NULL,
    sexe VARCHAR(7) CHECK(sexe IN('male','femelle','inconnu')),
    age VARCHAR(6) CHECK (age IN('enfant','adulte')),
    estMalade BOOL DEFAULT 0,
    jaugeSante INT DEFAULT 100,
    jaugeHydration INT DEFAULT 100,
    jaugeNourriture INT DEFAULT 100,
    CONSTRAINT jaugeHydration CHECK (jaugeHydration >= 0 AND jaugeHydration <= 100),
    CONSTRAINT jaugeNourriture CHECK (jaugeNourriture >=0 AND jaugeNourriture <= 100),
    CONSTRAINT jaugeSante CHECK (jaugeSante >= 0 AND jaugeSante <= 100),
    CONSTRAINT formatNom CHECK (nom ~* '^[a-zA-Z0-9_-]{3,50}$')
);

CREATE TABLE production(
    idProd SERIAL PRIMARY KEY,
    typeProd VARCHAR(50) NOT NULL CHECK(typeProd IN('oeuf','lapin','lait')),
    quantite INT NOT NULL,
    date_production TIMESTAMP DEFAULT NOW(),
    idFerme INT REFERENCES ferme(idFerme) ON DELETE CASCADE
);

CREATE TABLE stock(
    idStock SERIAL PRIMARY KEY,
    typeStock VARCHAR(50) NOT NULL CHECK (typeStock IN('grain','eau','savon','seringue','paille')),
    quantite INT NOT NULL,
    idFerme INT REFERENCES ferme(idFerme) ON DELETE CASCADE
);

CREATE TABLE transaction(
    idTransaction SERIAL PRIMARY KEY,
    typeTransaction VARCHAR(50) NOT NULL CHECK(typeTransaction IN ('achat','vente')),
    produit VARCHAR(50) NOT NULL CHECK (produit IN('oeuf','lapin','lait','grain','eau','savon','seringue','paille')),
    quantite INT NOT NULL,
    prix_unitaire INT NOT NULL,
    date_transaction TIMESTAMP DEFAULT NOW(),
    idVendeur INT REFERENCES ferme(idUtilisateur) ON DELETE SET NULL,
    idAcheteur INT REFERENCES ferme(idUtilisateur) ON DELETE SET NULL
);

CREATE TABLE marche(
    idOffre SERIAL PRIMARY KEY,
    produit VARCHAR(50) NOT NULL CHECK(produit IN ('oeuf','lapin','lait','grain','eau','savon','seringue','paille')),
    quantite INT NOT NULL,
    prixUnitaire INT NOT NULL,
    idFerme INT REFERENCES ferme(idFerme) ON DELETE CASCADE
);

CREATE TABLE Habitat(
    idHabitat SERIAL PRIMARY KEY,
    idFerme INT REFERENCES ferme(idFerme) ON DELETE CASCADE,
    typeHabitat VARCHAR(50) NOT NULL CHECK(typeHabitat IN('poulailler','clapier','pre')),
    occupation INT
);
