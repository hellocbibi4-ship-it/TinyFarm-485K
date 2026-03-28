import requests
import pytest

# Lancement:
# pytest project/tests/python/services/test_remise_service.py -qsv

BASE_URL = "http://localhost:8080/api/remise"
URL_FERME = "http://localhost:8080/api/fermes"


# Fonction qui permet de creer une ferme et de la supprimer a la fin du test.
@pytest.fixture
def new_remise():
    # SETUP
    response = requests.post(URL_FERME, json={"nom": "Ferme_Test"})
    assert response.status_code == 201
    ferme_data = response.json()
    ferme_id = ferme_data.get("idFerme")

    res_remise = requests.get(f"{BASE_URL}/{ferme_id}")
    assert res_remise.status_code == 200
    remise = res_remise.json()

    yield remise
    # TEARDOWN
    requests.delete(f"{URL_FERME}/{ferme_id}")


def test_create_remise():
    payload = {"nom": "Ferme485K"}
    response = requests.post(URL_FERME, json=payload)

    assert response.status_code == 201
    ferme_data = response.json()
    ferme_id = ferme_data.get("idFerme")

    res_remise = requests.get(f"{BASE_URL}/{ferme_id}")

    assert res_remise.status_code == 200
    remise_data = res_remise.json()

    print(remise_data)
    assert remise_data["remiseId"] == ferme_id
    requests.delete(f"{URL_FERME}/{ferme_data['idFerme']}")


def test_ajouter_stock(new_remise):
    ferme_id = new_remise.get("remiseId")

    patch_url = f"{BASE_URL}/{ferme_id}/ajouter-stock"
    response = requests.patch(
        patch_url,
        params={"montant": 2, "stock": "PAILLE"},
    )
    if response.status_code == 400:
        print("\nMESSAGE DU SERVEUR :", response.text)
    assert response.status_code == 200

    ferme_data = response.json()
    assert ferme_data.get("stockPaille") == 2
    print(ferme_data)
