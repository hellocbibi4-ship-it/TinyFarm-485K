import requests
import pytest

# Lancement:
# pytest project/tests/python/services/test_marche_service.py -qsv

BASE_URL = "http://localhost:8080/api/marche"
URL_FERME = "http://localhost:8080/api/fermes"


# Fonction qui permet de creer une ferme et de la supprimer a la fin du test.
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


def test_create_marche(new_ferme):
    ferme_id = new_ferme["idFerme"]
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
    assert data["produit"] == "PAILLE"
    assert data["quantite"] == 10
    assert data["prixUnitaire"] == 50


def test_ajouter_ecus(new_ferme):
    ferme_id = new_ferme["idFerme"]
    params_create = {
        "fermeId": ferme_id,
        "produit": "PAILLE",
        "quantite": 10,
        "prix": 50,
    }
    resp_create = requests.post(BASE_URL, params=params_create)
    assert resp_create.status_code == 200
    offre = resp_create.json()
    print("Offre creee :", offre)

    patch_url = f"{BASE_URL}/{ferme_id}/ajouter-ecus2"
    response = requests.patch(patch_url, json=offre)
    print("Status code:", response.status_code)
    print("Response text:", response.text)
    assert response.status_code == 200


def test_retirer_ecus(new_ferme):
    ferme_id = new_ferme["idFerme"]
    params_create = {
        "fermeId": ferme_id,
        "produit": "PAILLE",
        "quantite": 10,
        "prix": 50,
    }
    resp_create = requests.post(BASE_URL, params=params_create)
    assert resp_create.status_code == 200
    offre = resp_create.json()
    print("Offre creee :", offre)

    patch_url = f"{BASE_URL}/{ferme_id}/retirer-ecus2"
    response = requests.patch(patch_url, json=offre)
    print("Status code:", response.status_code)
    print("Response text:", response.text)
    assert response.status_code == 200


def test_transaction(new_ferme):
    ferme_id = new_ferme["idFerme"]
    params_create = {
        "fermeId": ferme_id,
        "produit": "PAILLE",
        "quantite": 10,
        "prix": 50,
    }
    resp_create = requests.post(BASE_URL, params=params_create)
    assert resp_create.status_code == 200
    offre = resp_create.json()
    print("Offre creee :", offre)
    offre_id = offre["idOffre"]

    resp_acheteur = requests.post(URL_FERME, json={"nom": "Ferme_Acheteur"})
    assert resp_acheteur.status_code == 201
    acheteur_id = resp_acheteur.json()["idFerme"]

    param_transaction = {
        "idFerme": acheteur_id,
        "idOffre": offre_id,
        "quantite": 3,
    }
    resp_transaction = requests.post(f"{BASE_URL}/transaction", params=param_transaction)
    print("Status code:", resp_transaction.status_code)
    print("Response text:", resp_transaction.text)
    assert resp_transaction.status_code == 200

    requests.delete(f"{URL_FERME}/{acheteur_id}")
