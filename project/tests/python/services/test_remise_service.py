import requests
import pytest

# Lancement:
# pytest project/tests/python/services/test_remise_service.py -qsv

BASE_URL = "http://localhost:8080/api/remise"
URL_FERME = "http://localhost:8080/api/fermes"


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


def test_remise_stocks_initiaux_vides(new_remise):
    # Tous les stocks doivent etre a 0 au depart.
    assert new_remise["stockOeuf"] == 0
    assert new_remise["stockLait"] == 0
    assert new_remise["stockNourriture"] == 0
    assert new_remise["stockEau"] == 0
    assert new_remise["stockPaille"] == 0
    assert new_remise["stockSavon"] == 0
    assert new_remise["stockSeringue"] == 0


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


def test_ajouter_stock_oeuf(new_remise):
    ferme_id = new_remise["remiseId"]
    response = requests.patch(
        f"{BASE_URL}/{ferme_id}/ajouter-stock",
        params={"montant": 5, "stock": "OEUF"},
    )
    assert response.status_code == 200
    assert response.json()["stockOeuf"] == 5


def test_ajouter_stock_lait(new_remise):
    ferme_id = new_remise["remiseId"]
    response = requests.patch(
        f"{BASE_URL}/{ferme_id}/ajouter-stock",
        params={"montant": 3, "stock": "LAIT"},
    )
    assert response.status_code == 200
    assert response.json()["stockLait"] == 3


def test_retirer_stock(new_remise):
    ferme_id = new_remise["remiseId"]
    # On ajoute d'abord 10 oeufs
    requests.patch(
        f"{BASE_URL}/{ferme_id}/ajouter-stock",
        params={"montant": 10, "stock": "OEUF"},
    )
    # Puis on en retire 4
    response = requests.patch(
        f"{BASE_URL}/{ferme_id}/retirer-stock",
        params={"montant": 4, "stock": "OEUF"},
    )
    assert response.status_code == 200
    assert response.json()["stockOeuf"] == 6


def test_retirer_stock_ramene_a_zero(new_remise):
    ferme_id = new_remise["remiseId"]
    # On ajoute 3 oeufs, puis on retire les 3 : on doit revenir a 0.
    requests.patch(
        f"{BASE_URL}/{ferme_id}/ajouter-stock",
        params={"montant": 3, "stock": "OEUF"},
    )
    response = requests.patch(
        f"{BASE_URL}/{ferme_id}/retirer-stock",
        params={"montant": 3, "stock": "OEUF"},
    )
    assert response.status_code == 200
    assert response.json()["stockOeuf"] == 0