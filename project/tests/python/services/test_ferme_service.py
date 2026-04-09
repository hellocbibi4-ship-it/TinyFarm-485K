import requests
import pytest

# Lancement:
# pytest project/tests/python/services/test_ferme_service.py -qsv

BASE_URL = "http://localhost:8080/api/fermes"


# Fonction qui permet de creer une ferme et de la supprimer a la fin du test.
@pytest.fixture
def new_ferme():
    # SETUP
    response = requests.post(BASE_URL, json={"nom": "Ferme_Test"})
    ferme = response.json()
    yield ferme
    # TEARDOWN
    requests.delete(f"{BASE_URL}/{ferme['idFerme']}")


def test_create_ferme():
    payload = {"nom": "Ferme485K"}
    response = requests.post(BASE_URL, json=payload)

    assert response.status_code == 201
    data = response.json()
    print(data)
    assert data["soldeEcus"] == 1500, "Le solde ecus n'est pas egal a 1500"
    assert data["score"] == 0, "Le score n'est pas egal a 0"
    assert data["nom"] == "Ferme485K", "Le nom de la ferme n'est pas 'Ferme485K'"
    requests.delete(f"{BASE_URL}/{data['idFerme']}")


def test_ajouter_ecus(new_ferme):
    ferme_id = new_ferme.get("idFerme")

    patch_url = f"{BASE_URL}/{ferme_id}/ajout-ecus"
    response = requests.patch(patch_url, params={"montant": 500})

    data = response.json()
    assert response.status_code == 200
    assert data.get("soldeEcus") == 2000
    print(data)


def test_retirer_ecus(new_ferme):
    ferme_id = new_ferme.get("idFerme")

    patch_url = f"{BASE_URL}/{ferme_id}/retirer-ecus"
    response = requests.patch(patch_url, params={"montant": 500})

    data = response.json()
    assert response.status_code == 200
    assert data.get("soldeEcus") == 1000
    print(data)


def test_ajouter_score(new_ferme):
    ferme_id = new_ferme["idFerme"]

    patch_url = f"{BASE_URL}/{ferme_id}/score"
    response = requests.patch(patch_url, params={"montant": 100})

    data = response.json()
    assert response.status_code == 200
    assert data.get("score") == 100
    print(data)
