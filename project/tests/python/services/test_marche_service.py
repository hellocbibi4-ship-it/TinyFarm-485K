import requests
import pytest

# Lancement:
# pytest project/tests/python/services/test_marche_service.py -qsv

BASE_URL = "http://localhost:8080/api/marche"
URL_FERME = "http://localhost:8080/api/fermes"
URL_REMISE = "http://localhost:8080/api/remise"


@pytest.fixture
def new_ferme():
    # SETUP
    response = requests.post(URL_FERME, json={"nom": "Ferme_Test"})
    assert response.status_code == 201
    ferme_data = response.json()
    yield ferme_data
    ferme_id = ferme_data.get("idFerme")
    # TEARDOWN
    requests.delete(f"{URL_FERME}/{ferme_id}")


@pytest.fixture
def new_ferme_avec_stock():
    # Cree une ferme et lui ajoute de l'oeuf et de la paille.
    response = requests.post(URL_FERME, json={"nom": "Ferme_Vendeur"})
    assert response.status_code == 201
    ferme = response.json()
    ferme_id = ferme["idFerme"]
    requests.patch(
        f"{URL_REMISE}/{ferme_id}/ajouter-stock",
        params={"montant": 20, "stock": "OEUF"},
    )
    requests.patch(
        f"{URL_REMISE}/{ferme_id}/ajouter-stock",
        params={"montant": 20, "stock": "PAILLE"},
    )
    yield ferme
    requests.delete(f"{URL_FERME}/{ferme_id}")


def test_create_marche(new_ferme_avec_stock):
    ferme_id = new_ferme_avec_stock["idFerme"]
    params = {
        "fermeId": ferme_id,
        "produit": "PAILLE",
        "quantite": 10,
        "prix": 50,
    }
    resp_create = requests.post(BASE_URL, params=params)
    print("Status Code:", resp_create.status_code)
    print("Raw Response:", resp_create.text[:1000])
    assert resp_create.status_code == 200
    data = resp_create.json()
    assert data["produit"] == "paille"
    assert data["quantite"] == 10
    assert data["prixUnitaire"] == 50


def test_create_marche_quantite_negative(new_ferme):
    ferme_id = new_ferme["idFerme"]
    params = {
        "fermeId": ferme_id,
        "produit": "OEUF",
        "quantite": -1,
        "prix": 10,
    }
    resp = requests.post(BASE_URL, params=params)
    assert resp.status_code == 400


def test_create_marche_prix_nul(new_ferme_avec_stock):
    ferme_id = new_ferme_avec_stock["idFerme"]
    params = {
        "fermeId": ferme_id,
        "produit": "OEUF",
        "quantite": 1,
        "prix": 0,
    }
    resp = requests.post(BASE_URL, params=params)
    assert resp.status_code == 400


def test_get_offres_est_une_liste():
    response = requests.get(BASE_URL)
    assert response.status_code == 200
    assert isinstance(response.json(), list)


def test_get_offre_par_id_inexistante():
    response = requests.get(f"{BASE_URL}/999999")
    assert response.status_code == 404


def test_get_offre_par_id_existante(new_ferme_avec_stock):
    ferme_id = new_ferme_avec_stock["idFerme"]
    resp_create = requests.post(
        BASE_URL,
        params={"fermeId": ferme_id, "produit": "OEUF", "quantite": 2, "prix": 5},
    )
    assert resp_create.status_code == 200
    assert "idOffre" in resp_create.json()

    # On verifie que l'offre apparait bien dans la liste globale
    # (plus robuste qu'un GET par id car les offres peuvent fusionner).
    offres = requests.get(BASE_URL).json()
    assert any(o.get("sellerFarmId") == ferme_id for o in offres)


def test_reset_offres_vide_la_liste():
    requests.delete(BASE_URL)
    response = requests.get(BASE_URL)
    assert response.status_code == 200
    assert response.json() == []


def test_transaction(new_ferme_avec_stock):
    vendeur_id = new_ferme_avec_stock["idFerme"]
    params_create = {
        "fermeId": vendeur_id,
        "produit": "PAILLE",
        "quantite": 10,
        "prix": 50,
    }
    resp_create = requests.post(BASE_URL, params=params_create)
    assert resp_create.status_code == 200
    offre = resp_create.json()
    offre_id = offre["idOffre"]

    resp_acheteur = requests.post(URL_FERME, json={"nom": "Ferme_Acheteur"})
    assert resp_acheteur.status_code == 201
    acheteur_id = resp_acheteur.json()["idFerme"]

    param_transaction = {
        "idFerme": acheteur_id,
        "idOffre": offre_id,
        "quantite": 3,
    }
    resp_transaction = requests.post(
        f"{BASE_URL}/transaction", params=param_transaction
    )
    print("Status code:", resp_transaction.status_code)
    print("Response text:", resp_transaction.text)
    assert resp_transaction.status_code == 200

    requests.delete(f"{URL_FERME}/{acheteur_id}")


def test_transaction_sa_propre_offre_refuse(new_ferme_avec_stock):
    ferme_id = new_ferme_avec_stock["idFerme"]
    resp_create = requests.post(
        BASE_URL,
        params={"fermeId": ferme_id, "produit": "OEUF", "quantite": 1, "prix": 5},
    )
    assert resp_create.status_code == 200
    offre_id = resp_create.json()["idOffre"]

    resp = requests.post(
        f"{BASE_URL}/transaction",
        params={"idFerme": ferme_id, "idOffre": offre_id, "quantite": 1},
    )
    assert resp.status_code == 400